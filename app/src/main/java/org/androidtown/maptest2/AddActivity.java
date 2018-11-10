package org.androidtown.maptest2;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class AddActivity extends AppCompatActivity{
    private String TAG = "AddActivity";

    String fileFullPath;
    CognitoCachingCredentialsProvider credentialsProvider;
    AmazonS3 s3;
    TransferUtility transferUtility;
    String msg;
    TextView uploadBtn,selectBtn;
    ImageView imageview;
    File f;
    String imagename;
    private String userChoosenTask;
    Uri imageUri;
    String imagePath;
    private Uri mImageUri;
    private int PICTURE_CHOICE = 1;
    private int REQUEST_CAMERA = 2;
    private int SELECT_FILE = 3;
    TextView date_t, address_t;
    EditText content_t,locainfo_t;
    String date, lat,lng,locainfo,content;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        uploadBtn = (TextView) findViewById(R.id.uploadBtn); //서버 전송
        selectBtn = (TextView) findViewById(R.id.selectBtn); //사진 선택
        imageview = (ImageView) findViewById(R.id.image5);
        address_t = (TextView)findViewById(R.id.adrress_t);
        address_t.setText(MapActivity.curSearch);
        //전 화면에서 넘어온 intent를 받는다
        Intent intent =getIntent();
        //intent에서 넘어온 값을 전달받는다.
        lat = Double.toString(intent.getDoubleExtra("Latitude",0));
        lng = Double.toString(intent.getDoubleExtra("Longitude",0)); //intent putExtra에서 준 식별 태그와 같은 이름이여야 한다.

        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "ap-northeast-2:2b9240a3-c677-4e01-b2d0-f4a7ec0f7222", // 자격 증명 풀 ID
                Regions.AP_NORTHEAST_2 // 리전
        );

        s3 = new AmazonS3Client(credentialsProvider);
        s3.setRegion(Region.getRegion(Regions.AP_NORTHEAST_2));
        s3.setEndpoint("s3.ap-northeast-2.amazonaws.com");

        transferUtility = new TransferUtility(s3, getApplicationContext());


        selectBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                selectImage();
            }
        });
        uploadBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                date_t = findViewById(R.id.date_t);
                date = date_t.getText().toString();
                locainfo_t = findViewById(R.id.locainfo_t);
                locainfo =locainfo_t.getText().toString();
                content_t = findViewById(R.id.cont);
                content = content_t.getText().toString();
                Log.d("=====Content==",content);
                OnClickImageSubmit(); // 이미지 전송
                OnClickSubmit(); // 이미지 외 정보 전달
                finish();
            }
        });

        address_t.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddActivity.this,SearchActivity.class);
                startActivityForResult(intent,3000);
            }
        });


    }


    void OnClickSubmit() {
        try{
            insertAllData task = new insertAllData();
            task.execute(date, locainfo,content, lat, lng,"1",fileFullPath);
            // Log.i("리턴 값",test);

        }catch (Exception e)
        {
            Log.e("err",e.getMessage());
        }
    }

    void OnClickImageSubmit() {
        fileFullPath = "https://s3.ap-northeast-2.amazonaws.com/s3hackerthon/"+f.getName();
        imagename="12";
       // insertoToDatabase(fileFullPath, imagename);

        TransferObserver observer = transferUtility.upload(
                "s3hackerthon",
                f.getName(),
                f
        );

    }


    /*
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.uploadBtn:
                fileFullPath = "https://s3.ap-northeast-2.amazonaws.com/s3hackerthon/"+f.getName();
                imagename="12";
                insertoToDatabase(fileFullPath, imagename);


                TransferObserver observer = transferUtility.upload(
                        "s3hackerthon",
                        f.getName(),
                        f
                );
                break;
            case R.id.selectBtn:
                selectImage();
                break;
        }
    }*/

    private void selectImage() {

        Log.d(TAG, "select Image");
        final CharSequence[] items = {"촬영하기", "사진 가져오기",
                "취소"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("사진가져오기");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
  //              boolean result = Utility.checkPermission(getApplicationContext());
                boolean result = Utility.checkPermission(AddActivity.this);

                if (items[item].equals("촬영하기")) {
                    userChoosenTask = "촬영하기";
                    if (result)
                        cameraIntent();


                } else if (items[item].equals("사진 가져오기")) {
                    userChoosenTask = "사진 가져오기";
                    if (result)
                        galleryIntent();

                } else if (items[item].equals("취소")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case Utility.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (userChoosenTask.equals("촬영하기"))
                        cameraIntent();
                    else if (userChoosenTask.equals("사진 가져오기"))
                        galleryIntent();
                } else {
//code for deny
                }
                break;
        }
    }

    private void galleryIntent() {
        Log.d(TAG, "Gallery Intent");
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    private void cameraIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File photo = null;
        try {
// place where to store camera taken picture
            photo = this.createTemporaryFile("picture", ".jpg");
            photo.delete();
        } catch (Exception e) {
            Log.v(TAG, "Can't create file to take picture!");
            Toast.makeText(this, "sd카드를 확인해주세요", Toast.LENGTH_SHORT);
        }
        mImageUri = Uri.fromFile(photo);
        Log.d(TAG, mImageUri.toString());
        if (Build.VERSION.SDK_INT <= 19) {

        } else {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
        }
        startActivityForResult(intent, REQUEST_CAMERA);

    }

    private File createTemporaryFile(String part, String ext) throws Exception {
        File tempDir = Environment.getExternalStorageDirectory();
        tempDir = new File(tempDir.getAbsolutePath() + "/.temp/");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        return File.createTempFile(part, ext, tempDir);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                Log.d(TAG, "onActivityResult, SELECT_FILE");
                onSelectFromGalleryResult(data, SELECT_FILE);
            } else if (requestCode == REQUEST_CAMERA) {
                try {
                    onCaptureImageResult(data, REQUEST_CAMERA);
                } catch (Exception e) {
                    this.grabImage(imageview, REQUEST_CAMERA);
                }
            }
            else if(requestCode==3000)
            {
                address_t.setText(data.getStringExtra("search"));
            }

        }
    }

    private void onCaptureImageResult(Intent data, int imagetype) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        Bitmap bm = null;
        bm = null;
        File destination = new File(Environment.getExternalStorageDirectory(),
                System.currentTimeMillis() + ".jpg");
        Bitmap correctBmp = null;
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();

            File f = new File(RealPathUtil.getRealPathFromURI_API19(this, data.getData()));
            ExifInterface exif = new ExifInterface(f.getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            int angle = 0;

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                angle = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                angle = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                angle = 270;
            }

            Matrix mat = new Matrix();
            mat.postRotate(angle);

            Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(f), null, null);
            correctBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);


        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        bm = getResizedBitmap(correctBmp, getResources().getDimensionPixelSize(R.dimen.idcard_pic_height), getResources().getDimensionPixelSize(R.dimen.idcard_pic_width));
        if (imagetype == REQUEST_CAMERA) {
            f = SaveImage(bm);
            imageview.setImageBitmap(bm);
        }
    }
    private void onSelectFromGalleryResult(Intent data, int imagetype) {

        Log.d(TAG, "onSelectFromGalleryResult");
        Bitmap bm = null;
        imageUri = data.getData();
        if (Build.VERSION.SDK_INT < 11) {
            imagePath = RealPathUtil.getRealPathFromURI_BelowAPI11(this, imageUri);
            Log.d(TAG, Build.VERSION.SDK_INT + "");
        } else if (Build.VERSION.SDK_INT < 19) {
            Log.d(TAG, Build.VERSION.SDK_INT + "");
            imagePath = RealPathUtil.getRealPathFromURI_API11to18(this, imageUri);
        } else {
            Log.d(TAG, Build.VERSION.SDK_INT + "");
            imagePath = RealPathUtil.getRealPathFromURI_API19(this, imageUri);
        }
        Log.d(TAG, imagePath);


        try {
            bm = getResizedBitmap(decodeUri(data.getData()), getResources().getDimensionPixelSize(R.dimen.idcard_pic_height), getResources().getDimensionPixelSize(R.dimen.idcard_pic_width));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (imagetype == SELECT_FILE) {
            f = new File(imagePath);
            imageview.setImageBitmap(bm);
        }
    }

    public static Bitmap getResizedBitmap(Bitmap image, int newHeight, int newWidth) {
        int width = image.getWidth();
        int height = image.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
// create a matrix for the manipulation
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        if (Build.VERSION.SDK_INT <= 19) {
//matrix.postRotate(90);
        }
// recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(image, 0, 0, width, height,
                matrix, false);
        return resizedBitmap;
    }

    private Bitmap decodeUri(Uri selectedImage) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(
                this.getContentResolver().openInputStream(selectedImage), null, o);

        final int REQUIRED_SIZE = 100;

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;
        while (true) {
            if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE) {
                break;
            }
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(
                this.getContentResolver().openInputStream(selectedImage), null, o2);
    }

    public File SaveImage(Bitmap finalBitmap) {
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/MobileCard");

        if (!myDir.exists()) {
            Log.d("SaveImage", "non exists : " + myDir);
            myDir.mkdirs();
        }

        long now = System.currentTimeMillis();
        String fname = now + ".jpg";
        File file = new File(myDir, fname);

        if (file.exists()) {
            Log.d("SaveImage", "file exists");
            file.delete();
        } else {
            Log.d("SaveImage", "file non exists");
        }

        try {
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            Log.d("SaveImage", "file save");
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return file;
    }
    public void grabImage(ImageView imgView, int imagetype) {

        this.getContentResolver().notifyChange(mImageUri, null);
        ContentResolver cr = this.getContentResolver();
        Bitmap bitmap = null;
        Bitmap bm = null;
        Bitmap correctBmp = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(cr, mImageUri);

            File f = new File(RealPathUtil.getRealPathFromURI_API19(this, mImageUri));
            ExifInterface exif = new ExifInterface(f.getPath());
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            int angle = 0;

            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                angle = 90;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                angle = 180;
            } else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                angle = 270;
            }

            Matrix mat = new Matrix();
            mat.postRotate(angle);

            Bitmap bmp = BitmapFactory.decodeStream(new FileInputStream(f), null, null);
            correctBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);

        } catch (Exception e) {
            Toast.makeText(this, "불러오기에 실패했습니다", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Failed to load", e);
        }
        bm = getResizedBitmap(correctBmp, getResources().getDimensionPixelSize(R.dimen.idcard_pic_height), getResources().getDimensionPixelSize(R.dimen.idcard_pic_width));

        if (imagetype == REQUEST_CAMERA) {
            f = SaveImage(bm);
            imageview.setImageBitmap(bm);
        }
    }


    private void insertoToDatabase(String ip, String name) {

        class InsertData extends AsyncTask<String, Void, String> {
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                //loading.dismiss();


            }
            @Override
            protected String doInBackground(String... params) {
                Log.e("Id값은 ",params[0]);
                try {

                    String ip = params[0].trim();
                    String name = params[1].trim();


                    String link = "http://ec2-54-180-86-219.ap-northeast-2.compute.amazonaws.com/upload_link.php";
                    String str;

                    String data = URLEncoder.encode("ip", "UTF-8") + "=" + URLEncoder.encode(ip, "UTF-8");
                    data += "&" + URLEncoder.encode("name", "UTF-8") + "=" + URLEncoder.encode(name, "UTF-8");

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
        task.execute(ip, name);
    }

    class insertAllData extends AsyncTask<String, Void, String> {
        ProgressDialog loading;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //loading = ProgressDialog.show(MainActivity.this, "Please Wait", null, true, true);
        }
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            //loading.dismiss();
            //Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();



        }
        @Override
        protected String doInBackground(String... params) {
            try {

                String date_t = params[0];
                String locainfo_t = params[1];
                String content_t = params[2];
                String lat_t = params[3];
                String lng_t = params[4];
                String uid = params[5];
                String filep = params[6];


                String link = "http://ec2-54-180-86-219.ap-northeast-2.compute.amazonaws.com/uploadAllData.php";
                String str;

                String data = URLEncoder.encode("date", "UTF-8") + "=" + URLEncoder.encode(date_t, "UTF-8");
                data += "&" + URLEncoder.encode("locainfo", "UTF-8") + "=" + URLEncoder.encode(locainfo_t, "UTF-8");
                data += "&" + URLEncoder.encode("content", "UTF-8") + "=" + URLEncoder.encode(content_t, "UTF-8");
                data += "&" + URLEncoder.encode("latitude", "UTF-8") + "=" + URLEncoder.encode(lat_t, "UTF-8");
                data += "&" + URLEncoder.encode("longitude", "UTF-8") + "=" + URLEncoder.encode(lng_t, "UTF-8");
                data += "&" + URLEncoder.encode("uid", "UTF-8") + "=" + URLEncoder.encode(uid, "UTF-8");
                data += "&" + URLEncoder.encode("filep", "UTF-8") + "=" + URLEncoder.encode(filep, "UTF-8");

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

                    Log.i("통신 결과", msg);

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
}