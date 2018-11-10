package org.androidtown.maptest2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.androidtown.maptest2.models.PlaceInfo;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by User on 10/2/2017.
 */

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.OnConnectionFailedListener{
    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));


    //widgets
    private AutoCompleteTextView mSearchText; //검색
    private ImageView mGps;  //누르면 현재위치로 이동

    //vars
    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;
    private PlaceInfo mPlace;
    //현재 가리키고 있는 위치
    Double curLatitude;
    Double curLongitude;
    public static String curSearch="";
    //for server
    String myJSON;
    JSONArray infos = null;
    //for Cluster
    private ClusterManager<MyItem> mClusterManager;
    //for ListLayout
    public static LinearLayout recyclerLayout;
    public static LinearLayout recyclerLayout1;
    public static TextView recylerText;
    public static TextView recylerText1;

    //for List
    public static RecyclerView recyclerView;

    // PagerAdapter padapter;
    List<String> conArray = new ArrayList<>();

    //for login
    static String idid;
    static String pwpw;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if (mLocationPermissionsGranted) {
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            init();
        }
        /*===========For map Click=========*/
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            @Override
            public void onMapClick(LatLng point) {
              //  viewPager.setVisibility(View.INVISIBLE);
             //   recyclerView.setVisibility(View.INVISIBLE);
                recyclerLayout.setVisibility(View.INVISIBLE);
                recyclerLayout1.setVisibility(View.INVISIBLE);
            }
        });

        /*===========For Clustering===========*/
        mClusterManager = new ClusterManager<>(this, mMap);
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        /*===마커 클릭시 ====*/
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
            @Override
            public boolean onClusterItemClick(MyItem myItem) {
               ListMarkerRead task = new ListMarkerRead(getApplicationContext());
               String lat =  Double.toString(myItem.getPosition().latitude);
               String longi = Double.toString(myItem.getPosition().longitude);
               task.execute("http://ec2-54-180-86-219.ap-northeast-2.compute.amazonaws.com/readsm.php",lat,longi);
               Toast.makeText(MapActivity.this,myItem.getPosition().toString() + "스와이프 intent 생성하기", Toast.LENGTH_SHORT).show();
               curLatitude = myItem.getPosition().latitude;
               curLongitude = myItem.getPosition().longitude;
                return true;
            }
        });
     //   addItems();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        Intent intent = getIntent();
        idid = intent.getStringExtra("id");
        pwpw = intent.getStringExtra("pw");
        mSearchText = (AutoCompleteTextView) findViewById(R.id.input_search);
        mGps = (ImageView) findViewById(R.id.ic_gps);

        getLocationPermission();
        /*==List====*/
        // 리니어로 레이아웃 설정
        recyclerView = findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        recyclerLayout = findViewById(R.id.listlayout);
        recyclerLayout1 = findViewById(R.id.listlayout1);
        recylerText = findViewById(R.id.listinfo);
        recylerText1 =findViewById(R.id.listinfo1);

        /*View Pager
        viewPager = (ViewPager) findViewById(R.id.pager);
        int dpValue=30;
        float d=getResources().getDisplayMetrics().density;
        int margin = (int)(dpValue*d);
        viewPager.setPadding(margin,0,margin,0);
        viewPager.setPageMargin(margin/2);
        viewPager.setClipToPadding(false);*/
        /*신고하기 버튼 누를때*/
        Button addBtn = (Button)findViewById(R.id.addBtn);
        addBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddActivity.class);
                intent.putExtra("Latitude",curLatitude);
                intent.putExtra("Longitude",curLongitude);
                startActivity(intent);
            }
        });
        /*===메뉴로 이동====*/
        ImageView menuBtn = (ImageView) findViewById(R.id.menubtn);
        menuBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MenuActivity.class);
                intent.putExtra("id",idid);
                intent.putExtra("pw",pwpw);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
        /*=== notification 테스트  ==*/
        /*
       Button notiBtn = (Button)findViewById(R.id.notibtn);
        notiBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapActivity.this,MyService.class);
                startService(intent);
            }
        });
        Button endBtn = (Button)findViewById(R.id.endbtn);
        endBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapActivity.this,MyService.class);
                stopService(intent);
            }
        });*/
    }

    protected void onStart() {
        //서버에 올라간 데이터를 읽고 리스트 업데이트 한다.
        getData("http://ec2-54-180-86-219.ap-northeast-2.compute.amazonaws.com/readm.php");
        super.onStart();
    }

    private void init(){

        Log.d(TAG, "init: initializing");

        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
        mSearchText.setOnItemClickListener(mAutocompleteClickListener);

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this, mGoogleApiClient,
                LAT_LNG_BOUNDS, null);

        mSearchText.setAdapter(mPlaceAutocompleteAdapter);
        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER){
                    //Log.e("result"," getlocate!");
                    //execute our method for searching
                    geoLocate();
                }

                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked gps icon");
                getDeviceLocation();
            }
        });

        hideSoftKeyboard();
    }
    //위치 검색하면 나오게 함
    private void geoLocate(){
        Log.d(TAG, "geoLocate: geolocating");
        //검색한 지역 문자열
        String searchString = mSearchText.getText().toString();
        curSearch = mSearchText.getText().toString();
        //문자열을 위도, 경도로 바꾸주는 역할 한다.
        Geocoder geocoder = new Geocoder(MapActivity.this);
        List<Address> list = new ArrayList<>();
        try{
            list = geocoder.getFromLocationName(searchString, 1);
        }catch (IOException e){
            Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
        }

        if(list.size() > 0){
            Address address = list.get(0);

            Log.d(TAG, "geoLocate: found a location: " + address.toString());
            //Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();
            //찾은 지역으로 카메라 이동
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
                    address.getAddressLine(0));

            Log.d("=====Location======",curLatitude+"");
        }
    }
    //현재 기기의 위치 구함
    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location!");
                            //현재 위치 구함
                            Location currentLocation = (Location) task.getResult();
                            //현재 위치로 카메라 이동
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM,
                                    "My Location");
                            curLatitude = currentLocation.getLatitude();
                            curLongitude = currentLocation.getLongitude();
                            double lat = currentLocation.getLatitude();
                            double longi = currentLocation.getLongitude();
                            List<Address> list = null;
                            Geocoder geocoder = new Geocoder(MapActivity.this, Locale.getDefault());
                            try {
                                list = geocoder.getFromLocation(lat,longi,1);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            curSearch = list.get(0).getAddressLine(0);
                            Log.d("cur adress", curSearch);

                        }else{
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(MapActivity.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title){
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        curLatitude = latLng.latitude;
        curLongitude =  latLng.longitude;
        Log.d("===title====", title);
        if(!title.equals("My Location")){
            Log.d("===State====", "검색한 위치임");
        }

        hideSoftKeyboard();
    }

    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        mapFragment.getMapAsync(MapActivity.this);
    }

    private void addItems() {
        double lat = 37.2706008;
        double lng = 127.01357559999997;
        for(int i=0; i<10; i++) {
            double offset = i/60d;
            lat = lat + offset;  lng = lng + offset;
            MyItem offsetItem = new MyItem(lat, lng);
            mClusterManager.addItem(offsetItem);
            //mClusterManager.
        }
    }
    private void getLocationPermission(){
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    //initialize our map
                    initMap();
                }
            }
        }
    }

    private void hideSoftKeyboard(){
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            hideSoftKeyboard();

            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(i);
            final String placeId = item.getPlaceId();

            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient,placeId);

            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if(!places.getStatus().isSuccess()) {
                Log.d(TAG, "onResult: Place query did not complete successfully: " + places.getStatus().toString());
                places.release();
                return;
            }
            final Place place = places.get(0);

            try{
                Log.d(TAG, "onResult: place: " + place.getViewport().getCenter().latitude);
                curSearch = place.getName().toString();
                mPlace = new PlaceInfo();
                mPlace.setName(place.getName().toString());
                Log.d(TAG, "onResult: place: " + place.getName().toString());
                mPlace.setAddress(place.getAddress().toString());
                Log.d(TAG, "onResult: place: " + place.getAddress().toString());
                mPlace.setLatlng(place.getLatLng());
                Log.d(TAG, "onResult: place: " + place.getLatLng());

                /*
                mPlace.setAttributions(place.getAttributions().toString());
                Log.d(TAG, "onResult: place: " + place.getAttributions().toString());



                mPlace.setId(place.getId());
                Log.d(TAG, "onResult: place: " + place.getId());

                mPlace.setRating(place.getRating());
                Log.d(TAG, "onResult: place: " + place.getRating());
                mPlace.setPhotoNumber(place.getPhoneNumber().toString());

                Log.d(TAG, "onResult: place: " + place.getPhoneNumber().toString());
                mPlace.setWebsiteUri(place.getWebsiteUri());
                Log.d(TAG, "onResult: place: " + place.getWebsiteUri());

                Log.d(TAG, "onResult: place: " + mPlace.toString());
                */
            }catch (NullPointerException e) {
                Log.e(TAG, "onResult: NullPointerException: " + e.getMessage());
            }

            //moveCamera(new LatLng(place.getViewport().getCenter().latitude, place.getViewport().getCenter().longitude), DEFAULT_ZOOM, mPlace.getName());

            moveCamera(new LatLng(place.getViewport().getCenter().latitude, place.getViewport().getCenter().longitude),
                    DEFAULT_ZOOM,
                    "My Location");


            places.release();
        }
    };


    /*서버에 올라간 data를 php파일 이용해 가져온다.*/
    public void getData(String url){
        class AllMarkerRead extends AsyncTask<String, Void, String> {
            @Override
            protected String doInBackground(String... params) {
                String uri = params[0];
                BufferedReader bufferedReader = null;
                try{
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    StringBuilder sb = new StringBuilder();
                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String json;

                    while((json = bufferedReader.readLine())!= null){
                        sb.append(json+"\n"); // json으로 array 받아오기
                    }
                    bufferedReader.close();
                    return sb.toString().trim();
                }catch(Exception e){
                    return null;
                }
            }//end doinBackgroung

            @Override /*doinbackground가 끝나면 실행되는 함수*/
            protected void onPostExecute(String result) {
                Log.d("====Async Pst===","running");
                myJSON=result;
                try{
                    JSONObject jsonObj = new JSONObject(myJSON);
                    infos = jsonObj.getJSONArray(BasicInfo.TAG_RESULTS);

                    for(int i=0; i<infos.length(); i++)
                    {
                        JSONObject c = infos.getJSONObject(i);
                        Double latitude = c.getDouble(BasicInfo.TAG_LAT);
                        Double longitude = c.getDouble(BasicInfo.TAG_LONG);
                        Log.d("==From server lat==", latitude+"");
                        /*
                        MarkerOptions mOptions = new MarkerOptions();
                        mOptions.position(new LatLng(latitude, longitude));
                        mMap.addMarker(mOptions);
                        */
                        mClusterManager.addItem(new MyItem(latitude,longitude));
                    }
                }catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }//end async class
        AllMarkerRead mr = new AllMarkerRead();
        mr.execute(url);
    }


}











