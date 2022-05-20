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

//	@Value("${spring.mail.username}")
//	private String senderName;
//
//	@Value("${spring.mail.password}")
//	private String pwd;
//
//	@Value("${spring.mail.port}")
//	private String port;
//
//	@Value("${spring.mail.host}")
//	private String host;
//
//	@Value("${spring.mail.sender}")
//	private String sender;
	
	private String senderName = "@gmail.com";
	private String pwd = "";
	private String port = "587";
	private String host = "smtp.gmail.com";
	private String sender = "@gmail.com";

	public boolean sendMail(String receiver, String subject, String text) throws AddressException, MessagingException {

		try {

			Properties props = new Properties();

			props.put("mail.smtp.user", senderName);
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
			// msg.setText(text,"text/html");
			msg.setContent(text, "text/html");
			msg.setFrom(new InternetAddress(senderName));

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
			boolean flag = mailTest.sendMail("prasad.more@apmosys.com", "Test Mail", "This is a test mail!");
			if(flag) {
				System.out.println("mail sent!");
			}else {
				System.out.println("Failed!");
			}
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}
}
