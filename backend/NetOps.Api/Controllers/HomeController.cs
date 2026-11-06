using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace NetOps.Api.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize] // Bắt buộc phải có Token (đã đăng nhập) mới được xem Dashboard
    public class HomeController : ControllerBase
    {
        [HttpGet("overview")]
        public IActionResult GetOverview()
        {
            // Trả về dữ liệu mẫu (Mock data) khớp với yêu cầu thiết kế app: 2 Servers, 3 Containers
            var dashboardData = new
            {
                totalServers = 2,
                totalContainers = 3,
                alerts = 1,
                servers = new[]
                {
                    new { id = 1, name = "Ubuntu-Web", ipAddress = "192.168.1.10", status = "Running" },
                    new { id = 2, name = "CentOS-DB", ipAddress = "192.168.1.11", status = "Offline" }
                },
                containers = new[]
                {
                    new { id = 1, name = "nginx-proxy", image = "nginx:latest", status = "Running", serverId = 1 },
                    new { id = 2, name = "postgres-db", image = "postgres:15", status = "Running", serverId = 2 },
                    new { id = 3, name = "redis-cache", image = "redis:alpine", status = "Stopped", serverId = 1 }
                }
            };

            return Ok(dashboardData);
        }
    }
}