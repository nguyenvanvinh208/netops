using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Caching.Memory;
using NetOps.Api.Services;

namespace NetOps.Api.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize] // Bắt buộc có token (đã đăng nhập)
    public class HomeController : ControllerBase
    {
        private readonly ServerMonitor _monitor;
        private readonly IMemoryCache _cache;

        public HomeController(ServerMonitor monitor, IMemoryCache cache)
        {
            _monitor = monitor;
            _cache = cache;
        }

        // GET: api/Home/overview  (số liệu thật đo từ các server, cache 5 giây)
        [HttpGet("overview")]
        public async Task<IActionResult> GetOverview()
        {
            var data = await _cache.GetOrCreateAsync("overview", async entry =>
            {
                entry.AbsoluteExpirationRelativeToNow = TimeSpan.FromSeconds(5);
                return await BuildAsync();
            });
            return Ok(data);
        }

        private async Task<object> BuildAsync()
        {
            var entries = _monitor.Servers;
            var snaps = await Task.WhenAll(entries.Select((e, i) => _monitor.CollectAsync(i + 1, e)));

            var servers = snaps.Select(s => new
            {
                id = s.Id,
                name = s.Name,
                ipAddress = s.Host,
                status = s.Online ? "Running" : "Offline",
                cpu = Math.Round(s.Cpu),
                ram = Math.Round(s.Ram),
                disk = Math.Round(s.Disk),
                uptime = s.UptimeSeconds
            }).ToList();

            int cid = 0;
            var containers = snaps.SelectMany(s => s.Containers.Select(c => new
            {
                id = ++cid,
                name = c.Name,
                image = c.Image,
                status = c.Status,
                serverId = s.Id,
                cpu = Math.Round(c.Cpu, 1)
            })).ToList();

            var attention = new List<object>();
            foreach (var s in snaps)
            {
                if (!s.Online)
                {
                    attention.Add(new { title = $"{s.Name} offline", sub = $"{s.Host} · không kết nối được SSH", severity = "critical", serverId = s.Id });
                    continue;
                }
                if (s.Cpu >= 85) attention.Add(new { title = $"CPU cao trên {s.Name}", sub = $"CPU {Math.Round(s.Cpu)}%", severity = "critical", serverId = s.Id });
                if (s.Ram >= 90) attention.Add(new { title = $"RAM cao trên {s.Name}", sub = $"RAM {Math.Round(s.Ram)}%", severity = "critical", serverId = s.Id });
                if (s.Disk >= 90) attention.Add(new { title = $"Ổ đĩa gần đầy trên {s.Name}", sub = $"Disk {Math.Round(s.Disk)}%", severity = "critical", serverId = s.Id });
                foreach (var c in s.Containers.Where(c => c.Status != "Running"))
                    attention.Add(new { title = $"Container {c.Name} đã dừng", sub = $"{s.Name} · {c.Image}", severity = "warning", serverId = s.Id });
            }

            return new
            {
                totalServers = servers.Count,
                totalContainers = containers.Count,
                alerts = attention.Count,
                servers,
                containers,
                attention,
                updatedAt = DateTime.UtcNow
            };
        }
    }
}
