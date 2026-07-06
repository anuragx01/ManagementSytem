package com.nexstar.portal.authentication.service;

import com.nexstar.portal.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    @Async
    public void sendEmailVerification(String to, String name, String token) {
        String subject = "Verify Your Email - Nexstar Portal";
        String body = buildEmailVerificationBody(name, token);
        sendEmail(to, subject, body);
    }

    @Async
    public void sendPasswordResetEmail(String to, String name, String token) {
        String subject = "Reset Your Password - Nexstar Portal";
        String body = buildPasswordResetBody(name, token);
        sendEmail(to, subject, body);
    }

    @Async
    public void sendWelcomeEmail(String to, String name) {
        String subject = "Welcome to Nexstar Portal";
        String body = buildWelcomeBody(name);
        sendEmail(to, subject, body);
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(appProperties.getMail().getFrom(), appProperties.getMail().getFromName());
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            mailSender.send(message);
            log.debug("Email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildEmailVerificationBody(String name, String token) {
        return """
                <html><body>
                <h2>Hello, %s!</h2>
                <p>Please verify your email address by clicking the link below:</p>
                <a href="%s/auth/verify-email?token=%s" style="background:#3B82F6;color:#fff;padding:12px 24px;text-decoration:none;border-radius:6px;">Verify Email</a>
                <p>This link expires in 24 hours.</p>
                <p>If you did not create an account, please ignore this email.</p>
                </body></html>
                """.formatted(name, "http://localhost:3000", token);
    }

    private String buildPasswordResetBody(String name, String token) {
        return """
                <html><body>
                <h2>Hello, %s!</h2>
                <p>You requested to reset your password. Click the link below:</p>
                <a href="%s/auth/reset-password?token=%s" style="background:#EF4444;color:#fff;padding:12px 24px;text-decoration:none;border-radius:6px;">Reset Password</a>
                <p>This link expires in 30 minutes.</p>
                <p>If you did not request a password reset, please ignore this email.</p>
                </body></html>
                """.formatted(name, "http://localhost:3000", token);
    }

    private String buildWelcomeBody(String name) {
        return """
                <html><body>
                <h2>Welcome, %s!</h2>
                <p>Your account has been successfully created on Nexstar Portal.</p>
                <p>You can now log in and start managing your work.</p>
                <a href="%s/login" style="background:#10B981;color:#fff;padding:12px 24px;text-decoration:none;border-radius:6px;">Go to Portal</a>
                </body></html>
                """.formatted(name, "http://localhost:3000");
    }
}
