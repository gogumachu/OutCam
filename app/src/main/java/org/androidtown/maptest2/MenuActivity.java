package org.androidtown.maptest2;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.util.Map;

public class MenuActivity extends AppCompatActivity {
    TextView uid;
    TextView upass;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
/*
        uid = findViewById(R.id.userId);
        upass =findViewById(R.id.userPass);
        Intent intent = getIntent();
        uid.setText(intent.getStringExtra("id"));
        upass.setText(intent.getStringExtra("pw"));*/
    }
}
