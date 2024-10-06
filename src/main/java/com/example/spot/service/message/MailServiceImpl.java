package com.example.spot.service.message;

import com.example.spot.api.code.status.ErrorStatus;
import com.example.spot.api.exception.handler.MemberHandler;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String sender;
    private static final String subject = "[SPOT] 이메일 확인 코드 : ";
    private static final String senderName = "SPOT";

    @Override
    public void sendMail(HttpServletRequest request, HttpServletResponse response, String email, String code) {
        MimeMessage message = createMessage(email, code);
        javaMailSender.send(message);
    }

    private MimeMessage createMessage(String email, String verificationCode) {

        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(sender, senderName);
            helper.setTo(email);
            helper.setSubject(subject + verificationCode);

            String htmlContent = getHtmlContent();

            htmlContent = htmlContent.replace("${email}", email);
            htmlContent = htmlContent.replace("${verificationCode}", verificationCode);
            htmlContent = htmlContent.replace("${sender}", sender);

            helper.setText(htmlContent, true);

        } catch (MessagingException e) {
            throw new MemberHandler(ErrorStatus._UNABLE_TO_SEND_EMAIL);
        } catch (IOException e) {
            throw new MemberHandler(ErrorStatus._UNABLE_TO_LOAD_MAIL_FORM);
        } catch (Exception e) {
            throw new MemberHandler(ErrorStatus._INTERNAL_SERVER_ERROR);
        }

        return message;
    }

    private String getHtmlContent() throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/MailForm.html");
        byte[] encoded = Files.readAllBytes(Paths.get(resource.getURI()));
        return new String(encoded, StandardCharsets.UTF_8);
    }

}
