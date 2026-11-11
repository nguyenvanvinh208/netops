using MailKit.Net.Smtp;
using MailKit.Security;
using Microsoft.Extensions.Options;
using MimeKit;

namespace NetOps.Api.Services
{
	public class SmtpOptions
	{
		public string Host { get; set; } = "smtp.gmail.com";
		public int Port { get; set; } = 587;
		public string User { get; set; } = "";      // Gmail dùng để gửi, vd netops.noreply@gmail.com
		public string Password { get; set; } = "";  // App Password 16 ký tự của Gmail (KHÔNG phải mật khẩu đăng nhập)
		public string FromName { get; set; } = "NetOps";
	}

	public interface IEmailSender
	{
		Task SendCodeAsync(string to, string purpose, string code);
	}

	public class EmailSender : IEmailSender
	{
		private readonly SmtpOptions _o;
		private readonly ILogger<EmailSender> _log;

		public EmailSender(IOptions<SmtpOptions> o, ILogger<EmailSender> log) { _o = o.Value; _log = log; }

		public async Task SendCodeAsync(string to, string purpose, string code)
		{
			// Chưa cấu hình SMTP (chạy máy local): in mã ra log để vẫn test được.
			if (string.IsNullOrWhiteSpace(_o.User))
			{
				_log.LogWarning("[DEV - chưa cấu hình SMTP] Mã {Purpose} cho {To}: {Code}", purpose, to, code);
				return;
			}

			bool reset = purpose == "reset";
			string subject = reset ? "NetOps - Mã đặt lại mật khẩu" : "NetOps - Mã xác minh email";
			string intro = reset ? "Bạn vừa yêu cầu đặt lại mật khẩu NetOps." : "Cảm ơn bạn đã đăng ký NetOps.";
			string html =
				$"<div style='font-family:Arial,sans-serif;max-width:420px;margin:auto;padding:24px;border:1px solid #e5e7eb;border-radius:12px'>" +
				$"<h2 style='margin:0 0 12px;color:#0e7490'>NetOps</h2>" +
				$"<p>{intro} Mã của bạn là:</p>" +
				$"<p style='font-size:34px;font-weight:bold;letter-spacing:8px;margin:16px 0'>{code}</p>" +
				$"<p style='color:#6b7280;font-size:13px'>Mã có hiệu lực 10 phút. Nếu không phải bạn thực hiện, hãy bỏ qua email này và không chia sẻ mã cho ai.</p></div>";

			var msg = new MimeMessage();
			msg.From.Add(new MailboxAddress(_o.FromName, _o.User));
			msg.To.Add(MailboxAddress.Parse(to));
			msg.Subject = subject;
			msg.Body = new BodyBuilder
			{
				HtmlBody = html,
				TextBody = $"{intro}\nMã của bạn: {code}\nMã có hiệu lực 10 phút."
			}.ToMessageBody();

			using var client = new SmtpClient();
			await client.ConnectAsync(_o.Host, _o.Port, SecureSocketOptions.StartTls);
			await client.AuthenticateAsync(_o.User, _o.Password.Replace(" ", ""));
			await client.SendAsync(msg);
			await client.DisconnectAsync(true);
		}
	}
}
