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
import android.widget.LinearLayout;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "Main";
    private String mUserInput;
    private ProgressDialog mDialog;

    @BindString(R.string.dev_email)
    String mSenderEmail;
    @BindString(R.string.dev_email_pass)
    String mSenderPass;
    @BindString(R.string.edit_text_email_error)
    String mEmailError;
    @BindString(R.string.edit_text_sms_error)
    String mSMSError;
    @BindString(R.string.subject_text)
    String mSubject;
    @BindString(R.string.link1)
    String mLink1;
    @BindString(R.string.link2)
    String mLink2;
    @BindString(R.string.link3)
    String mLink3;
    @BindString(R.string.link4)
    String mLink4;
    @BindView(R.id.user_input_et)
    EditText mInputEt;
    @BindView(R.id.send_email_button)
    Button mEmailButton;
    @BindView(R.id.user_inputlayout)
    TextInputLayout mInputLayout;
    @BindView(R.id.send_sms_button)
    Button mSmsButton;
    @BindView(R.id.activity_main)
    LinearLayout mMainLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        mEmailButton.setOnClickListener(this);
        mSmsButton.setOnClickListener(this);
        mMainLayout.setOnClickListener(this);
        setTypefaces();

        //TODO make a method for IF IT'S LANDSCAPE, MAKE EVERYTHING BIGGER programmatically

    }


    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.send_email_button:
                if (isValidInput()) {
                    cleanUpUI(v);
                    new GMailSender(this)
                            .executeSendEmailTask(mUserInput, getRandomLink(), v, mDialog);
                } else {
                    setError("email");
                }
                break;
            case R.id.send_sms_button:
                if (isValidInput()) {
                    cleanUpUI(v);
                    new SmsSender(this)
                            .executeSendSMSTask(mUserInput, getRandomLink(), v, mDialog);
                } else {
                    setError("sms");
                }

                break;
            case R.id.activity_main:
                mInputLayout.clearFocus();
                v.requestFocus();
                hideKeyboard(v);
                break;
        }

    }
    public void cleanUpUI(View v) {
        hideKeyboard(v);
//        mInputEt.clearComposingText();
        mInputEt.setText("");
        mInputEt.clearFocus();
        mInputLayout.clearFocus();
        showProgressDialog();

    }

    public void setError(String emailOrSMS) {
        if (emailOrSMS == "email") {
            //if the error is a format one:
            mInputLayout.setError(mEmailError);
        } else if (emailOrSMS.equals("sms")) {
            mInputLayout.setError(mSMSError);
        } else {
            //do nothing
        }

        //TODO account for errors other than just formatting
    }


    public void showProgressDialog() {
        mDialog = new ProgressDialog(MainActivity.this);
        mDialog.setMessage(getString(R.string.progress_dialog_message));
        mDialog.setIndeterminate(true);
        mDialog.show();
    }

    public boolean isValidInput() {
        mUserInput = mInputEt.getText().toString().trim();

        if (mUserInput.length() == 0) {
            return false;
        }
        //if it's an email:
        else if (mUserInput.contains("@")) {
            if (mUserInput.length() < 3) {
                return false;
            }
            String emailPattern = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
            Pattern pattern = Pattern.compile(emailPattern);
            Matcher matcher = pattern.matcher(mUserInput);
            return matcher.matches();
        } else {//it's a phone number
            if (mUserInput.length() < 7) {
                //invalid phone number length
                return false;
                // TODO figure out how to validate a phone num...?
            }
            //otherwise it's a valid phone number
            return true;
        }
    }


    public void setTypefaces() {
        Typeface hnLight = Typeface.createFromAsset(getAssets(), getString(R.string.helvetica_neue_light_path));
        Typeface hn = Typeface.createFromAsset(getAssets(), getString(R.string.helvetica_neue_path));
        mInputLayout.setTypeface(hn);
        mInputEt.setTypeface(hnLight);
        mEmailButton.setTypeface(hn);
        mSmsButton.setTypeface(hn);
    }

    public String getRandomLink() {
        String[] links = {mLink1, mLink2, mLink3, mLink4};
        Random random = new Random();
        int i = random.nextInt(links.length);
        return links[i];
    }

    public void hideKeyboard(View view) {
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//            imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

        }
    }


}
