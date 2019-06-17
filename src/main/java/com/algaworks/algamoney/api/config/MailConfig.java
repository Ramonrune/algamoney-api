package com.algaworks.algamoney.api.config;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import com.algaworks.algamoney.api.config.property.AlgamoneyApiProperty;

@Configuration
public class MailConfig {

	@Autowired
	private AlgamoneyApiProperty property;
	
	@Bean
	public JavaMailSender javaMailSender() {
		Properties properties = new Properties();
		properties.put("mail.transport.protocol", "smtp");
		properties.put("mail.smtp.auth", true);
		properties.put("mail.smtp.starttls.enable", true);
		properties.put("mail.smtp.connectiontimeout", 10000);
		
		JavaMailSenderImpl mailSenderImpl = new JavaMailSenderImpl();
		mailSenderImpl.setJavaMailProperties(properties);
		mailSenderImpl.setHost(property.getMail().getHost());
		mailSenderImpl.setPort(property.getMail().getPort());
		mailSenderImpl.setUsername(property.getMail().getUsername());
		mailSenderImpl.setPassword(property.getMail().getPassword());

		return mailSenderImpl;
	}
}
