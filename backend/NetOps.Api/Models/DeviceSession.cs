using System.ComponentModel.DataAnnotations;

namespace NetOps.Api.Models
{
	/// <summary>Một thiết bị đã đăng nhập. Mỗi dòng giữ 1 refresh token (đã băm) để app tự đăng nhập lại mà không hỏi mật khẩu.</summary>
	public class DeviceSession
	{
		[Key]
		public Guid Id { get; set; }
		public int UserId { get; set; }
		[Required] public string TokenHash { get; set; } = string.Empty;
		public string DeviceName { get; set; } = "Android";
		public DateTime CreatedAt { get; set; }
		public DateTime LastUsedAt { get; set; }
		public DateTime ExpiresAt { get; set; }
		public DateTime? RevokedAt { get; set; }
	}
}
