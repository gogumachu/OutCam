package org.androidtown.maptest2;

import android.app.Dialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Trace;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity  {

    private static final String TAG = "MainActivity";

    private static final int ERROR_DIALOG_REQUEST = 9001;
    private EditText editTextId;
    private EditText editTextPw;
    TextView signBtn;
    String sId, sPw;
    String param;
    String data = "";
    String idid,pwpw;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editTextId = findViewById(R.id.user_id);
        editTextPw = findViewById(R.id.user_pw);
        signBtn = findViewById(R.id.btn_signin);
        if(isServicesOK()){
            init();
        }

        signBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                signin(view);
            }
        });
    }

    private void init(){
        TextView btnMap = (TextView) findViewById(R.id.btnMap);
        btnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Intent intent = new Intent(MainActivity.this, MapActivity.class);
                //startActivity(intent);
                CliSignUp(view);
            }
        });
    }

    public void signin(View v)
    {
        try{
            String result;
            sId = editTextId.getText().toString();
            sPw = editTextPw.getText().toString();
            param = "u_id=" + sId + "&u_pw=" + sPw + "";
            loginDB lDB = new loginDB();
            lDB.execute(sId,sPw);
            /*
            result = lDB.execute(sId,sPw).get();
            Log.i("리턴 값",result);

            JSONObject jsonObj = new JSONObject(result);
            JSONArray peoples = null;

            peoples = jsonObj.getJSONArray("result");
            JSONObject c = peoples.getJSONObject(0);
            String id = c.getString("u_id");
            String asdf="없으";
            if(id=="")Log.i("id 값",asdf);
            else Log.i("id 값",id);*/
        }catch (Exception e)
        {
            Log.e("err",e.getMessage());
        }

    }

    class loginDB extends AsyncTask<String, Void, String> {
        String sendMsg, receiveMsg;
        @Override
        // doInBackground의 매개값이 문자열 배열인데요. 보낼 값이 여러개일 경우를 위해 배열로 합니다.
        protected String doInBackground(String... strings) {
            try {
                String Id = sId;
                String Pw = sPw;

                String link = "http://ec2-54-180-86-219.ap-northeast-2.compute.amazonaws.com/sign_in.php";
                String str;

                sendMsg = URLEncoder.encode("Id", "UTF-8") + "=" + URLEncoder.encode(Id, "UTF-8");
                sendMsg += "&" + URLEncoder.encode("Pw", "UTF-8") + "=" + URLEncoder.encode(Pw, "UTF-8");

                URL url = new URL(link);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("POST");
                conn.setRequestProperty("Accept-Charset", "UTF-8");
                conn.setRequestProperty("Context_Type", "application/x-www-form-urlencoded;charset=UTF-8");

                OutputStream os = conn.getOutputStream();
                os.write(sendMsg.getBytes("UTF-8"));
                os.flush();

                //jsp와 통신이 정상적으로 되었을 때 할 코드들입니다.
                if(conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    //jsp에서 보낸 값을 받겠죠?
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();

                } else {
                    Log.i("통신 결과", conn.getResponseCode()+"에러");
                    // 통신이 실패했을 때 실패한 이유를 알기 위해 로그를 찍습니다.
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //jsp로부터 받은 리턴 값입니다.
            return receiveMsg;
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try{

                JSONObject jsonObj = new JSONObject(s);
                JSONArray peoples = null;

                peoples = jsonObj.getJSONArray("result");

                if(peoples.length()==0) Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_LONG).show();
                else {
                    Toast.makeText(getApplicationContext(), "로그인 성공", Toast.LENGTH_LONG).show();
                    JSONObject c = peoples.getJSONObject(0);
                    idid = c.getString("u_id");
                    pwpw=c.getString("u_pw");
                    Intent intent =  new Intent(MainActivity.this, MapActivity.class);
                    intent.putExtra("id",idid);
                    intent.putExtra("pw",pwpw);
                    startActivity(intent);
                    finish();
                }

            }catch (Exception e)
            {
                Log.e("err",e.getMessage());
            }


        }
    }

    public void CliSignUp(View view)
    {
        Intent intent = new Intent(this, SignupPage.class);
        startActivity(intent);
    }


    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }



}