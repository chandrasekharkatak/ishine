package com.apmosys.employeeportal.service;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MailService {

	@Value("${mail.sender}")
	private String sender;

	@Value("${mail.password}")
	private String pwd;

	@Value("${mail.port}")
	private String port;

	@Value("${mail.host}")
	private String host;	

	public boolean sendMail(String receiver, String subject, String text) throws AddressException, MessagingException {

		try {

			Properties props = new Properties();

			props.put("mail.smtp.user", sender);
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.port", port);
			props.put("mail.smtp.starttls.enable", "false");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.socketFactory.port", port);
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.socketFactory.fallback", "false");
			props.put("mail.smtp.ssl.protocols", "TLSv1.2");

			props.remove("mail.smtp.socketFactory.class");
			props.setProperty("mail.smtp.starttls.enable", "true");
			SecurityManager security = System.getSecurityManager();
			System.out.println("Before Authentication");
			Authenticator auth = new Authenticator() {

				public PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(sender, pwd);
				}
			};
			System.out.println("After Authentication " + auth);
			javax.mail.Session session = javax.mail.Session.getInstance(props, auth);

			MimeMessage msg = new MimeMessage(session);

			msg.setSubject(subject);
			msg.setContent(text, "text/html");
			msg.setFrom(new InternetAddress(sender));

			msg.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(receiver));

			javax.mail.Transport.send(msg);

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;

		}
	}

	public static void main(String[] args) {
		MailService mailTest = new MailService();
		try {
			boolean flag = mailTest.sendMail("suraj.honavar@apmosys.com", "Test Mail", "This is a test mail!");
			if (flag) {
				System.out.println("mail sent!");
			} else {
				System.out.println("Failed!");
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
}
