package com.example.admin.ghr;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.qrcode.encoder.QRCode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VictimMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {


        public boolean onCreateOptionsMenu(Menu menu) {
                getMenuInflater().inflate(R.menu.rr, menu);
                // Action View
                //MenuItem searchItem = menu.findItem(R.id.action_search);
                //SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
                // Configure the search info and add any event listeners
                //return super.onCreateOptionsMenu(menu);
                return true;

        }


        @Override
        public boolean onOptionsItemSelected(MenuItem item) {


                switch (item.getItemId()) {


                        case R.id.Home: {
                                Intent intent=new Intent(VictimMapActivity.this,VictimMapActivity.class);
                                startActivity(intent);
                                break;
                        }




                        case R.id.hospital: {
                                Intent intent=new Intent(VictimMapActivity.this,VictimHospitalActivity.class);
                                startActivity(intent);
                                break;
                        }
                        case R.id.camera: {
                                //do somthing
                                Intent cameraIntent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                                //     cameraIntent.setType("image/*");
                                startActivityForResult(cameraIntent,CAMERA_REQUEST_CODE);
                                break;
                        }
                        case R.id.info: {
                                if ( !driverFound ){
                                        mAmbulanceInfo.setVisibility(View.GONE);
                                        Toast.makeText(VictimMapActivity.this,"There are no Ambulance connected to you",Toast.LENGTH_LONG).show();
                                }
                                else {
                                        if(open==false){
                                                getAssignedAmbulanceInfo();
                                        }
                                        else {
                                                mAmbulanceInfo.setVisibility(View.GONE);
                                                open=false;
                                        }
                                }
                                break;
                        }
                        case R.id.setting: {
                                Intent intent=new Intent(VictimMapActivity.this,VictimSettingActivity.class);
                                startActivity(intent);
                                break;
                        }
                        case R.id.logout: {
                                isLoggingout=true;

                                DatabaseReference        gg = FirebaseDatabase.getInstance().getReference().child("Users").child("Victim").child(userID).child("VictimImageUrl");
                                gg.removeValue();

                                FirebaseAuth.getInstance().signOut();
                                Intent intent= new Intent(VictimMapActivity.this,MainActivity.class);
                                startActivity(intent);
                                finish();
                               break;




                        }

                        case R.id.Scanner: {
                                Intent intent=new Intent(VictimMapActivity.this,ScannerQr.class);
                                startActivity(intent);
                                break;
                        }

                        case R.id.QRCODE: {
                                Intent intent=new Intent(VictimMapActivity.this,GenerateQr.class);
                                startActivity(intent);
                                break;
                        }

                }


                return super.onOptionsItemSelected(item);

        }







        private GoogleMap mMap;
        GoogleApiClient mGoogleApiClient;
        Location mLastLocation;
        LocationRequest mLocationRequest;
private Button mLogOut,mRequest;
private ImageView mtwitter;
private LatLng pickUp;
private Boolean requestBol=false;
private Marker pickUPMarker;
private Button mSetting,mInfo;
public static final int REQUEST_LOCATION_CODE=99;
private Button mHospital;
double latitude,longitude;
private ImageView mcamera;
int PROXIMITY_RADIUS=10000;
private Boolean isLoggingout=false;
private int infoClicked=0;
private int oncesent=0;
private String RelativePhone;
private LinearLayout mAmbulanceInfo;
private ImageView mAmbulanceProfileImage;
private TextView mAmbulanceName,mAmbulancePhone,mAmbulanceNo,mAmbulanceHospital;
private RadioGroup mRadioGroup;
private String mService,ambulancephone;
private boolean open=false;
String stname="",stphone="",stphone1="",stphone2="",stallergy="",stblood_group="";



        private Button mCapture;
        //private ImageView mcamera1;
        public static final int CAMERA_REQUEST_CODE=1;
        //  private Button mUpload;
        // private StorageReference mStorage;
        private ProgressDialog mProgressDialog;
        private Uri uri;
        private Uri resultUri1;

        private FirebaseAuth mAuth;
        private DatabaseReference mCustomerDatabase;

        private String userID;





@Override
protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_victim_map);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                checkLocationPermission();
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //  mLogOut=(Button)findViewById(R.id.logout);
   /*     mLogOut.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
                        isLoggingout=true;

      DatabaseReference        gg = FirebaseDatabase.getInstance().getReference().child("Users").child("Victim").child(userID).child("VictimImageUrl");
        gg.removeValue();

                FirebaseAuth.getInstance().signOut();
                        Intent intent= new Intent(VictimMapActivity.this,MainActivity.class);
                        startActivity(intent);
                        finish();
                        return;
                }
        });  */


        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Victim").child(userID);

        // mCapture=(Button)findViewById(R.id.capture);
        //mImageView=(ImageView)findViewById(R.id.imageView);
        // mUpload=(Button)findViewById(R.id.upload);
        //      mStorage= FirebaseStorage.getInstance().getReference();
        mProgressDialog = new ProgressDialog(this);


        mRadioGroup = (RadioGroup) findViewById(R.id.radioGroup);


        mAmbulanceInfo = (LinearLayout) findViewById(R.id.ambulance_info);
        mAmbulanceProfileImage = (ImageView) findViewById(R.id.ambulanceProfileImage);
        mAmbulanceName = (TextView) findViewById(R.id.ambulanceName);
        mAmbulancePhone = (TextView) findViewById(R.id.ambulancePhone);
        mAmbulanceNo = (TextView) findViewById(R.id.ambulanceNo);
        mAmbulanceHospital = (TextView) findViewById(R.id.ambulanceHospital);

        mRequest = (Button) findViewById(R.id.request);
        mRequest.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                       // messagerelative();
                        if (requestBol) {//cancelling the ambulance......................................
                                requestBol = false;
                                geoQuery.removeAllListeners();
                                driverLocationRef.removeEventListener(driverLocationRefListner);
                                if (driverId != null) {
                                        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Ambulance").child(driverId);
                                        driverRef.child("victimRideId").setValue("");
                                        driverId = null;
                                }
                                driverFound = false;
                                radius = 1;
                                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("VictimRequest");
                                ref.child("Caller").removeValue();
                                ref.child("name").removeValue();
                                ref.child("phone").removeValue();
                                ref.child("phone1").removeValue();
                                ref.child("phone2").removeValue();
                                ref.child("allergy").removeValue();
                                ref.child("bloodGroup").removeValue();
                                GeoFire geoFire = new GeoFire(ref);
                                geoFire.removeLocation(userId);
                                if (pickUPMarker != null) {
                                        pickUPMarker.remove();
                                        mdriverMarker.remove();
                                }

                                mAmbulanceNo.setText("");
                                mAmbulanceHospital.setText("");
                                mAmbulancePhone.setText("");
                                mAmbulanceName.setText("");
                                mAmbulanceProfileImage.setImageResource(R.mipmap.ic_launcher_user);
                                mRequest.setText("Call Ambulance");
                        }//..........................................................................
                        else {//calling ambulance........................................................
                                requestBol = true;
                                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("VictimRequest");
                                GeoFire geoFire = new GeoFire(ref);
                                geoFire.setLocation(userId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                                int selectId = mRadioGroup.getCheckedRadioButtonId();
                                final RadioButton radioButton = (RadioButton) findViewById(selectId);
                                mService = radioButton.getText().toString();
                                DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference("VictimRequest").child(userId);
                                ref1.child("Caller").setValue(mService);
                                if(mService.equals("Stranger")){
                                        Intent i=getIntent();
                                        Bundle bundle=i.getExtras();
                                        stname=bundle.getString("NAME");
                                        stphone=bundle.getString("PHONE");
                                        stphone1=bundle.getString("PHONE1");
                                        stphone2=bundle.getString("PHONE2");
                                        stallergy=bundle.getString("ALLERGY");
                                        stblood_group=bundle.getString("BG");
                                        ref1.child("name").setValue(stname);
                                        ref1.child("phone").setValue(stphone);
                                        ref1.child("phone1").setValue(stphone1);
                                        ref1.child("phone2").setValue(stphone2);
                                        ref1.child("allergy").setValue(stallergy);
                                        ref1.child("bloodGroup").setValue(stblood_group);


                                }
                                pickUp = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                                pickUPMarker = mMap.addMarker(new MarkerOptions().position(pickUp).title("Pick Up Here").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_victim)));
                                mRequest.setText("Getting Ambulance");
                                getClosestAmbulance();
                        }//...............................................................................

                }
        });

        DatabaseReference ref12=FirebaseDatabase.getInstance().getReference().child("VictimRequest").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        ref12.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                        if(!dataSnapshot.exists() && requestBol==true){
                                requestBol = false;
                                geoQuery.removeAllListeners();
                                driverLocationRef.removeEventListener(driverLocationRefListner);
                                if (driverId != null) {
                                        DatabaseReference driverRef = FirebaseDatabase.getInstance().getReference().child("Users").child("Ambulance").child(driverId);
                                        driverRef.child("victimRideId").setValue("");
                                        driverId = null;
                                }
                                driverFound = false;
                                radius = 1;
                                String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("VictimRequest");
                                ref.child("Caller").removeValue();
                                ref.child("name").removeValue();
                                ref.child("phone").removeValue();
                                ref.child("phone1").removeValue();
                                ref.child("phone2").removeValue();
                                ref.child("allergy").removeValue();
                                ref.child("bloodGroup").removeValue();
                                GeoFire geoFire = new GeoFire(ref);
                                geoFire.removeLocation(userId);
                                if (pickUPMarker != null) {
                                        pickUPMarker.remove();
                                        mdriverMarker.remove();
                                }

                                mAmbulanceNo.setText("");
                                mAmbulanceHospital.setText("");
                                mAmbulancePhone.setText("");
                                mAmbulanceName.setText("");
                                mAmbulanceProfileImage.setImageResource(R.mipmap.ic_launcher_user);
                                mRequest.setText("Call Ambulance");
                        }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
        });
  /*     mInfo=(Button)findViewById(R.id.info);
        mInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        if ( !driverFound ){
                                mAmbulanceInfo.setVisibility(View.GONE);
                                Toast.makeText(VictimMapActivity.this,"There are no Ambulance connected to you",Toast.LENGTH_LONG).show();
                        }
                        else {
                                if(open==false){
                                        getAssignedAmbulanceInfo();
                                }
                                else {
                                        mAmbulanceInfo.setVisibility(View.GONE);
                                        open=false;
                                }
                        }

                }
        }); */


        mcamera = (ImageView) findViewById(R.id.camera);
        mcamera.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        //     cameraIntent.setType("image/*");
                        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);


                }
        });

        mtwitter = (ImageView) findViewById(R.id.twitter);
        mtwitter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                        String url1 = "https://www.google.com/maps/search/?api=1&query=";
                        String sms = "I am in danger\n"+"Ambulance phone no.:"+ambulancephone+"\n"+"my location: "+url1+mLastLocation.getLatitude()+","+mLastLocation.getLongitude();

                        //  String message="I am in danger\\n\"+\"Ambulance phone no.:\"+ambulancephone+\"\\n\"+\"my location: \"+url+mLastLocation.getLatitude()+\",\"+mLastLocation.getLongitude();\n"
                        String url = "http://www.twitter.com/intent/tweet?url=.&text="+sms;
                        Intent i = new Intent(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(url));
                        startActivity(i);



                }
        });


















        //    mHospital=(Button)findViewById(R.id.hospital);
       // mHospital.setOnClickListener(new View.OnClickListener() {
             //   @Override
      //          public void onClick(View view) {
                     //   Intent intent=new Intent(VictimMapActivity.this,VictimHospitalActivity.class);
                     //   startActivity(intent);
                   //     return;
               // }
     //   });

    //    mSetting=(Button)findViewById(R.id.setting);
    //    mSetting.setOnClickListener(new View.OnClickListener() {
     //    //       @Override
           //     public void onClick(View view) {
       //                 Intent intent=new Intent(VictimMapActivity.this,VictimSettingActivity.class);
            //            startActivity(intent);
           //             return;
             //   }
     //   });

       }

        private void getAssignedAmbulanceInfo() {
                mAmbulanceInfo.setVisibility(View.VISIBLE);
                open=true;
                DatabaseReference mAmbulanceDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child("Ambulance").child(driverId);
                mAmbulanceDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                                        Map<String,Object> map=(Map<String,Object>)dataSnapshot.getValue();
                                        if(map.get("name")!=null){
                                                mAmbulanceName.setText(map.get("name").toString());
                                        }
                                        if(map.get("phone")!=null){
                                                ambulancephone=map.get("phone").toString();
                                                mAmbulancePhone.setText(map.get("phone").toString());
                                        }
                                        if(map.get("ambulance_no")!=null){
                                                mAmbulanceNo.setText(map.get("ambulance_no").toString());
                                        }
                                        if(map.get("hospital")!=null){
                                                mAmbulanceHospital.setText(map.get("hospital").toString());
                                        }
                                        if(map.get("profileImageUrl")!=null){
                                                Glide.with(getApplicationContext()).load(map.get("profileImageUrl").toString()).into(mAmbulanceProfileImage);
                                        }

                                }
                                messagerelative();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                });
        }


        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
                switch(requestCode){
                        case REQUEST_LOCATION_CODE:
                                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                                        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                                                if(mGoogleApiClient==null){
                                                        buildGoogleApiClient();
                                                }
                                                mMap.setMyLocationEnabled(true);
                                        }
                                }
                                else{
                                        Toast.makeText(this,"Permission Denied",Toast.LENGTH_LONG).show();
                                }
                                return;
                }
        }





        private int radius=1;
        private Boolean driverFound=false;
        private String driverId;

        GeoQuery geoQuery;
        private void getClosestAmbulance() {
                DatabaseReference ambulanceLocation=FirebaseDatabase.getInstance().getReference().child("AmbulanceAvailable");
                GeoFire geoFire=new GeoFire(ambulanceLocation);

                geoQuery=geoFire.queryAtLocation(new GeoLocation(pickUp.latitude,pickUp.longitude),radius);
                geoQuery.removeAllListeners();

                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                        @Override
                        public void onKeyEntered(String key, GeoLocation location) {
                                if(driverFound==false && requestBol){
                                        driverFound=true;
                                        driverId=key;
                                        getAssignedAmbulanceInfo();

                                        DatabaseReference driverRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Ambulance").child(driverId);
                                        String customerId=FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        driverRef.child("victimRideId").setValue(customerId);

                                        getDriverLocation();
                                        mRequest.setText("Looking for driver's location");

                                }
                        }

                        @Override
                        public void onKeyExited(String key) {

                        }

                        @Override
                        public void onKeyMoved(String key, GeoLocation location) {

                        }

                        @Override
                        public void onGeoQueryReady() {
                                if(driverFound==false){
                                        radius++;
                                        getClosestAmbulance();
                                }
                        }

                        @Override
                        public void onGeoQueryError(DatabaseError error) {

                        }
                });
        }


        private Marker mdriverMarker;
        private DatabaseReference driverLocationRef;
        private ValueEventListener driverLocationRefListner;

        private void getDriverLocation() {
                driverLocationRef = FirebaseDatabase.getInstance().getReference().child("AmbulanceWorking").child(driverId).child("l");
                driverLocationRefListner = driverLocationRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                        List<Object> map= (List<Object>) dataSnapshot.getValue();
                                        double LocationLat=0;
                                        double LocationLng=0;
                                        mRequest.setText("Ambulance Found");
                                        if(map.get(0)!=null){
                                                LocationLat=Double.parseDouble(map.get(0).toString());
                                        }
                                        if(map.get(1)!=null){
                                                LocationLng=Double.parseDouble(map.get(1).toString());
                                        }
                                       LatLng driverLatLng= new LatLng(LocationLat,LocationLng);
                                        if(mdriverMarker!=null){
                                                mdriverMarker.remove();
                                        }

                                        Location loc1=new Location("");
                                        loc1.setLatitude(pickUp.latitude);
                                        loc1.setLongitude(pickUp.longitude);

                                        Location loc2=new Location("");
                                        loc2.setLatitude(driverLatLng.latitude);
                                        loc2.setLongitude(driverLatLng.longitude);

                                        float distance=loc1.distanceTo(loc2);

                                        if(distance<100){
                                                mRequest.setText("Ambulance is Here");
                                        }else{
                                                mRequest.setText("Ambulance Found, Distance:"+String.valueOf(distance));
                                        }

                                        mdriverMarker=mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your Ambulance").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_ambulance)));

                                }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                });
        }


        @Override
