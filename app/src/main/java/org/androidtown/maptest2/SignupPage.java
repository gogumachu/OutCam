package org.androidtown.maptest2;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


public class SignupPage extends AppCompatActivity{
    private EditText editTextId;
    private EditText editTextPw;

    String msg, idid;
    String chkid="";
    String chkpw="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editTextId = findViewById(R.id.new_id);
        editTextPw = findViewById(R.id.new_pw);
    }

    public void insert(View view) {
        String Id = editTextId.getText().toString();
        String Pw = editTextPw.getText().toString();

        insertoToDatabase(Id, Pw);
    }

    private void insertoToDatabase(String Id, String Pw) {

        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(SignupPage.this, "Please Wait", null, true, true);
            }
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();

                Log.e("msg",msg);

                if(msg.equals("login success!")) {
                    Log.d("Login Check" , "=======LoginSuccesss!!");

                    finish();
                    // 왜 뒤로 안가는 지 의문임... => if문이 실행되지 않음! java의 특징 ==비교시 자바는 스트링의 주소값을 비교한다.
                    //equals을 써야한다.
                }else
                    Log.d("Login Check" , "=======Login Fail!");

            }
            @Override
            protected String doInBackground(String... params) {
                Log.e("Id값은 ",params[0]);
                try {

                    String Id = params[0].trim();
                    String Pw = params[1].trim();
                    chkid=Id;
                    chkpw=Pw;


                    String link = "http://ec2-54-180-86-219.ap-northeast-2.compute.amazonaws.com/sign_up.php";
                    String str;

                    String data = URLEncoder.encode("Id", "UTF-8") + "=" + URLEncoder.encode(Id, "UTF-8");
                    data += "&" + URLEncoder.encode("Pw", "UTF-8") + "=" + URLEncoder.encode(Pw, "UTF-8");

                    URL url = new URL(link);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Accept-Charset", "UTF-8");
                    conn.setRequestProperty("Context_Type", "application/x-www-form-urlencoded;charset=UTF-8");

                    OutputStream os = conn.getOutputStream();
                    os.write(data.getBytes("UTF-8"));
                    os.flush();

                    //jsp와 통신이 정상적으로 되었을 때 할 코드들입니다.
                    if(conn.getResponseCode() == conn.HTTP_OK) {

                        InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                        BufferedReader reader = new BufferedReader(tmp);
                        StringBuffer buffer = new StringBuffer();

                        while ((str = reader.readLine()) != null) {
                            buffer.append(str);
                        }
                        msg = buffer.toString();

                        JSONObject jsonObj = new JSONObject(msg);


                    } else {
                        Log.i("통신 결과", conn.getResponseCode()+"에러");
                        // 통신이 실패했을 때 실패한 이유를 알기 위해 로그를 찍습니다.
                    }

                } catch (Exception e) {

                    return new String("Exception: " + e.getMessage());
                }
                return msg;
            }
        }
        InsertData task = new InsertData();
        task.execute(Id, Pw);
    }
}
