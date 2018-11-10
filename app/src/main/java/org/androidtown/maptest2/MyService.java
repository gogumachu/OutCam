package org.androidtown.maptest2;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class MyService extends Service {
    NotificationManager Notifi_M;
    ServiceThread thread;
    Notification Notifi ;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Notifi_M = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        myServiceHandler handler = new myServiceHandler();
        thread = new ServiceThread(handler);
        thread.start();
        return START_STICKY;
    }

    //서비스가 종료될 때 할 작업

    public void onDestroy() {
        thread.stopForever();
        thread = null;//쓰레기 값을 만들어서 빠르게 회수하라고 null을 넣어줌.
    }

    class myServiceHandler extends Handler {
        @Override
        public void handleMessage(android.os.Message msg) {
            // 채널 아이디 넣어야 함
            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "default");
            // 필수 항목
            builder.setSmallIcon(R.mipmap.ic_launcher);
            builder.setContentTitle("알림 제목");
            builder.setContentText("알림 세부 텍스트");
            // 액션 정의
            // 메인 액티비티 실행시킬 인텐트
            Intent intent = new Intent(MyService.this, MapActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(MyService.this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            // 클릭 이벤트 설정
            builder.setContentIntent(pendingIntent);
            // 큰 아이콘 설정
            Bitmap largeIcon = BitmapFactory.decodeResource(getResources(),
                    R.mipmap.ic_launcher);
            builder.setLargeIcon(largeIcon);
            // 색상 변경
            builder.setColor(Color.RED);
            // 진동설정: 대기시간, 진동시간, 대기시간, 진동시간.. 패턴 반복
            long[] vibrate = {0, 100, 200, 300};
            builder.setVibrate(vibrate);
            builder.setAutoCancel(true);
            // 알림 매니저
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                manager.createNotificationChannel(new NotificationChannel("default", "기본채널",
                        NotificationManager.IMPORTANCE_DEFAULT));
            }
            // 알림 중지
            manager.notify(1, builder.build());
        }
    };
}