public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
                if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        buildGoogleApiClient();
                        mMap.setMyLocationEnabled(true);
                }

        }

protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient=new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API)
        .build();
        mGoogleApiClient.connect();


        }
@Override
public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,mLocationRequest,this);
        }
        }

@Override
public void onLocationChanged(Location location) {
        mLastLocation = location;
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        LatLng latlng=new LatLng(location.getLatitude(),location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18));

        if(mGoogleApiClient!=null){
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
        }
        }



public boolean checkLocationPermission(){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)){
                        ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_CODE);
                }
                else {
                        ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},REQUEST_LOCATION_CODE);
                }
                return false;
        }
        else
                return true;
}


@Override
public void onConnectionSuspended(int i) {

        }

@Override
public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        }

        private void disconnectDriver(){
               // String userid= FirebaseAuth.getInstance().getCurrentUser().getUid();

               // DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("VictimRequest");
               // ref.child(userid).child("l").child("0").removeValue();
               // ref.child(userid).child("l").child("1").removeValue();
               // ref.child(userid).child("g").removeValue();
               // GeoFire geofire=new GeoFire(ref);
               // geofire.removeLocation(userid);
        }

        @Override
        protected void onStop() {
                super.onStop();
                if(!isLoggingout){
                        disconnectDriver();
                }


        }




        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
                super.onActivityResult(requestCode, resultCode, data);

                if(resultCode==RESULT_OK){
                        if(requestCode==CAMERA_REQUEST_CODE ){


                                Bitmap cameraImage =(Bitmap) data.getExtras().get("data");
                                //   mImageView.setImageBitmap(cameraImage);

                                uri=getImageUri(VictimMapActivity.this,cameraImage);
                                uri=data.getData();
                                uri  = getImageUri(getApplicationContext(),cameraImage );
                                // Toast.makeText(Camera.this,"Here "+ getRealPathFromURI(uri),Toast.LENGTH_LONG).show();

                                mProgressDialog.setMessage("Uploading..");
                                mProgressDialog.show();

                                //     Uri imageUri = data.getData();
                                resultUri1 = uri;
                                // mcamera1.setImageURI(resultUri1);











                                StorageReference filePath = FirebaseStorage.getInstance().getReference().child("Victimimages").child(userID);
                                Bitmap bitmap = null;
                                try {
                                        bitmap = MediaStore.Images.Media.getBitmap(getApplication().getContentResolver(), resultUri1);
                                } catch (IOException e) {
                                        e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                                byte[] data1 = baos.toByteArray();
                                UploadTask uploadTask = filePath.putBytes(data1);

                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                                finish();
                                                return;
                                        }
                                });
                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                Uri downloadUrl = taskSnapshot.getDownloadUrl();

                                                Map newImage = new HashMap();
                                                newImage.put("VictimImageUrl", downloadUrl.toString());
                                                mCustomerDatabase.updateChildren(newImage);

                                                finish();
                                                //    return;
                                                Intent intent = new Intent(VictimMapActivity.this, VictimMapActivity.class);
                                                startActivity(intent);
                                        }
                                });




                        }
                }
        }
        public Uri getImageUri(Context inContext, Bitmap inImage) {
                //  ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                //  inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
                String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
                return Uri.parse(path);
        }


        private void messagerelative() {
                String customerId = FirebaseAuth.getInstance().getCurrentUser().getUid();


                //  final String relative="9167567496";
                // final String relative1="9702904658";
                // final String relative2="9167479135";


                if (customerId != null  &&  oncesent==0) {




                        DatabaseReference gg = FirebaseDatabase.getInstance().getReference().child("Users").child("Victim").child(customerId);
                        gg.addValueEventListener(new ValueEventListener() {


                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                        String name=null;
                                        //String url="https://www.google.com/maps/search/?api=1&query=";

                                        String url = "https://www.google.com/maps/search/?api=1&query=";

                                        name=dataSnapshot.child("name").getValue(String.class);
                                        RelativePhone=dataSnapshot.child("phone1").getValue(String.class);
                                        if(mService.equals("Stranger")){
                                                RelativePhone=stphone1;
                                                name=stname;
                                        }

                                                String sms = "I, "+name+" am in danger\n"+"Ambulance Phone No.:"+ambulancephone+"\n"+"my location: "+url+mLastLocation.getLatitude()+","+mLastLocation.getLongitude();

                                        try {
                                                SmsManager smsManager = SmsManager.getDefault();
                                                smsManager.sendTextMessage(RelativePhone, null, sms, null, null);
                                                //  smsManager.sendTextMessage(relative, null, sms, null, null);
                                                //  smsManager.sendTextMessage(relative1, null, sms, null, null);
                                                //  smsManager.sendTextMessage(relative2, null, sms, null, null);
                                                Toast.makeText(getApplicationContext(), "SMS Sent!",

                                                        Toast.LENGTH_LONG).show();
                                                oncesent=1;
                                        } catch (Exception e) {
                                                Toast.makeText(getApplicationContext(),
                                                        "SMS faild, please try again later!",
                                                        Toast.LENGTH_LONG).show();
                                                e.printStackTrace();
                                        }


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                        });



                        DatabaseReference gg1 = FirebaseDatabase.getInstance().getReference().child("Users").child("Victim").child(customerId);
                        gg1.addValueEventListener(new ValueEventListener() {


                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                        //String url="https://www.google.com/maps/search/?api=1&query=";
                                        String name=null;
                                        String url = "https://www.google.com/maps/search/?api=1&query=";


                                        RelativePhone=dataSnapshot.child("phone2").getValue(String.class);
                                        name=dataSnapshot.child("name").getValue(String.class);
                                        if(mService.equals("Stranger")){
                                                RelativePhone=stphone2;
                                                name=stname;
                                        }
                                        String sms = "I ," +name+" am in danger\n"+"Ambulance phone no.:"+ambulancephone+"\n"+"my location: "+url+mLastLocation.getLatitude()+","+mLastLocation.getLongitude();

                                        try {
                                                SmsManager smsManager = SmsManager.getDefault();
                                                smsManager.sendTextMessage(RelativePhone, null, sms, null, null);
                                                //  smsManager.sendTextMessage(relative, null, sms, null, null);
                                                //  smsManager.sendTextMessage(relative1, null, sms, null, null);
                                                //  smsManager.sendTextMessage(relative2, null, sms, null, null);
                                                Toast.makeText(getApplicationContext(), "SMS Sent!",

                                                        Toast.LENGTH_LONG).show();
                                                oncesent=1;
                                        } catch (Exception e) {
                                                Toast.makeText(getApplicationContext(),
                                                        "SMS faild, please try again later!",
                                                        Toast.LENGTH_LONG).show();
                                                e.printStackTrace();
                                        }


                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                }
                        });






                }






        }



}
