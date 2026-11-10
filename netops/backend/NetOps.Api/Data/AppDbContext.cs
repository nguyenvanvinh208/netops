using Microsoft.EntityFrameworkCore;
using NetOps.Api.Models;

namespace NetOps.Api.Data
{
    public class AppDbContext : DbContext
    {
        // Constructor bắt buộc phải có để nhận cấu hình kết nối từ Program.cs
        public AppDbContext(DbContextOptions<AppDbContext> options) : base(options) { }

        // Báo cho EF Core biết là cần tạo bảng Users trong Database dựa trên class User
        public DbSet<User> Users { get; set; }
    }
}