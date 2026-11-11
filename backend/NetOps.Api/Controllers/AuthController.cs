using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using System.IdentityModel.Tokens.Jwt;
using System.Net.Mail;
using System.Security.Claims;
using System.Security.Cryptography;
using System.Text;
using NetOps.Api.Data;
using NetOps.Api.DTOs;
using NetOps.Api.Models;
using NetOps.Api.Services;

namespace NetOps.Api.Controllers
{
	[Route("api/[controller]")]
	[ApiController]
	public class AuthController : ControllerBase
	{
		private const int AccessMinutes = 30;   // JWT ngắn hạn
		private const int RefreshDays = 60;     // "nhớ thiết bị": 60 ngày, dùng càng nhiều càng được gia hạn

		private readonly AppDbContext _context;
		private readonly IConfiguration _configuration;
		private readonly AuthCodeService _codes;
		private readonly IEmailSender _mail;
		private readonly ILogger<AuthController> _log;

		public AuthController(AppDbContext context, IConfiguration configuration,
			AuthCodeService codes, IEmailSender mail, ILogger<AuthController> log)
		{
			_context = context;
			_configuration = configuration;
			_codes = codes;
			_mail = mail;
			_log = log;
		}

		// ---------- tiện ích ----------
		private static string Norm(string? e) => (e ?? "").Trim().ToLowerInvariant();

		private static bool ValidEmail(string e)
		{
			if (e.Length == 0 || e.Length > 254) return false;
			try { return new MailAddress(e).Address == e; } catch { return false; }
		}

		private static bool PasswordOk(string? p)
			=> !string.IsNullOrEmpty(p) && p.Length >= 8 && p.Any(char.IsDigit) && p.Any(char.IsLetter);

		private static string Sha256(string s)
			=> Convert.ToHexString(SHA256.HashData(Encoding.UTF8.GetBytes(s)));

		private static string NewSecret() => Convert.ToBase64String(RandomNumberGenerator.GetBytes(32));

		private Task<User?> FindUser(string email)
			=> _context.Users.FirstOrDefaultAsync(u => u.Email.ToLower() == email);

		private async Task<IActionResult> SendCode(string email, string purpose, string okMessage)
		{
			var code = await _codes.IssueAsync(email, purpose);
			if (code == null)
				return StatusCode(429, new { message = $"Vui lòng đợi {AuthCodeService.CooldownSeconds} giây rồi gửi lại mã." });
			try { await _mail.SendCodeAsync(email, purpose, code); }
			catch (Exception ex)
			{
				_log.LogError(ex, "Gửi email thất bại");
				return StatusCode(503, new { message = "Không gửi được email, vui lòng thử lại sau." });
			}
			return Ok(new
			{
				message = okMessage,
				email,
				expiresInSeconds = AuthCodeService.TtlMinutes * 60,
				resendAfterSeconds = AuthCodeService.CooldownSeconds
			});
		}

		private async Task<IActionResult> IssueTokens(User user, string? deviceName, string message)
		{
			var now = DateTime.UtcNow;
			string raw = NewSecret();
			var s = new DeviceSession
			{
				Id = Guid.NewGuid(),
				UserId = user.Id,
				TokenHash = Sha256(raw),
				DeviceName = string.IsNullOrWhiteSpace(deviceName) ? "Android" : deviceName.Trim()[..Math.Min(deviceName.Trim().Length, 60)],
				CreatedAt = now,
				LastUsedAt = now,
				ExpiresAt = now.AddDays(RefreshDays)
			};
			_context.DeviceSessions.Add(s);
			await _context.SaveChangesAsync();
			return Ok(new
			{
				message,
				token = CreateToken(user),
				refreshToken = raw,
				expiresInSeconds = AccessMinutes * 60,
				email = user.Email,
				role = user.Role
			});
		}

		// ---------- ĐĂNG KÝ + XÁC MINH EMAIL ----------

		// POST: api/Auth/register  -> tạo tài khoản "chờ xác minh" và gửi mã 6 số qua email
		[HttpPost("register")]
		public async Task<IActionResult> Register([FromBody] RegisterDto request)
		{
			var email = Norm(request.Email);
			if (!ValidEmail(email)) return BadRequest(new { message = "Email không hợp lệ." });
			if (!PasswordOk(request.Password))
				return BadRequest(new { message = "Mật khẩu tối thiểu 8 ký tự, gồm cả chữ và số." });

			var user = await FindUser(email);
			if (user != null && !user.EmailPending)
				return Conflict(new { message = "Email đã được đăng ký. Hãy đăng nhập." });

			string hash = BCrypt.Net.BCrypt.HashPassword(request.Password);
			if (user == null)
			{
				user = new User { Email = email, PasswordHash = hash, Role = "User", EmailPending = true };
				_context.Users.Add(user);
			}
			else
			{
				user.PasswordHash = hash; // đăng ký lại khi chưa xác minh: lấy mật khẩu mới nhất
			}
			await _context.SaveChangesAsync();

			return await SendCode(email, "verify", $"Đã gửi mã xác minh tới {email}.");
		}

