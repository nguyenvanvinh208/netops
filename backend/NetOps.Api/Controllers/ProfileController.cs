using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;
using NetOps.Api.Data;
using NetOps.Api.Models;

namespace NetOps.Api.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    [Authorize]
    public class ProfileController : ControllerBase
    {
        private readonly AppDbContext _context;

        public ProfileController(AppDbContext context)
        {
            _context = context;
        }

        // 1. Xem thông tin cá nhân
        [HttpGet("me")]
        public async Task<IActionResult> GetMyProfile()
        {
            var userEmail = User.FindFirstValue(ClaimTypes.Email);

            if (string.IsNullOrEmpty(userEmail))
            {
                return Unauthorized(new { message = "Token không hợp lệ hoặc đã hết hạn." });
            }

            var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == userEmail);

            if (user == null)
            {
                return NotFound(new { message = "Không tìm thấy thông tin người dùng." });
            }

            return Ok(new
            {
                id = user.Id,
                email = user.Email
            });
        }

        // 2. Cập nhật thông tin cá nhân
        [HttpPut("update")]
        public async Task<IActionResult> UpdateProfile([FromBody] NetOps.Api.DTOs.UpdateProfileDto model)
        {
            var userEmail = User.FindFirstValue(ClaimTypes.Email);

            if (string.IsNullOrEmpty(userEmail))
            {
                return Unauthorized(new { message = "Token không hợp lệ hoặc đã hết hạn." });
            }

            var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == userEmail);

            if (user == null)
            {
                return NotFound(new { message = "Không tìm thấy thông tin người dùng." });
            }

            if (!string.IsNullOrEmpty(model.Email))
            {
                var existingUser = await _context.Users.FirstOrDefaultAsync(u => u.Email == model.Email);
                if (existingUser != null && existingUser.Id != user.Id)
                {
                    return BadRequest(new { message = "Email này đã được sử dụng bởi tài khoản khác." });
                }

                user.Email = model.Email;
            }

            await _context.SaveChangesAsync();

            return Ok(new
            {
                message = "Cập nhật thông tin thành công!",
                id = user.Id,
                email = user.Email
            });
        }

        // 3. Đổi mật khẩu
        [HttpPut("change-password")]
        public async Task<IActionResult> ChangePassword([FromBody] NetOps.Api.DTOs.ChangePasswordDto model)
        {
            var userEmail = User.FindFirstValue(ClaimTypes.Email);

            if (string.IsNullOrEmpty(userEmail))
            {
                return Unauthorized(new { message = "Token không hợp lệ hoặc đã hết hạn." });
            }

            var user = await _context.Users.FirstOrDefaultAsync(u => u.Email == userEmail);

            if (user == null)
            {
                return NotFound(new { message = "Không tìm thấy thông tin người dùng." });
            }

            // Kiểm tra mật khẩu cũ bằng BCrypt
            if (!BCrypt.Net.BCrypt.Verify(model.OldPassword, user.PasswordHash))
            {
                return BadRequest(new { message = "Mật khẩu cũ không chính xác." });
            }

            // Cập nhật mật khẩu mới (phải băm trước khi lưu)
            user.PasswordHash = BCrypt.Net.BCrypt.HashPassword(model.NewPassword);
            await _context.SaveChangesAsync();

            return Ok(new { message = "Đổi mật khẩu thành công!" });
        }
    }
}