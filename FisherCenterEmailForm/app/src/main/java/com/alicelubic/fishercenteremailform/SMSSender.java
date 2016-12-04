package com.alicelubic.fishercenteremailform;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import butterknife.BindString;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * Created by owlslubic on 12/4/16.
 */

public class SMSSender {
    private static final String TAG = "sendSMS";
    private Context mContext;
    private String mData;

    /**
     * doing this in its own class because i wanna try it the long way
     * rather than using a library
     * just for fun
     * but it'd get pretty messy in the main activity, so....
     */


    public SMSSender(Context context, String phoneNum, String linkUrl) {
        String senderUsername = context.getString(R.string.dev_sms_username);
        String senderPass = context.getString(R.string.dev_sms_pass);

        mContext = context;
        mData = "User=" + senderUsername + "&Password=" + senderPass +
                "&PhoneNumbers[]=" + phoneNum + "&Message=" + linkUrl;
    }


    public void sendSMS() {
        //first we connect
        ConnectivityManager conMng = (ConnectivityManager) mContext.getSystemService(CONNECTIVITY_SERVICE);
        //and we make sure the network is active
        NetworkInfo networkInfo = conMng.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            //then make the network request on worker thread
            new SendSmsTask().execute();

        } else {
            Toast.makeText(mContext, "No network connection available!", Toast.LENGTH_SHORT).show();
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


    public class SendSmsTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {

            try {

               URL url = new URL("https://app.eztexting.com/sending/messages?format=json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");

                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(mData);
                wr.flush();

                int responseCode = conn.getResponseCode();
                Log.i(TAG,"Response code: " + responseCode);

                boolean isSuccessResponse = responseCode < 400;

                InputStream responseStream = isSuccessResponse ? conn.getInputStream() : conn.getErrorStream();
                if (responseStream != null) {
                    String responseString = readIt(responseStream);
                    responseStream.close();

                    try {
                        parseJson(responseString);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                wr.close();


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        //TODO make a snackbar to confirm, adjust parameters for view accordingly


    }


}
