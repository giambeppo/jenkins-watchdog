package it.dellarciprete.watchdog.common;

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

import it.dellarciprete.watchdog.Alerter;
import it.dellarciprete.watchdog.utils.Configuration;
import it.dellarciprete.watchdog.utils.WatchDogException;
import it.dellarciprete.watchdog.utils.WatchDogLogger;

/**
 * Sends alerts as emails.
 */
public class MailClient implements Alerter {

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

  public MailClient(Configuration config, String pendingEmailFileName, String mailSubject, String mailBodyFormat)
      throws WatchDogException {
    smtpHost = config.get("mail.smtps.host");
    smtpPort = config.get("mail.smtps.port", "25");
    username = config.get("mail.username");
    password = config.get("mail.password");
    sender = config.get("mail.sender");
    recipients = config.get("mail.recipients");
    subject = mailSubject;
    bodyFormat = mailBodyFormat;
    pendingEmail = new File(pendingEmailFileName);
  }

  @Override
  public void sendAlert(Object... params) throws WatchDogException {
    LOGGER.log(Level.INFO, "Sending alert email to " + recipients);
    resetPendingAlert();
    String url = (String) params[0];
    try {
      sendEmail(url);
    } catch (MessagingException e) {
      LOGGER.log(Level.WARNING, "Unable to send the alert email, saving it in pending status");
      try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(pendingEmail))) {
        oos.writeUTF(url);
      } catch (IOException ex) {
        LOGGER.log(Level.SEVERE, "Unable to store the alert email for later re-sending, it will be lost");
      }
      throw new WatchDogException(e);
    }
  }

  @Override
  public void sendAlertIfPending() throws WatchDogException {
    if (pendingEmail.exists()) {
      LOGGER.log(Level.INFO, "Trying again to send the pending email");
      try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(pendingEmail))) {
        String url = ois.readUTF();
        sendEmail(url);
      } catch (IOException | MessagingException e) {
        throw new WatchDogException("Error while trying to re-send a pending alert", e);
      }
      try {
        Files.delete(pendingEmail.toPath());
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "Error deleting the pending alert email after sending it; it might get sent twice");
      }
    }
  }

  @Override
  public void resetPendingAlert() {
    if (pendingEmail.exists()) {
      LOGGER.log(Level.INFO, "Removing the pending email");
      try {
        Files.delete(pendingEmail.toPath());
      } catch (IOException e) {
        LOGGER.log(Level.SEVERE, "Error deleting the pending alert email");
      }
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
