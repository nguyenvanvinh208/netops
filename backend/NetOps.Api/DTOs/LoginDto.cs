namespace NetOps.Api.DTOs
{
    public class LoginDto
    {
        public string Email { get; set; } = string.Empty;
        public string Password { get; set; } = string.Empty;
        public string? DeviceName { get; set; }   // vd "Pixel 7" - hiện trong danh sách thiết bị
    }
}
