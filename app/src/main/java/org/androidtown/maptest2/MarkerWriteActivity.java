package org.androidtown.maptest2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class MarkerWriteActivity extends AppCompatActivity {
    TextView lat_t;
    TextView long_t;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_write);

        lat_t = findViewById(R.id.latinfo);
        long_t = findViewById(R.id.longinfo);

        //전 화면에서 넘어온 intent를 받는다
        Intent intent =getIntent();
        //intent에서 넘어온 값을 전달받는다.

        lat_t.setText(""+intent.getDoubleExtra("Latitude",0)); //intent putExtra에서 준 식별 태그와 같은 이름이여야 한다.
        long_t.setText(""+intent.getDoubleExtra("Longitude",0));
    }
}
