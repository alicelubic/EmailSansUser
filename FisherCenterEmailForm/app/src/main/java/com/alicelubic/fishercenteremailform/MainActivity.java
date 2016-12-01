package com.alicelubic.fishercenteremailform;

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
    String mUserEmail, mEmailPattern;
    @BindString(R.string.dev_email) String mSenderEmail;
    @BindString(R.string.dev_email_past) String mSenderPass;
    @BindString(R.string.edit_text_hint) String mHint;
    @BindString(R.string.edit_text_format_error) String mFormatError;
    @BindString(R.string.subject_text) String mSubject;
    @BindString(R.string.body_text) String mBody;
    @BindString(R.string.email_button_text) String mButtonText;
    @BindView(R.id.user_input_et) EditText mInputEt;
    @BindView(R.id.send_email_button) Button mSend;
    @BindView(R.id.progress_bar) ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mEmailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

        mInputEt.setHint(mHint);
        mSend.setText(mButtonText);
        mSend.setOnClickListener(MainActivity.this);


    }

    public boolean isValidInput() {
        mUserEmail = mInputEt.getText().toString().trim();

        String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(emailPattern);
        Matcher matcher = pattern.matcher(mUserEmail);
        return matcher.matches();
//    return !TextUtils.isEmpty(mUserEmail) &&
//            Patterns.EMAIL_ADDRESS.matcher(mUserEmail).matches();
    }

    @Override
    public void onClick(View v) {
        if (isValidInput()) {
            new SendEmailTask(v).execute();
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            Log.d(TAG, "onClick: mUserEmail is not valid : "+mUserEmail);
            mInputEt.setError(mFormatError);
        }

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
                return true;
            } catch (Exception e) {
                Log.e("SendEmail", e.getMessage(), e);
                return false;
            }
        }


        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

            for (int i = 0; i < values.length; i++) {
                mProgressBar.setProgress(i);
            }

        }

        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);

            mProgressBar.setVisibility(View.GONE);
            mInputEt.setText("");

            if (!bool) {
                Snackbar.make(mView, R.string.task_failed_message, Snackbar.LENGTH_LONG).setAction(R.string.task_failed_action, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new SendEmailTask(mView).execute();
                    }
                }).show();

            } else {
                Snackbar.make(mView, R.string.task_success_message, Snackbar.LENGTH_SHORT)
                        .show();
            }

        }
    }

}
