using System.ComponentModel.DataAnnotations;
using System.Text.Json.Serialization;

namespace NetOps.Api.Models
{
	public class User
	{
		[Key]
		public int Id { get; set; }

		[Required]
		public string Email { get; set; } = string.Empty;

		[Required]
		[JsonIgnore] // Giấu mật khẩu, không bao giờ vô tình gửi password xuống App Android
		public string PasswordHash { get; set; } = string.Empty;

		[Required]
		public string Role { get; set; } = "Operator"; // Mặc định là Operator

		// true = vừa đăng ký, chưa nhập mã xác minh email (chưa được đăng nhập)
		// Tài khoản cũ có sẵn trong DB sẽ mặc định false = đã xác minh, vẫn đăng nhập bình thường.
		public bool EmailPending { get; set; } = false;
	}
}
