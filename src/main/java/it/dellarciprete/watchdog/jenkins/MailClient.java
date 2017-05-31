package it.dellarciprete.watchdog.jenkins;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.security.Security;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.sun.mail.smtp.SMTPTransport;

/**
 * Handles sending the alert email in case of a new build failure.
 */
public class MailClient {

  private static final Logger LOGGER = WatchDogLogger.get();

  private final String smtpHost;
  private final String smtpPort;
  private final String username;
  private final String password;
  private final String sender;
  private final String recipients;
  private final String subject;
  private final String bodyFormat;
  private final File pendingEmail;

  public MailClient(Configuration config) throws WatchDogException {
    smtpHost = config.get("mail.smtps.host");
    if (smtpHost == null) {
      throw new WatchDogException("mail.smtps.host is not configured");
    }
    smtpPort = config.get("mail.smtps.port", "25");
    username = config.get("mail.username");
    if (username == null) {
      throw new WatchDogException("mail.username is not configured");
    }
    password = config.get("mail.password");
    if (password == null) {
      throw new WatchDogException("mail.password is not configured");
    }
    sender = config.get("mail.sender");
    if (sender == null) {
      throw new WatchDogException("mail.sender is not configured");
    }
    recipients = config.get("mail.recipients");
    if (recipients == null) {
      throw new WatchDogException("mail.recipients is not configured");
    }
    subject = config.get("mail.subject", "Jenkins build failed");
    bodyFormat = config.get("mail.body", "A Jenkins build (%s) has failed, please take action!");
    pendingEmail = new File(config.get("mail.file.pending", "pendingEmail.ser"));
  }

  /**
   * Tries to send an alert email.
   * 
   * <p>If it fails, the mail is stored in pending status in order to be re-sent later.</p>
   * 
   * @param url the url of the failed build
   * @throws MessagingException
   * @throws IOException
   */
  public void sendAlert(String url) throws MessagingException, IOException {
    LOGGER.log(Level.INFO, "Sending alert email to " + recipients);
    resetPendingAlert();
    try {
      sendEmail(url);
    } catch (Exception e) {
      LOGGER.log(Level.WARNING, "Unable to send the alert email, saving it in pending status");
      try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(pendingEmail))) {
        oos.writeUTF(url);
      }
      throw e;
    }
  }

  /**
   * If there is a mail pending, tries to send it again. Otherwise, nothing happens.
   * 
   * @throws IOException
   * @throws MessagingException
   */
  public void sendAlertIfPending() throws IOException, MessagingException {
    if (pendingEmail.exists()) {
      LOGGER.log(Level.INFO, "Trying again to send the pending email");
      try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(pendingEmail))) {
        String url = ois.readUTF();
        sendEmail(url);
      }
      Files.delete(pendingEmail.toPath());
    }
  }

  /**
   * If there is a mail pending, it is deleted.
   * 
   * @throws IOException
   */
  public void resetPendingAlert() throws IOException {
    if (pendingEmail.exists()) {
      LOGGER.log(Level.INFO, "Removing the pending email");
      Files.delete(pendingEmail.toPath());
    }
  }

  @SuppressWarnings("restriction")
  private void sendEmail(String url) throws MessagingException {
    Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
    final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
    Properties props = System.getProperties();
    props.setProperty("mail.smtps.host", smtpHost);
    props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
    props.setProperty("mail.smtp.socketFactory.fallback", "false");
    props.setProperty("mail.smtp.port", smtpPort);
    props.setProperty("mail.smtp.socketFactory.port", "465");
    props.setProperty("mail.smtps.auth", "true");
    props.put("mail.smtps.quitwait", "false");
    Session session = Session.getInstance(props, null);
    final MimeMessage msg = new MimeMessage(session);
    msg.setFrom(new InternetAddress(sender));
    msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients, false));
    msg.setSubject(subject);
    msg.setText(String.format(bodyFormat, url), "utf-8");
    msg.setSentDate(new Date());
    SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
    t.connect(smtpHost, username, password);
    t.sendMessage(msg, msg.getAllRecipients());
    t.close();
  }

}
