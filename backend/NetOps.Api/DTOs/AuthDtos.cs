namespace NetOps.Api.DTOs
{
    public class VerifyEmailDto
    {
        public string Email { get; set; } = string.Empty;
        public string Code { get; set; } = string.Empty;
        public string? DeviceName { get; set; }
    }
    public class EmailOnlyDto { public string Email { get; set; } = string.Empty; }
    public class VerifyResetDto
    {
        public string Email { get; set; } = string.Empty;
        public string Code { get; set; } = string.Empty;
    }
    public class ResetPasswordDto
    {
        public string Email { get; set; } = string.Empty;
        public string ResetToken { get; set; } = string.Empty;
        public string NewPassword { get; set; } = string.Empty;
    }
    public class RefreshDto { public string RefreshToken { get; set; } = string.Empty; }
}
