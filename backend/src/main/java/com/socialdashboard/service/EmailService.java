package com.socialdashboard.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @Value("${app.mail.from:}")
    private String fromEmail;

    @Value("${app.mail.sender-name:KLMedia}")
    private String senderName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Check if SMTP credentials are configured
     */
    public boolean isSmtpConfigured() {
        return mailUsername != null && !mailUsername.isBlank()
                && mailPassword != null && !mailPassword.isBlank();
    }

    /**
     * Send email verification code
     * @return true if successfully delivered via SMTP, false otherwise
     */
    public boolean sendVerificationEmail(String toEmail, String username, String code) {
        String subject = "Verify Your KLMedia Account - Verification Code: " + code;
        String content = buildHtmlTemplate(
                "Welcome to KLMedia! 🌊",
                "Hi <strong>" + escapeHtml(username) + "</strong>,",
                "Thank you for registering with KLMedia. To complete your registration and activate your account, please enter the 6-digit verification code below:",
                code,
                "5 minutes",
                "If you did not request this account registration, please disregard this email. Never share this code with anyone."
        );

        return sendHtmlEmail(toEmail, subject, content, code, "Account Verification");
    }

    /**
     * Send password reset verification code
     * @return true if successfully delivered via SMTP, false otherwise
     */
    public boolean sendPasswordResetEmail(String toEmail, String username, String code) {
        String subject = "Reset Your KLMedia Password - Security Code: " + code;
        String content = buildHtmlTemplate(
                "Password Reset Request 🔐",
                "Hello" + (username != null && !username.isBlank() ? " <strong>" + escapeHtml(username) + "</strong>" : "") + ",",
                "We received a request to reset your KLMedia account password. Use the 6-digit verification code below to proceed:",
                code,
                "5 minutes",
                "If you did not request a password reset, please ignore this email or review your account security immediately. Never share this code with anyone."
        );

        return sendHtmlEmail(toEmail, subject, content, code, "Password Reset");
    }

    private boolean sendHtmlEmail(String toEmail, String subject, String htmlBody, String code, String purpose) {
        if (!isSmtpConfigured()) {
            log.warn("===============================================================================");
            log.warn("⚠️ SMTP NOT CONFIGURED: Missing spring.mail.username or spring.mail.password.");
            log.warn("Target Recipient: {}", toEmail);
            log.warn("Purpose: {}", purpose);
            log.warn("👉 VERIFICATION CODE IS: [{}]", code);
            log.warn("To receive emails in your Gmail inbox, configure your Gmail App Password in:");
            log.warn("  backend/src/main/resources/application.properties");
            log.warn("  spring.mail.username=your-email@gmail.com");
            log.warn("  spring.mail.password=your-16-char-app-password");
            log.warn("===============================================================================");
            return false;
        }

        String effectiveSender = mailUsername.trim();
        log.info("Dispatching [{}] email via SMTP to: {} from: {}", purpose, toEmail, effectiveSender);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(effectiveSender, senderName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("✅ Successfully delivered [{}] email via SMTP to: {}", purpose, toEmail);
            return true;
        } catch (Exception e) {
            log.error("❌ SMTP delivery failed to {}: {}", toEmail, e.getMessage());
            log.warn("Fallback - Verification code for {}: [{}]", toEmail, code);
            return false;
        }
    }

    private String buildHtmlTemplate(
            String title,
            String greeting,
            String message,
            String code,
            String expirationText,
            String securityNotice
    ) {
        return "<!DOCTYPE html>"
                + "<html>"
                + "<head>"
                + "<meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<title>" + title + "</title>"
                + "</head>"
                + "<body style=\"margin:0;padding:0;background-color:#080d16;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,Helvetica,Arial,sans-serif;color:#e8f0fb;\">"
                + "<table role=\"presentation\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" style=\"background-color:#080d16;padding:40px 10px;\">"
                + "<tr>"
                + "<td align=\"center\">"
                + "<table role=\"presentation\" width=\"100%\" max-width=\"540\" style=\"max-width:540px;background:#101827;border-radius:18px;border:1px solid rgba(8,184,237,0.25);overflow:hidden;box-shadow:0 20px 60px rgba(0,0,0,0.5);\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\">"
                + "<tr>"
                + "<td style=\"background:linear-gradient(135deg,#0a192f,#0d223f);padding:32px 30px;text-align:center;border-bottom:1px solid rgba(8,184,237,0.2);\">"
                + "<div style=\"font-size:32px;font-weight:700;color:#08b8ed;letter-spacing:1px;\">"
                + "<span style=\"font-size:36px;color:#08b8ed;vertical-align:middle;margin-right:6px;\">◎</span>KLMedia"
                + "</div>"
                + "<div style=\"font-size:13px;color:#91a0b5;margin-top:6px;letter-spacing:0.5px;\">Connect &bull; Share &bull; Inspire</div>"
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td style=\"padding:36px 32px;\">"
                + "<h2 style=\"margin:0 0 16px;font-size:22px;color:#e8f0fb;font-weight:600;\">" + title + "</h2>"
                + "<p style=\"font-size:15px;line-height:1.6;color:#cbd6e5;margin:0 0 16px;\">" + greeting + "</p>"
                + "<p style=\"font-size:15px;line-height:1.6;color:#cbd6e5;margin:0 0 24px;\">" + message + "</p>"
                + "<div style=\"background:#080d16;border:2px dashed rgba(8,184,237,0.45);border-radius:12px;padding:24px 16px;text-align:center;margin:28px 0;\">"
                + "<div style=\"font-size:12px;text-transform:uppercase;color:#91a0b5;letter-spacing:2px;margin-bottom:8px;\">Verification Code</div>"
                + "<div style=\"font-size:38px;font-weight:800;letter-spacing:10px;color:#08b8ed;font-family:Consolas,Monaco,monospace;\">" + code + "</div>"
                + "<div style=\"font-size:13px;color:#91a0b5;margin-top:10px;\">⏱ Expires in <strong style=\"color:#cbd6e5;\">" + expirationText + "</strong></div>"
                + "</div>"
                + "<div style=\"background:rgba(8,184,237,0.06);border-left:4px solid #08b8ed;padding:14px 16px;border-radius:6px;margin:24px 0 10px;\">"
                + "<p style=\"margin:0;font-size:13px;line-height:1.5;color:#91a0b5;\">" + securityNotice + "</p>"
                + "</div>"
                + "</td>"
                + "</tr>"
                + "<tr>"
                + "<td style=\"background:#0b1422;padding:20px 30px;text-align:center;border-top:1px solid rgba(148,163,184,0.12);\">"
                + "<p style=\"margin:0;font-size:12px;color:#718198;\">A little space for your everyday moments ✨</p>"
                + "<p style=\"margin:6px 0 0;font-size:11px;color:#546478;\">&copy; " + java.time.Year.now().getValue() + " KLMedia. All rights reserved.</p>"
                + "</td>"
                + "</tr>"
                + "</table>"
                + "</td>"
                + "</tr>"
                + "</table>"
                + "</body>"
                + "</html>";
    }

    private String escapeHtml(String input) {
        if (input == null) return "";
        return input.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
