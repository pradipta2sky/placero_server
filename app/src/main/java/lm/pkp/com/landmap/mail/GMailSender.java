package lm.pkp.com.landmap.mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Security;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class GMailSender extends Authenticator {

    private final String mailhost = "smtp.gmail.com";
    private final String user;
    private final String password;
    private final Session session;


    private final Multipart _multipart = new MimeMultipart();

    static {
        Security.addProvider(new JSSEProvider());
    }


    public GMailSender(String user, String password) {
        this.user = user;
        this.password = password;

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", this.mailhost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        this.session = Session.getDefaultInstance(props, this);
    }


    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(this.user, this.password);
    }


    public synchronized void sendMail(String subject, String body, String sender, String recipients) throws Exception {
        try {
            MimeMessage message = new MimeMessage(this.session);
            DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));
            message.setSender(new InternetAddress(sender));
            message.setSubject(subject);
            message.setDataHandler(handler);
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(body);
            this._multipart.addBodyPart(messageBodyPart);
            // Put parts in message
            message.setContent(this._multipart);
            if (recipients.indexOf(',') > 0) {
                message.setRecipients(RecipientType.TO, InternetAddress.parse(recipients));
            } else {
                message.setRecipient(RecipientType.TO, new InternetAddress(recipients));
            }
            Transport.send(message);
        } catch (Exception e) {
        }
    }


    public void addAttachment(String filename) throws Exception {
        BodyPart messageBodyPart = new MimeBodyPart();
        DataSource source = new FileDataSource(filename);
        messageBodyPart.setDataHandler(new DataHandler(source));
        messageBodyPart.setFileName("download image");
        this._multipart.addBodyPart(messageBodyPart);
    }


    public class ByteArrayDataSource implements DataSource {
        private final byte[] data;
        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            this.data = data;
            this.type = type;
        }

        public ByteArrayDataSource(byte[] data) {
            this.data = data;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getContentType() {
            if (this.type == null)
                return "application/octet-stream";
            else
                return this.type;
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(this.data);
        }


        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }
}