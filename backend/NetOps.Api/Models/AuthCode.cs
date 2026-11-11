using System.ComponentModel.DataAnnotations;

namespace NetOps.Api.Models
{
	/// <summary>Mã 6 số gửi qua email (xác minh đăng ký / quên mật khẩu). Chỉ lưu bản băm, không lưu mã gốc.</summary>
	public class AuthCode
	{
		[Key]
		public int Id { get; set; }
		[Required] public string Email { get; set; } = string.Empty;
		// "verify" = xác minh email, "reset" = quên mật khẩu, "resetgrant" = vé đổi mật khẩu sau khi nhập đúng mã
		[Required] public string Purpose { get; set; } = string.Empty;
		[Required] public string CodeHash { get; set; } = string.Empty;
		public DateTime CreatedAt { get; set; }
		public DateTime ExpiresAt { get; set; }
		public int Attempts { get; set; }
	}
}
