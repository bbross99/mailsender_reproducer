package com.ejbException;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.FileDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import java.util.Properties;

public class MailSender {

    private final String smtpHost;
    private final String username;
    private final String password;
    private final boolean useTls;

    public MailSender(String smtpHost, String username, String password, boolean useTls) {
        this.smtpHost = smtpHost;
        this.username = username;
        this.password = password;
        this.useTls = useTls;
    }

    public void sendEmailWithAttachment(String to, String subject, String body, String attachmentPath)
            throws MessagingException {

        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(2587));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");


        // Timeout settings (10 seconds each)
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");


        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
        session.setDebug(true);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom("bbross@tomitribe.com");
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
            message.setSubject(subject);

            // Create message body
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);

            // Add attachment if specified
            if (attachmentPath != null && !attachmentPath.isEmpty()) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                DataSource source = new FileDataSource(attachmentPath);
                attachmentPart.setDataHandler(new DataHandler(source));
                attachmentPart.setFileName(source.getName());
                multipart.addBodyPart(attachmentPart);
            }

            message.setContent(multipart);

            long startTime = System.nanoTime();
            Transport.send(message);
            long endTime = System.nanoTime();
            double durationSeconds = (endTime - startTime) / 1_000_000_000.0;
            System.out.printf("Email sending took %.3f seconds%n", durationSeconds);

            System.out.println("Email sent successfully");

        } catch (MessagingException e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw e;
        }
    }

    public static void main(String[] args) {

        String smtpHost = ""; // your host name
        String username = ""; // your username
        String password = ""; // your password
        boolean useTls = true;

        MailSender mailSender = new MailSender(
                smtpHost, username, password, useTls);

        try {
            mailSender.sendEmailWithAttachment(
                    "bbross@tomitribe.com",
                    "Test Email with Attachment",
                    "This is a test email sent with an attachment",
                    "src/main/resources/images/doe.jpg"
            );
        } catch (MessagingException e) {
            System.err.println("Email sending failed:");
            e.printStackTrace();
        }
    }
}