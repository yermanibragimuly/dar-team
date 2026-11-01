package com.example.demo.service.Email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("resetalldar@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Сброс пароля — DAR TEAM");
        message.setText("Здравствуйте!\n\nВаш код для подтверждения сброса пароля: " + code +
                "\nКод действителен в течение 15 минут.\n\nЕсли вы не запрашивали сброс, просто игнорируйте это письмо.");

        mailSender.send(message);
    }

    public void sendRegistrationConfirmLink(String toEmail, String token) {
        String confirmLink = "https://dar-team.kz/confirm-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("resetalldar@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Подтверждение регистрации — DAR TEAM");
        message.setText("Добро пожаловать в DAR TEAM!\n\n" +
                "Чтобы завершить регистрацию, подтвердите ваш email по ссылке:\n" + confirmLink +
                "\n\nПосле подтверждения вы сможете войти в систему.");

        mailSender.send(message);
    }

    // УВЕДОМЛЕНИЕ О ТОМ, ЧТО ЗАКАЗ СОБРАН И ЖДЕТ В ЗАЛЕ
    public void sendOrderReadyEmail(String toEmail, String customerName, String orderNumber, String pickupCode, String branchName, String branchAddress) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("resetalldar@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Заказ " + orderNumber + " готов к выдаче! — DAR TEAM");
        message.setText("Здравствуйте, " + customerName + "!\n\n" +
                "Отличные новости: ваш заказ " + orderNumber + " успешно собран и ждёт вас на ресепшн клуба!\n\n" +
                "📍 Пункт самовывоза: " + branchName + " (" + branchAddress + ")\n" +
                "🔑 Пин-код для получения: " + pickupCode + "\n\n" +
                "Подойдите к администратору, назовите номер заказа или покажите пин-код. " +
                "Вы сможете спокойно примерить вещь перед получением.\n\n" +
                "С уважением,\nКоманда DAR TEAM");

        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Ошибка отправки письма о готовности заказа: " + e.getMessage());
        }
    }
}