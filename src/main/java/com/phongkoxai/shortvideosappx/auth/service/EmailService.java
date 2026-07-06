package com.phongkoxai.shortvideosappx.auth.service;

import com.phongkoxai.shortvideosappx.auth.entity.User;
import com.phongkoxai.shortvideosappx.common.exception.AppException;
import com.phongkoxai.shortvideosappx.common.exception.ErrorCode;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailService {
    JavaMailSender mailSender;
    @Async("emailTaskExecutor")
    public void sendEmailVerify(User recepient, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();

            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("thieuphongfo4@gmail.com", "Phong");
            helper.setTo(recepient.getEmail());
            helper.setSubject("Email Verification");
            String content = "<!DOCTYPE html>" +
                    "<html>" +
                    "<head>" +
                    "<meta charset='UTF-8'>" +
                    "<style>" +
                    "  body { font-family: 'Segoe UI', sans-serif; background-color: #f4f4f4; padding: 30px; }" +
                    "  .container { max-width: 600px; background-color: #ffffff; padding: 30px; border-radius: 10px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); margin: auto; }" +
                    "  h2 { color: #2c3e50; }" +
                    "  .code-box { background-color: #eafaf1; border-left: 6px solid #2ecc71; padding: 15px; font-size: 24px; letter-spacing: 3px; font-weight: bold; color: #27ae60; text-align: center; margin: 20px 0; }" +
                    "  p { color: #333333; font-size: 16px; line-height: 1.6; }" +
                    "  .footer { font-size: 13px; color: #999999; margin-top: 30px; text-align: center; }" +
                    "</style>" +
                    "</head>" +
                    "<body>" +
                    "<div class='container'>" +
                    "<h2>🔐 Verify email</h2>" +
                    "<p>Hello <strong>" + recepient.getUsername() + "</strong>,</p>" +
                    "<p>Cảm ơn bạn đã đăng ký tài khoản tại <strong>VietChefs</strong>.</p>" +
                    "<p>Để hoàn tất quá trình đăng ký, vui lòng sử dụng mã xác thực sau:</p>" +
                    "<div class='code-box'>" + otp + "</div>" +
                    "<p>Mã này sẽ hết hạn sau <strong>10 phút</strong>. Vui lòng không chia sẻ mã này với bất kỳ ai.</p>" +
                    "<p>Nếu bạn không yêu cầu đăng ký, hãy bỏ qua email này.</p>" +
                    "</div>" +
                    "</body>" +
                    "</html>";

            helper.setText(content, true);
            mailSender.send(message);

        } catch (Exception e) {
            log.error("Cannot send verification email to {}", recepient.getEmail(), e);
        }

    }

}
