package org.androidtown.maptest2;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListMarkerRead extends AsyncTask<String, Void, String> {
    //for server
    Context context;
    String myJSON;
    JSONArray infos = null;
    //list
    List<CardItem> dataList = new ArrayList<>();
    MyRecyclerAdapter adapter;
    //swipe
    List<String> conArray;
    PagerAdapter padapter;
    public ListMarkerRead(Context  context) {
        conArray=new ArrayList();
        this.context = context;
      //  viewpager =
    }

    public List<String> getConArray() {
        return conArray;
    }
    @Override
    protected String doInBackground(String... params) {
        String uri = params[0];
        String latitude = (String)params[1];
        String longitude = (String)params[2];
        BufferedReader bufferedReader = null;
        try{
            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST"); //요청 방식을 POST로 합니다.
            //json파일에 대한정보 설정 (꼭 해줘야 되는지는 모르겠음)
            con.setRequestProperty("Content-Type","application/json");
            con.setRequestProperty("Accept","application/json");
            con.setDoOutput(true);
            /* build JSON object & 데이터 전송*/
            JSONObject jsonObject = new JSONObject();
            try{
                jsonObject.accumulate("latitude",latitude);
                jsonObject.accumulate("longitude",longitude);
            }catch(JSONException e)
            {
                return "json error1";
            }
            //데이터 전송
            OutputStream os = con.getOutputStream(); //outputstream을 얻어온다.
            Log.d("Json location",jsonObject.toString());
            os.write(jsonObject.toString().getBytes());
            os.flush();
            os.close();
            //전송이 정상적으로 됐다면
            StringBuilder sb = new StringBuilder();
            if(con.getResponseCode() == con.HTTP_OK)
            {
                bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String json;
                while((json = bufferedReader.readLine())!= null){
                    sb.append(json+"\n"); // json으로 array 받아오기
                }
                bufferedReader.close();
            }else {
                Log.i("통신 결과", con.getResponseCode()+"에러");
                // 통신이 실패했을 때 실패한 이유를 알기 위해 로그를 찍습니다.
            }
            return sb.toString().trim();
        }catch(Exception e){
            return null;
        }
    }//end Backgroung

    @Override /*doinbackground가 끝나면 실행되는 함수*/
    protected void onPostExecute(String result) {
        Log.d("====Async Pst2===","running");
        myJSON=result;
        try{
            JSONObject jsonObj = new JSONObject(myJSON);
            infos = jsonObj.getJSONArray(BasicInfo.TAG_RESULTS);

            for(int i=0; i<infos.length(); i++)
            {
                JSONObject c = infos.getJSONObject(i);
     //           String adrr = c.getString(BasicInfo.TAG_ADDRESS);
                String contents = c.getString(BasicInfo.TAG_CONTENTS);
                String date = c.getString(BasicInfo.TAG_DATE);
                String locainfo= c.getString(BasicInfo.TAG_LOCAINFO);
                String urll = c.getString("url");
                //conArray.add(contents);
                dataList.add(new CardItem(locainfo,
                        contents, date,urll));
                Log.d("==From server adrr==", contents+"");
            }

        }catch (JSONException e) {
            e.printStackTrace();
        }
        //리스트 보이게 하기
      //  MapActivity.viewPager.setVisibility(View.VISIBLE);
       // MapActivity.recyclerView.setVisibility(View.VISIBLE);
        MapActivity.recyclerLayout.setVisibility(View.VISIBLE);
        MapActivity.recyclerLayout1.setVisibility(View.VISIBLE);
        MapActivity.recylerText.setText("발견사례 3건");
        MapActivity.recylerText1.setText("임시주소");
        // Pass results to ViewPagerAdapter Class
       // padapter = new ViewPagerAdapter(context, conArray);
        adapter = new MyRecyclerAdapter(dataList);
        // Binds the Adapter to the ViewPager
        MapActivity.recyclerView.setAdapter(adapter);
      //  MapActivity.viewPager.setAdapter(padapter);
    }
}