		// POST: api/Auth/verify-email  -> nhập đúng mã thì kích hoạt tài khoản và đăng nhập luôn
		[HttpPost("verify-email")]
		public async Task<IActionResult> VerifyEmail([FromBody] VerifyEmailDto dto)
		{
			var email = Norm(dto.Email);
			var user = await FindUser(email);
			if (user == null || !user.EmailPending)
				return BadRequest(new { message = "Mã không đúng hoặc đã hết hạn." });

			if (!await _codes.VerifyAsync(email, "verify", (dto.Code ?? "").Trim()))
				return BadRequest(new { message = "Mã không đúng hoặc đã hết hạn." });

			user.EmailPending = false;
			await _context.SaveChangesAsync();
			return await IssueTokens(user, dto.DeviceName, "Xác minh email thành công!");
		}

		// POST: api/Auth/resend-code  -> gửi lại mã xác minh (cách nhau 60 giây)
		[HttpPost("resend-code")]
		public async Task<IActionResult> ResendCode([FromBody] EmailOnlyDto dto)
		{
			var email = Norm(dto.Email);
			var user = await FindUser(email);
			if (user == null || !user.EmailPending)
				return Ok(new { message = "Nếu email hợp lệ, mã mới đã được gửi." });
			return await SendCode(email, "verify", $"Đã gửi lại mã tới {email}.");
		}

		// ---------- ĐĂNG NHẬP / NHỚ THIẾT BỊ ----------

		// POST: api/Auth/login
		[HttpPost("login")]
		public async Task<IActionResult> Login([FromBody] LoginDto request)
		{
			var email = Norm(request.Email);
			var user = await FindUser(email);
			if (user == null || !BCrypt.Net.BCrypt.Verify(request.Password ?? "", user.PasswordHash))
				return Unauthorized(new { message = "Email hoặc mật khẩu không chính xác!" });

			if (user.EmailPending)
			{
				// mật khẩu đúng nhưng chưa xác minh email -> gửi lại mã và báo app mở màn nhập mã
				var code = await _codes.IssueAsync(email, "verify");
				if (code != null) { try { await _mail.SendCodeAsync(email, "verify", code); } catch (Exception ex) { _log.LogError(ex, "Gửi email thất bại"); } }
				return StatusCode(403, new { message = "Email chưa được xác minh. Chúng tôi đã gửi mã tới email của bạn.", code = "EMAIL_NOT_VERIFIED" });
			}

			return await IssueTokens(user, request.DeviceName, "Đăng nhập thành công!");
		}

		// POST: api/Auth/refresh  -> app đổi refresh token lấy JWT mới (và refresh token mới), không cần mật khẩu
		[HttpPost("refresh")]
		public async Task<IActionResult> Refresh([FromBody] RefreshDto dto)
		{
			var hash = Sha256(dto.RefreshToken ?? "");
			var now = DateTime.UtcNow;
			var s = await _context.DeviceSessions.FirstOrDefaultAsync(x => x.TokenHash == hash);
			if (s == null || s.RevokedAt != null || s.ExpiresAt < now)
				return Unauthorized(new { message = "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại." });

			var user = await _context.Users.FindAsync(s.UserId);
			if (user == null || user.EmailPending)
				return Unauthorized(new { message = "Tài khoản không hợp lệ." });

			string raw = NewSecret();          // xoay vòng: mỗi lần dùng cấp refresh token mới, cái cũ vô hiệu
			s.TokenHash = Sha256(raw);
			s.LastUsedAt = now;
			s.ExpiresAt = now.AddDays(RefreshDays);
			await _context.SaveChangesAsync();

			return Ok(new { token = CreateToken(user), refreshToken = raw, expiresInSeconds = AccessMinutes * 60 });
		}

		// POST: api/Auth/logout  -> thu hồi phiên của thiết bị này
		[HttpPost("logout")]
		public async Task<IActionResult> Logout([FromBody] RefreshDto dto)
		{
			var hash = Sha256(dto.RefreshToken ?? "");
			var s = await _context.DeviceSessions.FirstOrDefaultAsync(x => x.TokenHash == hash);
			if (s != null && s.RevokedAt == null)
			{
				s.RevokedAt = DateTime.UtcNow;
				await _context.SaveChangesAsync();
			}
			return Ok(new { message = "Đã đăng xuất." });
		}

