package com.apmosys.employeeportal.service;

import java.io.File;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

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
	
	@Value("${file.location.image}")
	private String imageFilepath;
	
	public Session mailProperties() {
		javax.mail.Session session = null;
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
			session = javax.mail.Session.getInstance(props, auth);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
		return session;
	}

	public boolean sendMail(String receiver, String subject, String text) throws AddressException, MessagingException {

		try {

			Session session = mailProperties();

			MimeMessage msg = new MimeMessage(session);

			msg.setSubject(subject);
			msg.setContent(text, "text/html");
			msg.setFrom(new InternetAddress(sender));

			msg.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(receiver, true));

			javax.mail.Transport.send(msg);

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;

		}
	}
	
	public boolean sendMailWithCC(String receiver,String cc, String subject, String text) throws AddressException, MessagingException {

		try {

			Session session = mailProperties();

			MimeMessage msg = new MimeMessage(session);

			msg.setSubject(subject);
			msg.setContent(text, "text/html");
			msg.setFrom(new InternetAddress(sender));

			msg.setRecipients(javax.mail.Message.RecipientType.TO, InternetAddress.parse(receiver, true));
			msg.setRecipients(javax.mail.Message.RecipientType.CC, InternetAddress.parse(cc, true));

			javax.mail.Transport.send(msg);

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;

		}
	}
	
	public boolean sendMailWithImage(String receiver, String cc, String subject, String htmlBody ,String imageFileName)
			throws AddressException, MessagingException {

		try {
			Session session = mailProperties();

			Message msg = new MimeMessage(session);

			msg.setSubject(subject);
			msg.setFrom(new InternetAddress(sender));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));
			msg.setRecipients(javax.mail.Message.RecipientType.CC, InternetAddress.parse(cc, true));

			// creates message part
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(htmlBody, "text/html");
			

			// creates multi-part
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			
			// adds inline image attachments
			MimeBodyPart imagePart = new MimeBodyPart();
			imagePart.setHeader("Content-ID", "image");
			imagePart.setDisposition(MimeBodyPart.INLINE);
			// attach the image file
			imagePart.attachFile(imageFilepath+  File.separator +imageFileName);
			
			multipart.addBodyPart(imagePart);

			msg.setContent(multipart);

			javax.mail.Transport.send(msg);

			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;

		}
	}
	
	public boolean sendMailWithAttachment(String receiver, String cc, String subject, String htmlBody ,File attachment)
			throws AddressException, MessagingException {

		try {
			Session session = mailProperties();

			Message msg = new MimeMessage(session);

			msg.setSubject(subject);
			msg.setFrom(new InternetAddress(sender));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));
			msg.setRecipients(javax.mail.Message.RecipientType.CC, InternetAddress.parse(cc, true));

			// creates message part
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(htmlBody, "text/html");
			

			// creates multi-part
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			
			// add attachment
			MimeBodyPart attachmentBodypart = new MimeBodyPart();
			attachmentBodypart.attachFile(attachment);

			multipart.addBodyPart(attachmentBodypart);
			msg.setContent(multipart);

			javax.mail.Transport.send(msg);
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;

		}
	}
	
//	added by anurag without cc
	
	public boolean sendMailWithoutAttachment(String receiver, String subject, String htmlBody ,File attachment)
			throws AddressException, MessagingException {

		try {
			Session session = mailProperties();

			Message msg = new MimeMessage(session);

			msg.setSubject(subject);
			msg.setFrom(new InternetAddress(sender));
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));
			

			// creates message part
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(htmlBody, "text/html");
			

			// creates multi-part
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			
			// add attachment
			MimeBodyPart attachmentBodypart = new MimeBodyPart();
			attachmentBodypart.attachFile(attachment);

			multipart.addBodyPart(attachmentBodypart);
			msg.setContent(multipart);

			javax.mail.Transport.send(msg);
			return true;

		} catch (Exception e) {
			e.printStackTrace();
			return false;

		}
	}
	
	public boolean sendMailWithoutAttachmentWithMailBody(String receiver, String subject, String htmlBody ,File attachment, String cc)
			throws AddressException, MessagingException {

		try {
			Session session = mailProperties();

			Message msg = new MimeMessage(session);

			msg.setSubject(subject);
			msg.setFrom(new InternetAddress(sender));
//			msg.setContent(text, "text/html");
			msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(receiver));
			
			if (cc != null && !cc.isEmpty()) {
	            msg.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
	        }


			// creates message part
			MimeBodyPart messageBodyPart = new MimeBodyPart();
			messageBodyPart.setContent(htmlBody, "text/html");
			

			// creates multi-part
			Multipart multipart = new MimeMultipart();
			multipart.addBodyPart(messageBodyPart);
			
			// add attachment
			MimeBodyPart attachmentBodypart = new MimeBodyPart();
			attachmentBodypart.attachFile(attachment);

			multipart.addBodyPart(attachmentBodypart);
			msg.setContent(multipart);

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
			boolean flag = mailTest.sendMail("harshit.toxia@apmosys.com", "Test Mail", "This is a test mail!");
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
