package org.androidtown.maptest2;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class MarkerInsert extends AsyncTask<String, Void, String> {
    String sendMsg;
    @Override
    protected String doInBackground(String... params) {
        //1. php파일을 실행시킬수 있는 주소와 전송할 데이터 준비 (post방식)
        String serverURL = (String)params[0];
        String latitude = (String)params[1];
        String longitude = (String)params[2];
        String contents = (String)params[3];
        String uid = MapActivity.idid;
        Log.d("Currrent user", uid);

        try {
            sendMsg = URLEncoder.encode("latitude", "UTF-8") + "=" + URLEncoder.encode(latitude, "UTF-8");
            sendMsg += "&" + URLEncoder.encode("longitude", "UTF-8") + "=" + URLEncoder.encode(longitude, "UTF-8");
            sendMsg +=  "&" + URLEncoder.encode("contents", "UTF-8") + "=" + URLEncoder.encode(contents, "UTF-8");
            sendMsg +=  "&" + URLEncoder.encode("uid", "UTF-8") + "=" + URLEncoder.encode(uid, "UTF-8");
            URL url = new URL(serverURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept-Charset", "UTF-8");
            conn.setRequestProperty("Context_Type", "application/x-www-form-urlencoded;charset=UTF-8");

            OutputStream os = conn.getOutputStream();
            os.write(sendMsg.getBytes("UTF-8"));
            os.flush();
            Log.d("sendmsg", sendMsg);
            Log.d("==response Server", conn.getResponseMessage()+"");
            return conn.getResponseMessage()+"";
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return new String("Error: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            return new String("Error: " + e.getMessage());
        }
    }
}
