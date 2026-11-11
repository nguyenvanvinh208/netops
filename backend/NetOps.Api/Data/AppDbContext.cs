using Microsoft.EntityFrameworkCore;
using NetOps.Api.Models;

namespace NetOps.Api.Data
{
    public class AppDbContext : DbContext
    {
        // Constructor bắt buộc phải có để nhận cấu hình kết nối từ Program.cs
        public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }

        public DbSet<User> Users { get; set; }
        public DbSet<AuthCode> AuthCodes { get; set; }
        public DbSet<DeviceSession> DeviceSessions { get; set; }

        protected override void OnModelCreating(ModelBuilder b)
        {
            b.Entity<AuthCode>().HasIndex(c => new { c.Email, c.Purpose });
            b.Entity<DeviceSession>().HasIndex(s => s.TokenHash).IsUnique();
            b.Entity<DeviceSession>().HasIndex(s => s.UserId);
        }
    }
}
