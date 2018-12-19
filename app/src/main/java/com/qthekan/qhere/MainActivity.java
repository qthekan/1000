package com.qthekan.qhere;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.qthekan.qhere.Favorite.FavoriteActivity;
import com.qthekan.qhere.joystick.JoystickService;
import com.qthekan.qhere.radar.Poke;
import com.qthekan.qhere.radar.RadarActivity;
import com.qthekan.qhere.radar.listview.CustomAdapter;
import com.qthekan.qhere.talk.ChatService;
import com.qthekan.qhere.walk.WalkActivity;
import com.qthekan.qhere.walk.WalkThread;
import com.qthekan.util.qBackPressExitApp;
import com.qthekan.util.qlog;
import com.qthekan.util.qutil;

import java.util.ArrayList;

import static java.lang.Thread.sleep;


public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private static MainActivity ins = null;

    public GoogleMap mMap;

    private Button mBtnSearch;
    private EditText mEtSearch;

    // mock location is running
    public boolean mMockRunning;
    public AdsMgr mAds;


    public static MainActivity getIns()
    {
        return ins;
    }


    private void initView()
    {
        mAds = new AdsMgr(this, (AdView) findViewById(R.id.adView));

        mEtSearch = findViewById(R.id.etSearch);
        mBtnSearch = findViewById(R.id.btnSearch);

        mLayoutSubMenu = findViewById(R.id.viewSubMenu);
        mLayoutStop = findViewById(R.id.viewStop);

        initChatService();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ins = this;
        mMockRunning = false;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //=====================================================================
        // Runtime Exception 에 의해 thread 가 종료될 경우
        // 로그를 출력하도록 설정한다.
        //=====================================================================
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException(Thread thread, Throwable e)
            {
                qlog.e("uncaughtException() thread dead: " + thread.getName() + "\n" + e.toString() );
                // 20180307 main thread 가 죽은 경우 프로세스 재기동이 되지 않아서 추가함.
                System.exit(0);
            }
        });

        initView();

        //===========================================================
        // init google admob
        //===========================================================
        mAds.initAds();
        mAds.showInterAds();

        //===========================================================
        // init google map
        //===========================================================
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //===========================================================
        // init search frame element
        //===========================================================
        mBtnSearch.setOnClickListener( new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("btn", "search click");
                if(mMockRunning)
                {
                    showToast("Already running. Stop first.");
                    return;
                }

                if( setPosition() < 0 )
                {
                    return;
                }

                moveCamera();
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        Button btnStartWithMove = findViewById(R.id.btnStart);
        btnStartWithMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qlog.i("start move");
                if(mMockRunning)
                {
                    qutil.showToast(MainActivity.getIns(),"already running!!");
                    return;
                }
                startJoystick();
            }
        });
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if(mMockRunning)
        {
            stopJoystick();
        }
    }


    qBackPressExitApp mBack = new qBackPressExitApp(this);
    @Override
    public void onBackPressed()
    {
        mBack.onBackPressed();
    }

    public void showToast(String msg)
    {
        Toast t = Toast.makeText(this, msg, Toast.LENGTH_LONG);
        t.setGravity(Gravity.CENTER, 0, 0);
        t.show();
    }


    /**
     *
     * @return r == 0 : all permission ok <br>
     *     r < 0 : you need to add permission
     */
    private final int mPERMISSION_CODE_FINE_LOCATION = 1;
    private final int mPERMISSION_CODE_COARSE_LOCATION = 2;
    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 3;
    private final int mPERMISSION_CODE_EXTERNAL_STROAGE_WRITE = 4;
    private int checkPermission()
    {
        //===========================================================
        // check permission: gps
        //===========================================================
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            qutil.showDialog(this, "Need Permission", "Get GPS location", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.requestPermissions(MainActivity.getIns(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, mPERMISSION_CODE_FINE_LOCATION);
                }
            }, null);
        }
        else
        {
            activateFindMyLocationButton();
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            qutil.showDialog(this, "Need Permission", "Fake GPS location", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.requestPermissions(MainActivity.getIns(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, mPERMISSION_CODE_COARSE_LOCATION);
                }
            }, null);
        }

        //===========================================================
        // check permission: external storage
        //===========================================================
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
        {
            qutil.showDialog(this, "Need Permission", "For save favorite as file", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ActivityCompat.requestPermissions(MainActivity.getIns(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, mPERMISSION_CODE_EXTERNAL_STROAGE_WRITE);
                }
            }, null);
        }

        //===========================================================
        // check permission: overlay window
        //===========================================================
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마시멜로우 이상일 경우
            if (!Settings.canDrawOverlays(this)) {              // 체크
                qutil.showDialog(this, "Need Permission", "For joystick", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
                    }
                }, null);
            }
        }

        //===========================================================
        // check permission: mock gps
        //===========================================================
        if( !isMockLocationOn() )
        {
            qutil.showDialog(this, "Need Set Mock Location App", "설정 -> 개발자옵션 -> 모의위치앱 -> qHere", null, null);
            return -1;
        }

        return 0;
    }


    /**
     * 퍼미션 설정 창을 띄운 후 사용자 설정결과를 수신하는 함수.
     */
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // 동의를 얻지 못했을 경우의 처리
                //Log.d("", "not allowed overlay permission\n\n\n");
                //showToast("You MUST accept the DrawOverys permisstion!!");
            } else {
                //Log.d("", "start joystick service\n\n\n");
                //startService(new Intent(this, JoystickService.class));
                //showToast("NOW, You can move mock GPS!!");
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case mPERMISSION_CODE_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("", "permission on ACCESS_FINE_LOCATION");
                    activateFindMyLocationButton();
                } else {
                    Log.d("", "permission off ACCESS_FINE_LOCATION");
                }
                break;
            }
            case mPERMISSION_CODE_COARSE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("", "permission on ACCESS_COARSE_LOCATION");
                } else {
                    Log.d("", "permission off ACCESS_COARSE_LOCATION");
                }
                break;
            }
            case mPERMISSION_CODE_EXTERNAL_STROAGE_WRITE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("", "permission on EXTERNAL_STROAGE_WRITE");
                } else {
                    Log.d("", "permission off EXTERNAL_STROAGE_WRITE");
                }
                break;
            }
        }
    }


    /**
     * 개발자옵션 - 가상위치사용앱 이 설정되어 있는지 확인한다.
     */
    private boolean isMockLocationOn()
    {
        try {
            LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            if (lm != null)
            {
                lm.addTestProvider("check", false, true, false,
                        true, false, true, false,
                        Criteria.POWER_LOW, Criteria.ACCURACY_COARSE);
            }
        }
        catch (IllegalArgumentException e)
        {
            // activate mock location
            return true;
        }
        catch (SecurityException e)
        {
            // deactivate mock location
            return false;
        }

        return false;
    }


    /**
     * EditText 에 입력된 위경도로 마커를 이동.
     */
    private int setPosition()
    {
        String latLng = mEtSearch.getText().toString().replace(" ", "");
        Log.d("mEtSearch", "LatLng: " + latLng);

        if(!latLng.matches("^[-0-9].*[,].*[-0-9]$"))
        {
            showToast("Invalid LatLng. \nYou Must Input like this \n123.456,321.654");
            return -1;
        }

        String latitude = latLng.split(",")[0].trim();
        String longitude = latLng.split(",")[1].trim();

        double lat = Double.valueOf(latitude);
        double lng = Double.valueOf(longitude);
        mNewPosition = new LatLng(lat, lng);
        moveMarker();

        return 0;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapClickListener(this);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // 이거 동작 안해서 activateFindMyLocationButton() 수행
        //mMap.getUiSettings().setMyLocationButtonEnabled(true);
        //activateFindMyLocationButton();

        checkPermission();
    }


    /**
     * show find my location button in google map
     */
    private void activateFindMyLocationButton()
    {
        mMap.setMyLocationEnabled(true);
    }

    //===============================================================
    // User touch position
    //===============================================================
    private Marker mMarker;
    public LatLng mNewPosition;

    @Override
    public void onMapClick(LatLng latLng) {
        mNewPosition = latLng;
        moveMarker();
    }


    /**
     * mNewPosition 에 설정된 위경도로 marker 를 이동
     */
    public void moveMarker()
    {
        mEtSearch.clearFocus();

        if(mMarker != null)
        {
            mMarker.remove();
        }

        //String title = "Lat:" + mNewPosition.latitude + ", Lng:" + mNewPosition.longitude;
        String title = qutil.getDouble(mNewPosition.latitude, 4) + "," + qutil.getDouble(mNewPosition.longitude, 4);
        MarkerOptions mo = new MarkerOptions();
        mo.position(mNewPosition).title(title);

        mMarker = mMap.addMarker(mo);
        mMarker.showInfoWindow();

        // marker info window click listener
        mMap.setOnInfoWindowClickListener(infoWindowClickListener);

        showSubMenu();
    }


    /**
     *
     * @param lat : latitude
     * @param lng : longitude
     * @param title : info window title
     * @param snippet : info window content
     */
    public void addMarker(double lat, double lng, String title, String snippet, int id)
    {
        if(mMarker != null)
        {
            mMarker.remove();
        }

        MarkerOptions mo = new MarkerOptions();
        mo.position( new LatLng(lat, lng) )
                //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                .title(title).snippet(snippet);

        Bitmap bitmap = ((CustomAdapter)RadarActivity.mListViewPoke.getAdapter()).getItemById(id).image;
        mo.icon( BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bitmap, 130, 130, false)) );

        mMap.addMarker(mo);

        // marker info window click listener
        mMap.setOnInfoWindowClickListener(infoWindowClickListener);

        // marker click listener
        mMap.setOnMarkerClickListener(markerClickListener);
    }


    //===============================================================
    // marker click listener for radar
    // if click other marker, then remove marker added by user
    //===============================================================
    GoogleMap.OnMarkerClickListener markerClickListener = new GoogleMap.OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            if(mMockRunning)
            {
                //qutil.showToast(MainActivity.getIns(),"already running. you must stop!!");
                return false;
            }

            if(mMarker != null)
            {
                mMarker.remove();
            }

            mNewPosition = marker.getPosition();
            showSubMenu();

            return false;
        }
    };


    //===============================================================
    // marker info window click listener
    //===============================================================
    GoogleMap.OnInfoWindowClickListener infoWindowClickListener = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            //String latlng = marker.getPosition().latitude + "," + marker.getPosition().longitude;
            String latlng = qutil.getDouble(marker.getPosition().latitude, 4) + "," + qutil.getDouble(marker.getPosition().longitude, 4);
            qutil.showToast(ins, "copied " + latlng);

            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("latlng", latlng);
            clipboardManager.setPrimaryClip( clipData );
        }
    };


    private void moveCamera()
    {
        //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mNewPosition, 15));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mNewPosition, 17));
    }


    //===============================================================
    // start, stop button
    //===============================================================
    private LinearLayout mLayoutSubMenu;
    private LinearLayout mLayoutStop;


    private void showSubMenu()
    {
        if(mMockRunning)
        {
            //qlog.i("move GPS is running. skip show sub menu");
            return;
        }

        //===========================================================
        // move start button
        //===========================================================
//        Button btnStartWithMove = findViewById(R.id.btnStart);
//        btnStartWithMove.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                qlog.i("start move");
//                startJoystick();
//            }
//        });

        mLayoutSubMenu.setVisibility(View.VISIBLE);
    }


    public void invisibleSubMenu()
    {
//        mLayoutSubMenu.setVisibility(View.INVISIBLE);
        mLayoutSubMenu.setVisibility(View.GONE);
    }


    @SuppressLint("SetTextI18n")
    private void genStopButton()
    {
        Button button = findViewById(R.id.btnStop);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("button", "click");
                stopJoystick();
            }
        });

        mLayoutStop.setVisibility(View.VISIBLE);
    }


    public void startJoystick()
    {
        Log.d("", "start joystick");
        if( startMock() < 0 )
        {
            return;
        }

        startService(new Intent(this, JoystickService.class));
    }


    public void stopJoystick()
    {
        Log.d("", "stop joystick");
        stopMock();
        stopService(new Intent(this, JoystickService.class));
    }


    //===============================================================
    // mock location
    //===============================================================
    public MockUpdateGPSThread mMock;
    //FakeUpdateGPSThread mMock;


    /**
     *
     * @return r == 0 : ok
     *      r < 0 : fail
     */
    public int startMock()
    {
        if(checkPermission() < 0)
        {
            qlog.e("checkPermission() nok");
            return -1;
        }

        mAds.showInterAds();
        mMockRunning = true;

        moveMarker();
        moveCamera();

        mMock = new MockUpdateGPSThread(this);
        //mMock = new FakeUpdateGPSThread(this);
        mMock.setLocation(mNewPosition);
        mMock.start();

        mLayoutSubMenu.setVisibility(View.INVISIBLE);
        genStopButton();

        return 0;
    }


    public void stopMock()
    {
        if(!mMockRunning)
        {
            Log.d("", "is not running mock location");
            return;
        }

        stopWalk();

        mMockRunning = false;
        mBtnSearch.setClickable(true);

        mMock.Running = false;
        mMock.interrupt();
        mMock = null;

        mLayoutStop.setVisibility(View.INVISIBLE);
        mLayoutSubMenu.setVisibility(View.VISIBLE);
    }


    public void setMockLoc()
    {
        if(mMock == null)
        {
            return;
        }

        mMock.setLocation(mNewPosition);
        moveMarker();
    }


    //===============================================================
    // change activity
    //===============================================================
    public static ArrayList<Poke> mListPoke = new ArrayList<>();

    public void onGoRadarActivity(View v)
    {
        Intent intent = new Intent(this, RadarActivity.class);
        startActivity(intent);
    }


    public void onGoDictionary(View view)
    {
        String url = "https://pokemon.gameinfo.io/ko/pokemon/list/best-pokemon-by-cp";

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        view.getContext().startActivity(intent);
    }


    public void onFavorite(View view)
    {
        Intent intent = new Intent(this, FavoriteActivity.class);
        startActivity(intent);
    }


    public void onWalk(View view)
    {
        Intent intent = new Intent(this, WalkActivity.class);
        startActivity(intent);
    }

    /**
     *
     * @param site : nation, region, site ...
     */
    public static final int mLIST_POKE_MAX = 100;
    public void drawPokeListInMap(int site)
    {
        mMap.clear();

        qlog.i("drawPokeListInMap() mListPoke.size(): " + mListPoke.size());
        if(mListPoke.size() > mLIST_POKE_MAX)
        {
            qutil.showToast(this, "Too many result. Will show " + mLIST_POKE_MAX + " limit.");

            for(int i = mListPoke.size() - 1 ; i > mLIST_POKE_MAX ; i--)
            {
                qlog.i("drawPokeListInMap() delete index : " + i);
                mListPoke.remove(i);
            }
        }

        for(Poke p : mListPoke)
        {
            qlog.i(p.toStr());
            String disapearHHmm = qutil.unixtimeToHourMin(p.mDespawn);
            String title = p.mID + "  " + p.mName + "  LV:" + p.mLevel + "  CP:" + p.mCP;
            String snippet = "ATT:" + p.mAtt + "  DEF:" + p.mDef + "  HP:" + p.mHp + "  " + disapearHHmm;
            addMarker(p.mLat, p.mLng, title, snippet, p.mID);
        }

        //LatLng camera = new LatLng(37.521938, 126.981117);
        LatLng camera = new LatLng(1.3565, 103.871);
        if(site == RadarActivity.mNEWYORK)
        {
            camera = new LatLng(40.695842, -73.946729);
        }
        else if(site == RadarActivity.mRONDON)
        {
            camera = new LatLng(51.513818, -0.115680);
        }

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(camera, 10));
    }


    public static void printListPoke()
    {
        for(Poke p : mListPoke)
        {
            qlog.i( p.toStr() );
        }
    }


    //===============================================================
    // chat service
    //===============================================================
    private Button mBtnTalk;


    private void initChatService()
    {
        mBtnTalk = findViewById(R.id.btnTalk);
    }


    public void onJoin(View v)
    {
        mAds.showInterAds();
        startChatService();
    }


    private void startChatService()
    {
        Log.d("startChatService()", "start");
        mBtnTalk.setEnabled(false);

        // start service
        String roomname = "PokemonGo";

        int num = randomRange(1, 1000);
        String nickname = "player" + num;

        Intent intent = new Intent(this, ChatService.class);
        intent.putExtra("NickName", nickname);
        intent.putExtra("RoomName", roomname);
        startService(intent);

        // home 버튼 클릭한 것 처럼 activity 숨기기
//        Intent intentHome = new Intent();
//        intentHome.setAction("android.intent.action.MAIN");
//        intentHome.addCategory("android.intent.category.HOME");
//        intentHome.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
//                | Intent.FLAG_ACTIVITY_FORWARD_RESULT
//                | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP
//                | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
//        startActivity(intentHome);
    }


    // 지정된 범위의 정수 1개를 램덤하게 반환하는 메서드
    // n1 은 "하한값", n2 는 상한값
    public static int randomRange(int n1, int n2)
    {
        return (int) (Math.random() * (n2 - n1 + 1)) + n1;
    }


    public void stopChatService()
    {
        Log.i("stopChatService()", "stop");
        mBtnTalk.setEnabled(true);

        // stop service
        Intent intent = new Intent(this, ChatService.class);
        stopService(intent);
    }


    //===============================================================
    // 현재 위치 정보를 구해오는 함수 (포켓몬고와 동일한 방식)
    //===============================================================
    private FusedLocationProviderClient mFusedLocationClient;
    public Location mCurrentLocation;
    public boolean mIsMockLoc = true;
    public float mAccuracy = 9999;

    @SuppressLint("MissingPermission")
    public Location getCurrentLocation()
    {
        OnCompleteListener<Location> mCompleteListener = new OnCompleteListener<Location>()
        {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if( task.isSuccessful() && task.getResult() != null )
                {
                    mCurrentLocation = task.getResult();
                    if( mMock != null ) {
                        mMock.setCurrentLocation(mCurrentLocation);
                    }
//                    qlog.e("provider: " + mCurrentLocation.getProvider()
//                            + ", lat: " + mCurrentLocation.getLatitude()
//                            + ", lng: " + mCurrentLocation.getLongitude()
//                            + ", accuracy: " + mCurrentLocation.getAccuracy() );

                    mAccuracy = mCurrentLocation.getAccuracy();

                    if(mCurrentLocation.isFromMockProvider())
                    {
                        mIsMockLoc = true;
                    }
                    else
                    {
                        mIsMockLoc = false;
                    }
                }
                else
                {
                    mAccuracy = 9999;
                    if(task != null) {
                        qlog.e("getCurrentLocation() fail: " + task.getException().getMessage());
                    }
                }
            }
        };

        mFusedLocationClient.getLastLocation().addOnCompleteListener(this, mCompleteListener);
        return mCurrentLocation;
    }


    //===============================================================
    // walk 관련 기능
    //===============================================================
    public ArrayList<String> mListPosition = new ArrayList<>();
    public int mInterval = 3;
    public WalkThread mWalkThread = null;


    public void startWalk()
    {
        if(mWalkThread != null)
        {
            return;
        }

        mWalkThread = new WalkThread();
        mWalkThread.start();
    }


    public void stopWalk()
    {
        if(mWalkThread == null)
        {
            return;
        }

        mWalkThread.interrupt();
        mWalkThread = null;
    }

}
