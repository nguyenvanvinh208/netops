using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using NetOps.Api.Data;
using System.Text;

var builder = WebApplication.CreateBuilder(args);

// 1. THÊM DỊCH VỤ VÀO ĐÂY
builder.Services.AddControllers(); // Nhận diện các Controller (như AuthController, ProfileController)
builder.Services.AddOpenApi();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(); // Đăng ký dịch vụ tạo Swagger

// Cấu hình kết nối PostgreSQL
builder.Services.AddDbContext<AppDbContext>(options =>
    options.UseNpgsql(builder.Configuration.GetConnectionString("DefaultConnection")));

// Giám sát server qua SSH (đọc CPU/RAM/disk/container của Server 1 và Server 2)
builder.Services.AddMemoryCache();
builder.Services.Configure<NetOps.Api.Services.MonitoringOptions>(builder.Configuration.GetSection("Monitoring"));
builder.Services.AddSingleton<NetOps.Api.Services.ServerMonitor>();

// Gửi email OTP (Gmail SMTP) + tạo/kiểm tra mã xác minh
builder.Services.Configure<NetOps.Api.Services.SmtpOptions>(builder.Configuration.GetSection("Smtp"));
builder.Services.AddScoped<NetOps.Api.Services.IEmailSender, NetOps.Api.Services.EmailSender>();
builder.Services.AddScoped<NetOps.Api.Services.AuthCodeService>();

// CẤU HÌNH JWT AUTHENTICATION (BẮT BUỘC ĐỂ DÙNG [Authorize])
builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
})
.AddJwtBearer(options =>
{
    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateIssuer = true,
        ValidateAudience = true,
        ValidateLifetime = true,
        ValidateIssuerSigningKey = true,
        ValidIssuer = builder.Configuration["JwtSettings:Issuer"],
        ValidAudience = builder.Configuration["JwtSettings:Audience"],
        IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(builder.Configuration["JwtSettings:Secret"]!))
    };
});

// 2. CHỐT CẤU HÌNH VÀ BUILD APP
var app = builder.Build();

// Tự áp migration khi khởi động (DB mới trên server sẽ tự có bảng)
using (var scope = app.Services.CreateScope())
{
    scope.ServiceProvider.GetRequiredService<AppDbContext>().Database.Migrate();
}

// 3. CẤU HÌNH HTTP PIPELINE
// Đưa Swagger ra ngoài để chắc chắn nó luôn chạy không bị phụ thuộc vào biến môi trường
app.UseSwagger();
app.UseSwaggerUI(c =>
{
    c.SwaggerEndpoint("/swagger/v1/swagger.json", "NetOps API V1");
    c.RoutePrefix = string.Empty; // Đưa giao diện Swagger ra ngay trang chủ (http://localhost:5146/)
});

if (app.Environment.IsDevelopment())
{
    app.MapOpenApi();
}

// HTTPS do Caddy xử lý ở phía trước, nên không redirect trong app

// KÍCH HOẠT XÁC THỰC VÀ PHÂN QUYỀN (Rất quan trọng: Phải đặt trước UseAuthorization và MapControllers)
app.UseAuthentication();
app.UseAuthorization();

// Map các Controller vào app
app.MapControllers();

var summaries = new[]
{
    "Freezing", "Bracing", "Chilly", "Cool", "Mild", "Warm", "Balmy", "Hot", "Sweltering", "Scorching"
};

app.MapGet("/weatherforecast", () =>
{
    var forecast = Enumerable.Range(1, 5).Select(index =>
        new WeatherForecast
        (
            DateOnly.FromDateTime(DateTime.Now.AddDays(index)),
            Random.Shared.Next(-20, 55),
            summaries[Random.Shared.Next(summaries.Length)]
        ))
        .ToArray();
    return forecast;
})
.WithName("GetWeatherForecast");

// 4. LỆNH CHẠY KHỞI ĐỘNG SERVER
app.Run();

// 5. KHAI BÁO CLASS/RECORD
record WeatherForecast(DateOnly Date, int TemperatureC, string? Summary)
{
    public int TemperatureF => 32 + (int)(TemperatureC / 0.5556);
}