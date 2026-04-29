package live.qsmc.api.account;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class VerificationEmailService {

    private final JavaMailSender mailSender;
    private final String baseUrl;
    private final String fromAddress;

    public VerificationEmailService(JavaMailSender mailSender, @Value("${app.base-url}") String baseUrl, @Value("${app.mail.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.baseUrl = baseUrl;
        this.fromAddress = fromAddress;
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String verifyUrl = baseUrl + "/account/verify?token=" + token + "&email=" + toEmail;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(toEmail);
        message.setSubject("Verify your Quipt account");
        message.setText("Welcome to Quipt!\n\nPlease verify your account using this token:\n" + token + "\n\nOr open this link:\n" + verifyUrl);

        mailSender.send(message);
    }
}

