package com.alicelubic.fishercenteremailform;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * Created by owlslubic on 12/4/16.
 */

public class SmsSender {
    private static final String TAG = "sendSMS";
    private Context mContext;

    public SmsSender(Context context) {
        mContext = context;
    }

    public void executeSendSMSTask(final String phoneNum, final String body, final View view, final ProgressDialog dialog) {
        //first we connect
        ConnectivityManager conMng = (ConnectivityManager) mContext.getSystemService(CONNECTIVITY_SERVICE);
        //and we make sure the network is active
        NetworkInfo networkInfo = conMng.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //then make the network request on worker thread
            new SendSmsTask(phoneNum, body, view, dialog).execute();

        } else {
            Snackbar.make(view, R.string.no_internet_connection, Snackbar.LENGTH_LONG)
                    .setAction(R.string.task_failed_action, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new SendSmsTask(phoneNum, body, view, dialog).execute();
                        }
                    }).show();
        }
    }


    private void parseJson(String responseString) throws JSONException {

        JSONObject root = new JSONObject(responseString);
        JSONObject response = root.getJSONObject("Response");
        Log.d(TAG, "response Status: " + response.getString("Status"));
        Log.d(TAG, "response Code: " + response.getString("Code"));

        JSONObject entry = response.getJSONObject("Entry");
        Log.d(TAG, "Message ID: " + entry.getString("ID"));
        Log.d(TAG, "Subject: " + entry.getString("Subject"));
        Log.d(TAG, "Message: " + entry.getString("Message"));
        Log.d(TAG, "Message Type ID: " + entry.getString("MessageTypeID"));
        Log.d(TAG, "Total Recipients: " + entry.getString("RecipientsCount"));
        Log.d(TAG, "Credits Charged: " + entry.getString("Credits"));
        Log.d(TAG, "Time To Send: " + entry.getString("StampToSend"));
        Log.d(TAG, "Phone Numbers: " + entry.optString("PhoneNumbers", ""));
        Log.d(TAG, "Locally Opted Out Numbers: " + entry.optString("LocalOptOuts", ""));
        Log.d(TAG, "Globally Opted Out Numbers: " + entry.optString("GlobalOptOuts", ""));
    }

    private String readIt(InputStream is) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        String read;
        while ((read = reader.readLine()) != null) {
            builder.append(read);
        }
        return builder.toString();
    }


    private class SendSmsTask extends AsyncTask<Void, Void, Boolean> {
        private ProgressDialog mDialog;
        private View mView;
        private String mData;
        private String mPhoneNum;
        private String mLink;

        public SendSmsTask(String phoneNum, String body, View view, ProgressDialog dialog) {
            String eztUsername = mContext.getString(R.string.dev_sms_username);
            String eztPass = mContext.getString(R.string.dev_sms_pass);

            mPhoneNum = phoneNum;
            mLink = body;
            mDialog = dialog;
            mView = view;
            mData = "User=" + eztUsername + "&Password=" + eztPass +
                    "&PhoneNumbers[]=" + mPhoneNum + "&Message=" + mLink;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                //connect to the endpoint
                URL url = new URL("https://app.eztexting.com/sending/messages?format=json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //allowing output because thats how you POST
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(mData);
                //sending the data on its way, waiting for a response... in the form of an inputstream
                wr.flush();

                //check if the post request went through
                int responseCode = conn.getResponseCode();
                Log.i(TAG, "Response code: " + responseCode);
                boolean isSuccessResponse = responseCode < 400;

                InputStream responseStream = isSuccessResponse ? conn.getInputStream() : conn.getErrorStream();
                //note to self: this is a ternary operator, read as:
                // if response is successful, responseStream = conn.getInputStream(), else responseStream = getErrorStream

                if (responseStream != null) {
                    String responseString = readIt(responseStream);
                    Log.i(TAG, "ResponseString = " + responseString);
                    responseStream.close();

                    try {
                        //parsing just to log the info, not doing anything with it per se
                        parseJson(responseString);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                wr.close();
                return isSuccessResponse;


            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);
            mDialog.dismiss();

            if (!bool) {
                //let the user know it didn't work, action will try again
                Snackbar.make(mView, R.string.task_failed_message, Snackbar.LENGTH_LONG)
                        .setAction(R.string.task_failed_action, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new SmsSender(mContext).executeSendSMSTask(mPhoneNum, mLink,mView,mDialog);
                            }
                        }).show();
            }
            //let user know that it did work
            Snackbar.make(mView, R.string.task_success_message, Snackbar.LENGTH_SHORT)
                    .show();

        }

        //TODO make a snackbar to confirm, adjust parameters for view accordingly


    }


}