		// GET: api/Auth/devices  -> danh sách thiết bị đang đăng nhập
		[Authorize]
		[HttpGet("devices")]
		public async Task<IActionResult> Devices()
		{
			if (!int.TryParse(User.FindFirstValue(ClaimTypes.NameIdentifier), out int uid)) return Unauthorized();
			var now = DateTime.UtcNow;
			var list = await _context.DeviceSessions
				.Where(s => s.UserId == uid && s.RevokedAt == null && s.ExpiresAt > now)
				.OrderByDescending(s => s.LastUsedAt)
				.Select(s => new { s.Id, s.DeviceName, s.CreatedAt, s.LastUsedAt })
				.ToListAsync();
			return Ok(list);
		}

		// DELETE: api/Auth/devices/{id}  -> đăng xuất 1 thiết bị từ xa
		[Authorize]
		[HttpDelete("devices/{id:guid}")]
		public async Task<IActionResult> RevokeDevice(Guid id)
		{
			if (!int.TryParse(User.FindFirstValue(ClaimTypes.NameIdentifier), out int uid)) return Unauthorized();
			var s = await _context.DeviceSessions.FirstOrDefaultAsync(x => x.Id == id && x.UserId == uid);
			if (s == null) return NotFound(new { message = "Không tìm thấy thiết bị." });
			s.RevokedAt = DateTime.UtcNow;
			await _context.SaveChangesAsync();
			return Ok(new { message = "Đã đăng xuất thiết bị." });
		}

		// ---------- QUÊN MẬT KHẨU ----------

		// POST: api/Auth/forgot-password  -> luôn trả cùng 1 câu trả lời, không tiết lộ email có tồn tại hay không
		[HttpPost("forgot-password")]
		public async Task<IActionResult> ForgotPassword([FromBody] EmailOnlyDto dto)
		{
			var email = Norm(dto.Email);
			var generic = new
			{
				message = "Nếu email tồn tại, mã đặt lại mật khẩu đã được gửi.",
				email,
				expiresInSeconds = AuthCodeService.TtlMinutes * 60,
				resendAfterSeconds = AuthCodeService.CooldownSeconds
			};
			if (!ValidEmail(email)) return BadRequest(new { message = "Email không hợp lệ." });

			var user = await FindUser(email);
			if (user != null && !user.EmailPending)
			{
				var code = await _codes.IssueAsync(email, "reset");
				if (code != null)
				{
					try { await _mail.SendCodeAsync(email, "reset", code); }
					catch (Exception ex) { _log.LogError(ex, "Gửi email thất bại"); }
				}
			}
			return Ok(generic);
		}

		// POST: api/Auth/verify-reset-code  -> nhập đúng mã thì nhận "vé" resetToken để đổi mật khẩu
		[HttpPost("verify-reset-code")]
		public async Task<IActionResult> VerifyResetCode([FromBody] VerifyResetDto dto)
		{
			var email = Norm(dto.Email);
			if (!await _codes.VerifyAsync(email, "reset", (dto.Code ?? "").Trim()))
				return BadRequest(new { message = "Mã không đúng hoặc đã hết hạn." });
			string grant = await _codes.CreateGrantAsync(email);
			return Ok(new { resetToken = grant });
		}

		// POST: api/Auth/reset-password  -> đặt mật khẩu mới, đăng xuất mọi thiết bị
		[HttpPost("reset-password")]
		public async Task<IActionResult> ResetPassword([FromBody] ResetPasswordDto dto)
		{
			var email = Norm(dto.Email);
			if (!PasswordOk(dto.NewPassword))
				return BadRequest(new { message = "Mật khẩu tối thiểu 8 ký tự, gồm cả chữ và số." });

			var user = await FindUser(email);
			if (user == null || !await _codes.VerifyAsync(email, "resetgrant", dto.ResetToken ?? ""))
				return BadRequest(new { message = "Phiên đặt lại mật khẩu không hợp lệ hoặc đã hết hạn." });

			user.PasswordHash = BCrypt.Net.BCrypt.HashPassword(dto.NewPassword);
			var now = DateTime.UtcNow;
			var sessions = await _context.DeviceSessions.Where(s => s.UserId == user.Id && s.RevokedAt == null).ToListAsync();
			foreach (var s in sessions) s.RevokedAt = now;   // an toàn: mọi thiết bị phải đăng nhập lại
			await _context.SaveChangesAsync();
			return Ok(new { message = "Đặt lại mật khẩu thành công. Hãy đăng nhập lại." });
		}

		// ---------- JWT ----------
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
				Expires = DateTime.UtcNow.AddMinutes(AccessMinutes),
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
