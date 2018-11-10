package org.androidtown.maptest2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

public class MarkerInfoActivity extends AppCompatActivity {
    TextView  minfo1;
    TextView  minfo2;
    TextView minfo3;
    ImageView img;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_info);
        minfo1 = findViewById(R.id.date_tt);
       minfo2 = findViewById(R.id.locainfo_tt);
       minfo3= findViewById(R.id.contt);
       img = findViewById(R.id.image5_1);

        //전 화면에서 넘어온 intent를 받는다
        Intent intent =getIntent();
        //intent에서 넘어온 값을 전달받는다.
        minfo1.setText(intent.getStringExtra("date")); //intent putExtra에서 준 식별 태그와 같은 이름이여야 한다.
        minfo2.setText(intent.getStringExtra("locainfo"));
        minfo3.setText(intent.getStringExtra("content"));
        String urll = intent.getStringExtra("url").trim();
        Glide.with(this).load(urll).into(img);
    }
}
