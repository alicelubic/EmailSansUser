package com.alicelubic.fishercenteremailform.email_reqs;

/**
 * Created by owlslubic on 12/1/16.
 */


import android.util.Log;

import javax.activation.DataHandler;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.security.Security;
import java.util.Arrays;
import java.util.Properties;

/**
 * create email sender object that will hold the logic needed to send an email.
 * using gmail as the smtp server
 */

public class GMailSender extends javax.mail.Authenticator {
    private static final String TAG = "tag";
    private String mMailhost = "smtp.gmail.com";
    private String mUser;
    private String mPassword;
    private Session mSession;

    static {
        Security.addProvider(new JSSEProvider());
    }

    public GMailSender(String user, String password) {
        mUser = user;
        mPassword = password;

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        props.setProperty("mail.host", mMailhost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.port", "465");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        mSession = Session.getDefaultInstance(props, this);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(mUser, mPassword);
    }

    public synchronized void sendMail(String subject, String body, String sender, String recipients) throws Exception {
        try{
            MimeMessage message = new MimeMessage(mSession);
            DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));
            message.setSender(new InternetAddress(sender));
            message.setSubject(subject);
            message.setDataHandler(handler);
            if (recipients.indexOf(',') > 0)
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            else
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
            Transport.send(message);
        }catch(Exception e){
            Log.e(TAG, "sendMail: ",e);
            Log.e(TAG, "sendMail: "+ Arrays.toString(e.getStackTrace()));

        }
    }


}