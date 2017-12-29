package com.qthekan.qhere;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.qthekan.qhere.joystick.JoystickService;
import com.qthekan.util.qBackPressExitApp;
import com.qthekan.util.qlog;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private static MapsActivity ins = null;

    private GoogleMap mMap;

    private Button mBtnSearch;
    private EditText mEtSearch;

    // mock location is running
    private boolean mMockRunning;
    private AdsMgr mAds;

    private static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 3;


    public static MapsActivity getIns()
    {
        return ins;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ins = this;
        mMockRunning = false;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        //===========================================================
        // init google admob
        //===========================================================
        mAds = new AdsMgr(this, (AdView) findViewById(R.id.adView));
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
        mEtSearch = findViewById(R.id.etSearch);
        mBtnSearch = findViewById(R.id.btnSearch);
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

    private void showToast(String msg)
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
    private int checkPermission()
    {
        //===========================================================
        // check permission: gps
        //===========================================================
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) )
            {
                Log.d("", "user reject permission ACCESS_FINE_LOCATION");
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
        else
        {
            activateFindMyLocationButton();
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION) )
            {
                Log.d("", "user reject permission ACCESS_COARSE_LOCATION");
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
            }
        }

        //===========================================================
        // check permission: overlay window
        //===========================================================
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {   // 마시멜로우 이상일 경우
            if (!Settings.canDrawOverlays(this)) {              // 체크
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
            }
        }

        //===========================================================
        // check permission: mock gps
        //===========================================================
        if( !isMockLocationOn() )
        {
            showToast("You MUST set MOCK LOCATION!!\n Developer option\n -> Mock location app\n -> qHere");
            return -1;
        }

        return 0;
    }


    /**
     * 퍼미션 설정 창을 띄운 후 사용자 설정결과를 수신하는 함수.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                // 동의를 얻지 못했을 경우의 처리
                //Log.d("", "not allowed overlay permission\n\n\n");
                showToast("You MUST accept the DrawOverys permisstion!!");
            } else {
                //Log.d("", "start joystick service\n\n\n");
                //startService(new Intent(this, JoystickService.class));
                showToast("NOW, You can move mock GPS!!");
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1: {
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
            case 2: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d("", "permission on ACCESS_COARSE_LOCATION");
                } else {
                    Log.d("", "permission off ACCESS_COARSE_LOCATION");
                }
                break;
            }
        }
    }


    /**
     * 개발자옵션 - 가상위치사용앱 이 설정되어 있는지 확인한다.
     * @return
     */
    private boolean isMockLocationOn()
    {
        try {
            LocationManager lm = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
            lm.addTestProvider("check", false, true, false, true, false, true, false, Criteria.POWER_LOW, Criteria.ACCURACY_COARSE);
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
     * @return
     */
    private int setPosition()
    {
        String latLng = mEtSearch.getText().toString();
        Log.d("mEtSearch", "LatLng: " + latLng);

        if(!latLng.matches("^[0-9].*[,].*[0-9]$"))
        {
            showToast("Invalid LatLng. \nYou Must Input like this \n123.456,321.654");
            return -1;
        }

        String latitude = latLng.split(",")[0];
        String longitude = latLng.split(",")[1];

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
    private void moveMarker()
    {
        mEtSearch.clearFocus();

        if(mMarker != null)
        {
            mMarker.remove();
        }

        String title = "Lat:" + mNewPosition.latitude + ", Lng:" + mNewPosition.longitude;
        MarkerOptions mo = new MarkerOptions();
        mo.position(mNewPosition).title(title);

        mMarker = mMap.addMarker(mo);
        //mMarker.showInfoWindow();

        showSubMenu();
    }


    private void moveCamera()
    {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(mNewPosition, 15));
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
            qlog.i("move GPS is running. skip show sub menu");
            return;
        }

        mLayoutSubMenu = findViewById(R.id.viewSubMenu);

        /**
         * stand start button
         */
        Button btnStartWithLocation = findViewById(R.id.btnStand);
        btnStartWithLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qlog.i("start stand");
                startMock();
            }
        });

        /**
         * move start button
         */
        Button btnStartWithMove = findViewById(R.id.btnMove);
        btnStartWithMove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                qlog.i("start move");
                startJoystick();
            }
        });

        mLayoutSubMenu.setVisibility(View.VISIBLE);
    }


    @SuppressLint("SetTextI18n")
    private void genStopButton()
    {
        mLayoutStop = findViewById(R.id.viewStop);

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


    private void startJoystick()
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
    private int startMock()
    {
        if(checkPermission() < 0)
        {
            qlog.e("checkPermission() nok");
            return -1;
        }

        mAds.showInterAds();
        mMockRunning = true;

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
        if(mMockRunning == false)
        {
            Log.d("", "is not running mock location");
            return;
        }

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




}
