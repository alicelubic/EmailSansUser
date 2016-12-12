package com.alicelubic.fishercenteremailform;

/**
 * Created by owlslubic on 12/1/16.
 */


import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

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
    private String mGmailUser;
    private String mPassword;
    private Session mSession;
    private Context mContext;

    static {
        Security.addProvider(new JSSEProvider());
    }

    public GMailSender(Context context) {
        mContext = context;
        mGmailUser = context.getResources().getString(R.string.dev_email);
        mPassword = context.getResources().getString(R.string.dev_email_pass);

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
        return new PasswordAuthentication(mGmailUser, mPassword);
    }

    public synchronized void sendMail(String subject, String body, String sender, String recipients) throws Exception {
        try {
            MimeMessage message = new MimeMessage(mSession);
            DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));
            message.setSender(new InternetAddress(sender));
            message.setSubject(subject);
            message.setDataHandler(handler);
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
            Transport.send(message);

        } catch (Exception e) {
            Log.e(TAG, "sendMail: ", e);
            Log.e(TAG, "sendMail: " + Arrays.toString(e.getStackTrace()));

        }
    }

    public void executeSendEmailTask(String userEmail, String body, View view, ProgressDialog progressDialog) {
        new SendEmailTask(userEmail, body, view, progressDialog).execute();
    }


    public class SendEmailTask extends AsyncTask<Void, Void, Boolean> {
        //making the task not take in a string, which originally was for the body of the email
        //instead i'm doing it all thru my own custom constructor to keep it neater. not sure if that
        //affects the function at all... we'll see!

        View mView;
        String mBody;
        String mUserEmail;
        ProgressDialog mProgressDialog;


        public SendEmailTask(String userEmail, String body, View view, ProgressDialog progressDialog) {
            mView = view;
            mBody = body;
            mUserEmail = userEmail;
            mProgressDialog = progressDialog;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                sendMail(mContext.getString(R.string.subject_text),
                        mBody,
                        mGmailUser,
                        mUserEmail);
                Log.i(TAG, "doInBackground: sendMail was called for " + mUserEmail);

                //TODO make sure it doesn't always return true (which is whats happening currently)

                //TODO figure out how to make sure the email actually sent. some sort of completion listener...?
                return true;
            } catch (Exception e) {
                Log.e("SendEmail", e.getMessage(), e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);

            mProgressDialog.dismiss();

            if (!bool) {
                //let the user know it didn't work, action will try again
                Snackbar.make(mView, R.string.task_failed_message, Snackbar.LENGTH_LONG)
                        .setAction(R.string.task_failed_action, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new SendEmailTask(mUserEmail, mBody, mView,
                                        mProgressDialog).execute();
                            }
                        }).show();

            } else {
                //let user know that it did work
                String message = String.format(mContext.getResources().getString(R.string.task_success_message), "to " + mUserEmail);
                Snackbar.make(mView, message, Snackbar.LENGTH_SHORT)
                        .show();

            }

        }
    }


}