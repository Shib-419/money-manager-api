package com.shiv.MoneyManager.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.properties.mail.smtp.from}")
    private String fromEmail;

    public void sendEmail(String to , String subject , String body){
        try{
            System.out.println("=== EMAIL DEBUG INFO ===");
            System.out.println("From: " + fromEmail);
            System.out.println("To: " + to);
            System.out.println("Subject: " + subject);
            System.out.println("Attempting to send email...");
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Failed to send email: "+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

    }
}
