package com.alicelubic.fishercenteremailform;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    String mUserEmail;
    ProgressDialog mDialog;
    @BindString(R.string.dev_email)
    String mSenderEmail;
    @BindString(R.string.dev_email_past)
    String mSenderPass;
    @BindString(R.string.edit_text_format_error)
    String mFormatError;
    @BindString(R.string.subject_text)
    String mSubject;
    @BindString(R.string.body_text)
    String mBody;
    @BindView(R.id.user_input_et)
    EditText mInputEt;
    @BindView(R.id.send_email_button)
    Button mSend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        mSend.setOnClickListener(MainActivity.this);
    }


    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.send_email_button){

            //check for valid input
            if (isValidInput()) {
                new SendEmailTask(v).execute();
                mInputEt.setText("");
                showProgressDialog();
            } else {
                //otherwise show an error
                Log.d(TAG, "onClick: mUserEmail is not valid : " + mUserEmail);
                mInputEt.setError(mFormatError);
                //TODO account for errors other than just formatting
            }
        }//else if it's the send sms button instead


    }


    public class SendEmailTask extends AsyncTask<Void, Integer, Boolean> {
        View mView;

        SendEmailTask(View view) {
            mView = view;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                GMailSender sender = new GMailSender(mSenderEmail,
                        mSenderPass);
                sender.sendMail(mSubject,
                        mBody,
                        mSenderEmail,
                        mUserEmail);
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
            if (!bool) {
                //let the user know it didn't work, action will try again
                Snackbar.make(mView, R.string.task_failed_message, Snackbar.LENGTH_LONG)
                        .setAction(R.string.task_failed_action, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new SendEmailTask(mView).execute();
                    }
                }).show();

            } else {
                //let user know that it did work
                mDialog.dismiss();
                Snackbar.make(mView, R.string.task_success_message, Snackbar.LENGTH_SHORT)
                        .show();

            }

        }
    }


    public void showProgressDialog() {
        mDialog = new ProgressDialog(MainActivity.this);
        mDialog.setMessage(getString(R.string.progress_dialog_message));
        mDialog.setIndeterminate(true);
        mDialog.show();
    }
    public boolean isValidInput() {
        mUserEmail = mInputEt.getText().toString().trim();

        String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(mUserEmail);
        return matcher.matches();

    }
}
