using System.Security.Cryptography;
using System.Text;
using Microsoft.EntityFrameworkCore;
using NetOps.Api.Data;
using NetOps.Api.Models;

namespace NetOps.Api.Services
{
	/// <summary>Tạo / kiểm tra mã 6 số: băm HMAC, hết hạn 10 phút, tối đa 5 lần nhập sai, gửi lại cách nhau 60 giây.</summary>
	public class AuthCodeService
	{
		public const int TtlMinutes = 10;
		public const int MaxAttempts = 5;
		public const int CooldownSeconds = 60;

		private readonly AppDbContext _db;
		private readonly byte[] _key;

		public AuthCodeService(AppDbContext db, IConfiguration cfg)
		{
			_db = db;
			_key = Encoding.UTF8.GetBytes(cfg["JwtSettings:Secret"] ?? "SuperDefaultSecretKey123456");
		}

		private string Hash(string email, string purpose, string secret)
			=> Convert.ToHexString(HMACSHA256.HashData(_key, Encoding.UTF8.GetBytes($"{email}|{purpose}|{secret}")));

		private async Task Store(string email, string purpose, string secret)
		{
			var old = _db.AuthCodes.Where(c => c.Email == email && c.Purpose == purpose);
			_db.AuthCodes.RemoveRange(old);
			var now = DateTime.UtcNow;
			_db.AuthCodes.Add(new AuthCode
			{
				Email = email, Purpose = purpose, CodeHash = Hash(email, purpose, secret),
				CreatedAt = now, ExpiresAt = now.AddMinutes(TtlMinutes), Attempts = 0
			});
			await _db.SaveChangesAsync();
		}

		/// <summary>Trả về mã 6 số mới, hoặc null nếu vừa gửi chưa đủ 60 giây.</summary>
		public async Task<string?> IssueAsync(string email, string purpose)
		{
			var last = await _db.AuthCodes
				.Where(c => c.Email == email && c.Purpose == purpose)
				.OrderByDescending(c => c.CreatedAt).FirstOrDefaultAsync();
			if (last != null && (DateTime.UtcNow - last.CreatedAt).TotalSeconds < CooldownSeconds) return null;

			string code = RandomNumberGenerator.GetInt32(0, 1_000_000).ToString("D6");
			await Store(email, purpose, code);
			return code;
		}

		/// <summary>Đúng mã => xóa mã (dùng 1 lần) và trả true.</summary>
		public async Task<bool> VerifyAsync(string email, string purpose, string secret)
		{
			var c = await _db.AuthCodes
				.Where(x => x.Email == email && x.Purpose == purpose)
				.OrderByDescending(x => x.CreatedAt).FirstOrDefaultAsync();
			if (c == null || c.ExpiresAt < DateTime.UtcNow || c.Attempts >= MaxAttempts) return false;

			c.Attempts++;
			bool ok = CryptographicOperations.FixedTimeEquals(
				Encoding.UTF8.GetBytes(c.CodeHash), Encoding.UTF8.GetBytes(Hash(email, purpose, secret)));
			if (ok) _db.AuthCodes.Remove(c);
			await _db.SaveChangesAsync();
			return ok;
		}

		/// <summary>Sau khi nhập đúng mã quên mật khẩu: cấp "vé" ngẫu nhiên để đổi mật khẩu (hạn 10 phút).</summary>
		public async Task<string> CreateGrantAsync(string email)
		{
			string token = Convert.ToBase64String(RandomNumberGenerator.GetBytes(32));
			await Store(email, "resetgrant", token);
			return token;
		}
	}
}
