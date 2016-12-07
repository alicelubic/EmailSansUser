package com.alicelubic.fishercenteremailform;

import android.app.ProgressDialog;
import android.graphics.Typeface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "Main";
    String mUserInput;
    ProgressDialog mDialog;

    @BindString(R.string.dev_email)
    String mSenderEmail;
    @BindString(R.string.dev_email_pass)
    String mSenderPass;
    @BindString(R.string.edit_text_format_error)
    String mFormatError;
    @BindString(R.string.subject_text)
    String mSubject;
    @BindView(R.id.user_input_et)
    EditText mInputEt;
    @BindView(R.id.send_email_button)
    Button mEmailButton;
    @BindView(R.id.user_inputlayout)
    TextInputLayout mInputLayout;
    @BindView(R.id.send_sms_button)
    Button mSmsButton;

    @BindString(R.string.link1)
    String mLink1;
    @BindString(R.string.link2)
    String mLink2;
    @BindString(R.string.link3)
    String mLink3;
    @BindString(R.string.link4)
    String mLink4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        mEmailButton.setOnClickListener(this);
        mSmsButton.setOnClickListener(this);
        setTypefaces();

    }


    @Override
    public void onClick(View v) {
        //first, clean up
        cleanUpUI();

        //then send the link
        if(isValidInput()) {

            switch (v.getId()) {
                case R.id.send_email_button:
                    new GMailSender(this)
                            .executeSendEmailTask(mUserInput, getRandomLink(), v, mDialog);
                    break;
                case R.id.send_sms_button:
                    new SmsSender(this)
                            .executeSendSMSTask(mUserInput,getRandomLink(),v, mDialog);
                    break;
            }
        }
        else {
            //otherwise show an error
            Log.d(TAG, "onClick: mUserInput is not valid : " + mUserInput);
            setError();

        }

        }

    public void setError(){
        //if the error is a format one:
        mInputLayout.setError(mFormatError);
        //TODO account for errors other than just formatting
    }


        /*switch (v.getId()) {
            case R.id.send_email_button:
                //check for valid input
                if (isValidInput()) {
                    showProgressDialog();
                    mInputEt.clearComposingText();
                    new GMailSender(this)
                            .executeSendEmailTask(v, getRandomLink(), mUserInput, mDialog);

                } else {
                    //otherwise show an error
                    Log.d(TAG, "onClick: mUserInput is not valid : " + mUserInput);
                    mInputLayout.setError(mFormatError);
                    //TODO account for errors other than just formatting
                }
                break;
            case R.id.temp_button:
                new SmsSender(this, tempEt.getText().toString(), getRandomLink()).executeSendSMSTask(v, mDialog);
                break;
        }*/





/*

    public class SendEmailTask extends AsyncTask<String, Integer, Boolean> {
        View mView;


        SendEmailTask(View view) {
            mView = view;
        }

        @Override
        protected Boolean doInBackground(String... params) {

            try {
                GMailSender sender = new GMailSender(mSenderEmail,
                        mSenderPass);
                sender.sendMail(mSubject,
                        params[0],
                        mSenderEmail,
                        mUserInput);

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
*/


    public void showProgressDialog() {
        mDialog = new ProgressDialog(MainActivity.this);
        mDialog.setMessage(getString(R.string.progress_dialog_message));
        mDialog.setIndeterminate(true);
        mDialog.show();
    }

    public boolean isValidInput() {
        mUserInput = mInputEt.getText().toString().trim();
        if(mUserInput.contains("@")){
            String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
            Pattern pattern = Pattern.compile(emailPattern);
            Matcher matcher = pattern.matcher(mUserInput);
            return matcher.matches();
        }
        else{
            //else its a phone nmber. return true in the meantime but...
            // TODO figure out how to validate a phone num...?
            return true;
        }


    }



    public void setTypefaces() {
        Typeface hnLight = Typeface.createFromAsset(getAssets(), getString(R.string.helvetica_neue_light_path));
        Typeface hn = Typeface.createFromAsset(getAssets(), getString(R.string.helvetica_neue_path));
        mInputLayout.setTypeface(hnLight);
        mInputEt.setTypeface(hn);
        mEmailButton.setTypeface(hn);
        mSmsButton.setTypeface(hn);
    }

    public String getRandomLink() {
        String[] links = {mLink1, mLink2, mLink3, mLink4};
        Random random = new Random();
        int i = random.nextInt(links.length);
        return links[i];
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }
    public void cleanUpUI(){
        hideKeyboard();
        showProgressDialog();
        mInputEt.clearComposingText();
    }


}
