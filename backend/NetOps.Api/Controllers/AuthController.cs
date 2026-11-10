using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using NetOps.Api.Data;
using NetOps.Api.DTOs;
using NetOps.Api.Models;

namespace NetOps.Api.Controllers
{
	[Route("api/[controller]")]
	[ApiController]
	public class AuthController : ControllerBase
	{
		private readonly AppDbContext _context;
		private readonly IConfiguration _configuration;

		public AuthController(AppDbContext context, IConfiguration configuration)
		{
			_context = context;
			_configuration = configuration;
		}

		// POST: api/Auth/register
		[HttpPost("register")]
		public async Task<IActionResult> Register([FromBody] RegisterDto request)
		{
			// Kiểm tra xem email đã tồn tại chưa
			if (await _context.Users.AnyAsync(u => u.Email == request.Email))
			{
				return BadRequest(new { message = "Email đã tồn tại trong hệ thống!" });
			}

			// Băm mật khẩu bằng BCrypt
			string passwordHash = BCrypt.Net.BCrypt.HashPassword(request.Password);

			var newUser = new User
			{
				Email = request.Email,
				PasswordHash = passwordHash,
				Role = "User" // Mặc định là User, sau này có thể phân quyền Admin
			};

			_context.Users.Add(newUser);
			await _context.SaveChangesAsync();

			return Ok(new { message = "Đăng ký thành công tài khoản!" });
		}

		// POST: api/Auth/login
		[HttpPost("login")]
		public async Task<IActionResult> Login([FromBody] LoginDto request)
		{
			var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == request.Email);
			if (user == null)
			{
				return Unauthorized(new { message = "Email hoặc mật khẩu không chính xác!" });
			}

			// Kiểm tra mật khẩu đã băm
			bool isPasswordValid = BCrypt.Net.BCrypt.Verify(request.Password, user.PasswordHash);
			if (!isPasswordValid)
			{
				return Unauthorized(new { message = "Email hoặc mật khẩu không chính xác!" });
			}

			// Tạo JWT Token
			string token = CreateToken(user);

			return Ok(new { message = "Đăng nhập thành công!", token = token });
		}

		private string CreateToken(User user)
		{
			var claims = new List<Claim>
			{
				new Claim(ClaimTypes.NameIdentifier, user.Id.ToString()),
				new Claim(ClaimTypes.Email, user.Email),
				new Claim(ClaimTypes.Role, user.Role)
			};

			var jwtKey = _configuration["JwtSettings:Secret"] ?? "SuperDefaultSecretKey123456";
			var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwtKey));
			var creds = new SigningCredentials(key, SecurityAlgorithms.HmacSha512Signature);

			var tokenDescriptor = new SecurityTokenDescriptor
			{
				Subject = new ClaimsIdentity(claims),
				Expires = DateTime.Now.AddDays(1), // Token có hạn trong 1 ngày
				Issuer = _configuration["JwtSettings:Issuer"],
				Audience = _configuration["JwtSettings:Audience"],
				SigningCredentials = creds
			};

			var tokenHandler = new JwtSecurityTokenHandler();
			var token = tokenHandler.CreateToken(tokenDescriptor);

			return tokenHandler.WriteToken(token);
		}
	}
}