using System.Globalization;
using Microsoft.Extensions.Options;
using Renci.SshNet;

namespace NetOps.Api.Services
{
    public class ServerEntry
    {
        public string Name { get; set; } = "";
        public string Host { get; set; } = "";
        public int Port { get; set; } = 22;
        public string User { get; set; } = "azureuser";
    }

    public class MonitoringOptions
    {
        public string KeyPath { get; set; } = "/run/secrets/netops_ssh_key";
        public List<ServerEntry> Servers { get; set; } = new List<ServerEntry>();
    }

    public record ContainerInfo(string Name, string Image, string Status, double Cpu);

    public record ServerSnapshot(
        int Id, string Name, string Host, bool Online,
        double Cpu, double Ram, double Disk, long UptimeSeconds,
        List<ContainerInfo> Containers);

    /// <summary>Đăng nhập SSH vào từng server, chạy một đoạn lệnh và đọc kết quả.</summary>
    public class ServerMonitor
    {
        private readonly MonitoringOptions _opt;
        private readonly ILogger<ServerMonitor> _log;

        public ServerMonitor(IOptions<MonitoringOptions> opt, ILogger<ServerMonitor> log)
        {
            _opt = opt.Value;
            _log = log;
        }

        public IReadOnlyList<ServerEntry> Servers => _opt.Servers;

        // Mỗi dòng in ra có nhãn đầu dòng để C# đọc lại.
        private const string Script = """
a=$(head -1 /proc/stat); sleep 1; b=$(head -1 /proc/stat)
echo "CPU1 $a"
echo "CPU2 $b"
echo "MEM $(free -m | awk '/^Mem:/ {print $2, $7}')"
echo "DISK $(df -P / | awk 'NR==2 {print $2, $3}')"
echo "UP $(cut -d' ' -f1 /proc/uptime)"
if command -v docker >/dev/null 2>&1; then
  docker ps -a --format 'CT|{{.Names}}|{{.Image}}|{{.Status}}' 2>/dev/null
  docker stats --no-stream --format 'ST|{{.Name}}|{{.CPUPerc}}' 2>/dev/null
fi
""";

        public Task<ServerSnapshot> CollectAsync(int id, ServerEntry e) => Task.Run(() => Collect(id, e));

        private ServerSnapshot Collect(int id, ServerEntry e)
        {
            try
            {
                using var key = new PrivateKeyFile(_opt.KeyPath);
                using var client = new SshClient(e.Host, e.Port, e.User, key);
                client.ConnectionInfo.Timeout = TimeSpan.FromSeconds(5);
                client.Connect();
                var cmd = client.RunCommand(Script);
                client.Disconnect();
                return Parse(id, e, cmd.Result);
            }
            catch (Exception ex)
            {
                _log.LogWarning(ex, "Khong ket noi duoc {Name} ({Host})", e.Name, e.Host);
                return new ServerSnapshot(id, e.Name, e.Host, false, 0, 0, 0, 0, new List<ContainerInfo>());
            }
        }

        private static double Num(string s)
            => double.TryParse(s, NumberStyles.Float, CultureInfo.InvariantCulture, out var v) ? v : 0;

        private static long[] CpuFields(string line)
        {
            // "CPU1 cpu  user nice system idle iowait irq softirq steal ..."
            var p = line.Split(' ', StringSplitOptions.RemoveEmptyEntries);
            var list = new List<long>();
            for (int i = 2; i < p.Length && list.Count < 8; i++)
                list.Add(long.TryParse(p[i], out var v) ? v : 0);
            return list.ToArray();
        }

        private static ServerSnapshot Parse(int id, ServerEntry e, string output)
        {
            long[]? c1 = null, c2 = null;
            double ram = 0, disk = 0, up = 0;
            var cpuByName = new Dictionary<string, double>();
            var ct = new List<(string Name, string Image, string Status)>();

            foreach (var raw in output.Split('\n'))
            {
                var line = raw.Trim();
                if (line.StartsWith("CPU1 ")) c1 = CpuFields(line);
                else if (line.StartsWith("CPU2 ")) c2 = CpuFields(line);
                else if (line.StartsWith("MEM "))
                {
                    var p = line.Split(' ', StringSplitOptions.RemoveEmptyEntries);
                    if (p.Length >= 3 && Num(p[1]) > 0) ram = (Num(p[1]) - Num(p[2])) / Num(p[1]) * 100;
                }
                else if (line.StartsWith("DISK "))
                {
                    var p = line.Split(' ', StringSplitOptions.RemoveEmptyEntries);
                    if (p.Length >= 3 && Num(p[1]) > 0) disk = Num(p[2]) / Num(p[1]) * 100;
                }
                else if (line.StartsWith("UP ")) up = Num(line.Substring(3));
                else if (line.StartsWith("CT|"))
                {
                    var p = line.Split('|');
                    if (p.Length >= 4) ct.Add((p[1], p[2], p[3]));
                }
                else if (line.StartsWith("ST|"))
                {
                    var p = line.Split('|');
                    if (p.Length >= 3) cpuByName[p[1]] = Num(p[2].TrimEnd('%'));
                }
            }

            double cpu = 0;
            if (c1 != null && c2 != null && c1.Length >= 5 && c2.Length >= 5)
            {
                double total1 = c1.Sum(), total2 = c2.Sum();
                double idle1 = c1[3] + c1[4], idle2 = c2[3] + c2[4];
                double dTotal = total2 - total1;
                if (dTotal > 0) cpu = (dTotal - (idle2 - idle1)) / dTotal * 100;
            }

            var containers = ct.Select(c => new ContainerInfo(
                c.Name, c.Image,
                c.Status.StartsWith("Up") ? "Running" : "Stopped",
                cpuByName.TryGetValue(c.Name, out var v) ? v : 0)).ToList();

            return new ServerSnapshot(id, e.Name, e.Host, true,
                Math.Clamp(cpu, 0, 100), Math.Clamp(ram, 0, 100), Math.Clamp(disk, 0, 100),
                (long)up, containers);
        }
    }
}
