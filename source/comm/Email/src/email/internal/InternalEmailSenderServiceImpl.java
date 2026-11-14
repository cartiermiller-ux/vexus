package email.internal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import email.sender.EmailMessage;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import kernel.util.StringUtils;

public class InternalEmailSenderServiceImpl implements InternalEmailSenderService {
	private static final Logger logger = LoggerFactory.getLogger(InternalEmailSenderServiceImpl.class);
	private JavaMailSenderImpl mailSender;
	private SimpleMailMessage mailMessage;
	private FreeMarkerConfigurer freeMarkerConfigurer;

	@Override
	public void send(EmailMessage emailMessage) {
		try {

			MimeMessage mailMsg = this.mailSender.createMimeMessage();

			MimeMessageHelper messageHelper = new MimeMessageHelper(mailMsg, true, "UTF-8");
			messageHelper.setTo(emailMessage.getTomail());// 接收邮箱
			messageHelper.setFrom(this.mailMessage.getFrom());// 发送邮箱
			messageHelper.setSentDate(new Date());// 发送时间
			messageHelper.setSubject(emailMessage.getSubject());// 邮件标题

			if (StringUtils.isNullOrEmpty(emailMessage.getFtlname())) {
				messageHelper.setText(emailMessage.getContent());// 邮件内容
			} else {
				messageHelper.setText(this.getMailText(emailMessage.getFtlname(), emailMessage.getMap()), true);// 邮件内容
			}

			// true 表示启动HTML格式的邮件
			if (emailMessage.getFile() != null) {
				// 添加邮件附件
				FileSystemResource rarfile = new FileSystemResource(emailMessage.getFile());

				// addAttachment addInline 两种附件添加方式
				// 以附件的形式添加到邮件
				// 使用MimeUtility.encodeWord 解决附件名中文乱码的问题
				messageHelper.addAttachment(MimeUtility.encodeWord(emailMessage.getFilename()), rarfile);
			}

			this.mailSender.send(mailMsg);// 发送

		} catch (MessagingException e) {
			logger.error(e.getMessage(), e);
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
		}

	}

	/**
	 * 获取模板并将内容输出到模板
	 * 
	 * @param content
	 * @return
	 */
	private String getMailText(String ftlname, Map<String, Object> map) {
		String html = "";

		try {

			// 装载模板
			Template tpl = this.freeMarkerConfigurer.getConfiguration().getTemplate(ftlname);
			// 加入map到模板中 输出对应变量
			html = FreeMarkerTemplateUtils.processTemplateIntoString(tpl, map);

		} catch (IOException e) {
			e.printStackTrace();
		} catch (TemplateException e) {
			e.printStackTrace();
		}
		return html;
	}

	public void setMailSender(JavaMailSenderImpl mailSender) {
		this.mailSender = mailSender;
	}

	public void setMailMessage(SimpleMailMessage mailMessage) {
		this.mailMessage = mailMessage;
	}

	public void setFreeMarkerConfigurer(FreeMarkerConfigurer freeMarkerConfigurer) {
		this.freeMarkerConfigurer = freeMarkerConfigurer;
	}

}
