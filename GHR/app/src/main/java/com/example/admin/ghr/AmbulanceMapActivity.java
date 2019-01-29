package com.example.admin.ghr;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AmbulanceMapActivity extends AppCompatActivity implements OnMapReadyCallback,RoutingListener,GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.yy, menu);
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
                Intent intent=new Intent(this,AmbulanceMapActivity.class);
                startActivity(intent);
                break;
            }




            case R.id.hospital: {
                Intent intent=new Intent(this,HospitalMapActivity.class);
                startActivity(intent);
                break;
            }
           // case R.id.camera: {
                //do somthing
              //  Intent cameraIntent =new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //     cameraIntent.setType("image/*");
              //  startActivityForResult(cameraIntent,CAMERA_REQUEST_CODE);
              //  break;
          //  }
            case R.id.info: {
                if ( !victimFound ){
                    mVictimInfo.setVisibility(View.GONE);
                    Toast.makeText(this,"There are no Victim connected to you",Toast.LENGTH_LONG).show();
                }
                else {
                    if(open==false){
                       getAssignedCustomerInfo();
                    }
                    else {
                        mVictimInfo.setVisibility(View.GONE);
                        open=false;
                    }
                }
                break;
            }
            case R.id.setting: {
                Intent intent=new Intent(this,AmbulanceSettingActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.logout: {
                isLoggingout=true;


                disconnectDriver();
                FirebaseAuth.getInstance().signOut();
                Intent intent= new Intent(this,MainActivity.class);
                startActivity(intent);
                finish();
                break;




            }

            case R.id.information: {
                Intent intent = new Intent(this,MyPage.class);
                // intent.putExtra(customerId1,customerId.);
                Bundle bundle=new Bundle();
                bundle.putString("customerId",customerId);
                intent.putExtras(bundle);
                startActivity(intent);
             break;



            }



          /*  case R.id.Scanner: {
                Intent intent=new Intent(VictimMapActivity.this,ScannerQr.class);
                startActivity(intent);
                break;
            }

            case R.id.QRCODE: {
                Intent intent=new Intent(VictimMapActivity.this,GenerateQr.class);
                startActivity(intent);
                break;
            }  */

        }


        return super.onOptionsItemSelected(item);

    }






    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private Button mLogOut,mInfo, minfo1, mRideStatus;
    private String customerId="";
    private Marker mdriverMarker;
    private int status = 0;
    private int infoClicked=0;
    private Button mSetting,mHospital;
    private LatLng destinationLatLng;
    private String mCaller="Self";
    private boolean victimFound=false;
    private LatLng pickUpLatLng;
    private boolean open=false;
    private LinearLayout mVictimInfo;
    private ImageView mVictimProfileImage;
    private TextView mVictimName,mVictimPhone,mVictimPhone1,mVictimPhone2,mVictimBG,mVictimAllergy,mVictimType;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.primary_dark_material_light};


    private Boolean isLoggingout=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {








        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambulance_map);
        polylines = new ArrayList<>();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);





        mVictimInfo=(LinearLayout)findViewById(R.id.victim_info);
        mVictimProfileImage=(ImageView)findViewById(R.id.victimProfileImage);
        mVictimName=(TextView)findViewById(R.id.victimName);
        mVictimPhone=(TextView)findViewById(R.id.victimPhone);
        mVictimPhone1=(TextView)findViewById(R.id.victimPhone1);
        mVictimPhone2=(TextView)findViewById(R.id.victimPhone2);
        mVictimBG=(TextView)findViewById(R.id.victimBG);
        mVictimAllergy=(TextView)findViewById(R.id.victimAllergy);
        mVictimType=(TextView)findViewById(R.id.victimType);
      //  mInfo=(Button)findViewById(R.id.info);
      //  minfo1=(Button)findViewById(R.id.info1);
        mRideStatus = (Button) findViewById(R.id.rideStatus);

       // mHospital=(Button)findViewById(R.id.ambulance_hospital);





      //  mHospital.setOnClickListener(new View.OnClickListener() {
       //     @Override
      //      public void onClick(View view) {
      //          Intent intent=new Intent(AmbulanceMapActivity.this,HospitalMapActivity.class);
     //           startActivity(intent);
      //          return;
      //      }
      //  });


        mRideStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                endRide();
            }


        });




     //   mSetting=(Button)findViewById(R.id.settings);
     //   mSetting.setOnClickListener(new View.OnClickListener() {
       //     @Override
      //      public void onClick(View view) {
     //           Intent intent=new Intent(AmbulanceMapActivity.this,AmbulanceSettingActivity.class);
      //          startActivity(intent);
      //          return;
     //       }
    //    });

    /*    mInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if ( victimFound==false ){
                    mVictimInfo.setVisibility(View.GONE);
                    Toast.makeText(AmbulanceMapActivity.this,"There are no Victims",Toast.LENGTH_LONG).show();
                }
                else {
                    if(open==false){
                        getAssignedCustomerInfo();
                    }
                    else {
                        mVictimInfo.setVisibility(View.GONE);
                        open=false;
                    }
                }

            }
        });

        minfo1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AmbulanceMapActivity.this,MyPage.class);
                // intent.putExtra(customerId1,customerId.);
                Bundle bundle=new Bundle();
                bundle.putString("customerId",customerId);
                intent.putExtras(bundle);
                startActivity(intent);
                return;
            }
        });




        mLogOut=(Button)findViewById(R.id.logout);
        mLogOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLoggingout=true;
                disconnectDriver();
                FirebaseAuth.getInstance().signOut();
                Intent intent= new Intent(AmbulanceMapActivity.this,MainActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });   */

        getAssignedCustomer();
    }

    private void getAssignedCustomer() {
        String driverId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference assignedCustomerRef=FirebaseDatabase.getInstance().getReference().child("Users").child("Ambulance").child(driverId).child("victimRideId");
        assignedCustomerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                     customerId=dataSnapshot.getValue().toString();
                     if(!customerId.equals("")){
                         victimFound=true;
                         getAssignedCustomerPickUpLocation();
                     }
                     else{
                         victimFound=false;
                         erasePolylines();
                         customerId ="";

                         if(pickUpMarker != null){
                             pickUpMarker.remove();
                         }

                         if (assignedCustomerPickUPLocationRefListerner != null) {
                             assignedCustomerPickUPLocationRef.removeEventListener(assignedCustomerPickUPLocationRefListerner);
                         }
                         mVictimBG.setText("");
                         mVictimAllergy.setText("");
                         mVictimPhone.setText("");
                         mVictimPhone1.setText("");
                         mVictimPhone2.setText("");
                         mVictimName.setText("");
                         mVictimType.setText("");
                         mVictimProfileImage.setImageResource(R.mipmap.ic_launcher_user);

                     }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void getAssignedCustomerInfo(){

        DatabaseReference ref1 =FirebaseDatabase.getInstance().getReference().child("VictimRequest").child(customerId).child("Caller");
        ref1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.exists() && dataSnapshot.getValue().toString()!=null){
                    mCaller=(dataSnapshot.getValue().toString());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if(mCaller.equals("Self")){
            mVictimInfo.setVisibility(View.VISIBLE);
            open=true;

            Context context = getApplicationContext();
            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent notificationIntent = new Intent(context, MainActivity.class);

            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent intent =
                    PendingIntent.getActivity(context, 0,
                            notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

            String message="A victim is Found";
            String title="Golden Hour Response";
            int icon = R.drawable.login_image;
            long when = System.currentTimeMillis();

            Notification notification = new NotificationCompat.Builder(context)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setContentIntent(intent)
                    .setSmallIcon(icon)
                    .setWhen(when)
                    .build();

            notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

// Play default notification sound
            notification.defaults |= Notification.DEFAULT_SOUND;
            notification.defaults |= Notification.DEFAULT_VIBRATE;
            notificationManager.notify(0, notification);



            DatabaseReference mCustomerDatabase= FirebaseDatabase.getInstance().getReference().child("Users").child("Victim").child(customerId);
            mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                        Map<String,Object> map=(Map<String,Object>)dataSnapshot.getValue();
                        if(map.get("name")!=null){
                            mVictimName.setText(map.get("name").toString());
                        }
                        if(map.get("phone")!=null){
                            mVictimPhone.setText(map.get("phone").toString());
                        }
                        if(map.get("phone1")!=null){
                            mVictimPhone1.setText(map.get("phone1").toString());
                        }
                        if(map.get("phone2")!=null){
                            mVictimPhone2.setText(map.get("phone2").toString());
                        }
                        if(map.get("allergy")!=null){
                            mVictimAllergy.setText(map.get("allergy").toString());
                        }
                        if(map.get("bloodGroup")!=null){
                            mVictimBG.setText(map.get("bloodGroup").toString());
                        }
                        if(map.get("profileImageUrl")!=null){
                            Glide.with(getApplicationContext()).load(map.get("profileImageUrl").toString()).into(mVictimProfileImage);
                        }
                        mVictimType.setText("Self");

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

        }

        else{
            mVictimInfo.setVisibility(View.VISIBLE);
            open=true;

            Context context = getApplicationContext();
            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(Context.NOTIFICATION_SERVICE);
            Intent notificationIntent = new Intent(context, MainActivity.class);

            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent intent =
                    PendingIntent.getActivity(context, 0,
                            notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

            String message="A victim is Found";
            String title="Golden Hour Response";
            int icon = R.drawable.login_image;
            long when = System.currentTimeMillis();

            Notification notification = new NotificationCompat.Builder(context)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setContentIntent(intent)
                    .setSmallIcon(icon)
                    .setWhen(when)
                    .build();

            notification.flags |= Notification.FLAG_ONLY_ALERT_ONCE;

// Play default notification sound
            notification.defaults |= Notification.DEFAULT_SOUND;
            notification.defaults |= Notification.DEFAULT_VIBRATE;
            notificationManager.notify(0, notification);



            DatabaseReference getStranger=FirebaseDatabase.getInstance().getReference().child("VictimRequest").child(customerId);
            getStranger.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if(dataSnapshot.exists()){
                        mVictimBG.setText(dataSnapshot.child("bloodGroup").getValue().toString());
                        mVictimAllergy.setText(dataSnapshot.child("allergy").getValue().toString());
                        mVictimPhone.setText(dataSnapshot.child("phone").getValue().toString());
                        mVictimPhone1.setText(dataSnapshot.child("phone1").getValue().toString());
                        mVictimPhone2.setText(dataSnapshot.child("phone2").getValue().toString());
                        mVictimName.setText(dataSnapshot.child("name").getValue().toString());
                        mVictimType.setText("Stranger");
                        mVictimProfileImage.setImageResource(R.mipmap.ic_launcher_user);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        }

    }

    private Marker pickUpMarker;
    DatabaseReference assignedCustomerPickUPLocationRef;
    private ValueEventListener assignedCustomerPickUPLocationRefListerner;

    private void getAssignedCustomerPickUpLocation() {
        assignedCustomerPickUPLocationRef=FirebaseDatabase.getInstance().getReference().child("VictimRequest").child(customerId).child("l");
        assignedCustomerPickUPLocationRefListerner=assignedCustomerPickUPLocationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && !customerId.equals("")){
                    List<Object> map=(List<Object>) dataSnapshot.getValue();
                    double LocationLat=0;
                    double LocationLng=0;
                    if(map.get(0)!=null){
                        LocationLat=Double.parseDouble(map.get(0).toString());
                    }
                    if(map.get(1)!=null){
                        LocationLng=Double.parseDouble(map.get(1).toString());
                    }
                    pickUpLatLng= new LatLng(LocationLat,LocationLng);
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(pickUpLatLng));
                    mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
                    pickUpMarker = mMap.addMarker(new MarkerOptions().position(pickUpLatLng).title("PickUP Location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_victim)));
                    getRouteToMarker(pickUpLatLng);
                    mRideStatus.setVisibility(View.VISIBLE);
                    getAssignedCustomerInfo();
                    mVictimInfo.setVisibility(View.VISIBLE);
                    open=true;
                    }
                }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });

    }

    private void getRouteToMarker(LatLng pickUpLatLng) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(false)
                .waypoints(new LatLng(mLastLocation.getLatitude(),mLastLocation.getLongitude()), pickUpLatLng)
                .build();
        routing.execute();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        buildGoogleApiClient();
        mMap.setMyLocationEnabled(true);
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
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(getApplicationContext()!=null) {
            mLastLocation = location;
            LatLng latlng = new LatLng(location.getLatitude(), location.getLongitude());
            LatLng driverLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            if (mdriverMarker != null) {
                mdriverMarker.remove();
            }
            mdriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("Your Location").icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_ambulance)));
           if(victimFound==false) {
               mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
               mMap.animateCamera(CameraUpdateFactory.zoomTo(18));
           }

              //  erasePolylines();
              //  if (victimFound == true && !customerId.equals("")) {
             //       getRouteToMarker(pickUpLatLng);
            //    }



            String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
            DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("AmbulanceAvailable");
            DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("AmbulanceWorking");
            GeoFire geofireAvailable = new GeoFire(refAvailable);
            GeoFire geofireWorking = new GeoFire(refWorking);

            switch (customerId) {
                        case "":
                            geofireWorking.removeLocation(userid);
                            geofireAvailable.setLocation(userid, new GeoLocation(location.getLatitude(), location.getLongitude()));
                            break;
                        default:
                            geofireAvailable.removeLocation(userid);
                            geofireWorking.setLocation(userid, new GeoLocation(location.getLatitude(), location.getLongitude()));
                            break;
            }

        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void disconnectDriver(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        String userid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("AmbulanceAvailable");
         ref.child(userid).child("l").child("0").removeValue();
        ref.child(userid).child("l").child("1").removeValue();
        ref.child(userid).child("g").removeValue();
        GeoFire geofire=new GeoFire(ref);
       geofire.removeLocation(userid);

        DatabaseReference ref1 = FirebaseDatabase.getInstance().getReference().child("AmbulanceWorking");
       ref1.child(userid).child("l").child("0").removeValue();
        ref1.child(userid).child("l").child("1").removeValue();
       ref1.child(userid).child("g").removeValue();
        GeoFire geofire1=new GeoFire(ref1);
       geofire1.removeLocation(userid);

      DatabaseReference ref2=FirebaseDatabase.getInstance().getReference().child("Users").child("Ambulance").child(userid).child("victimRideId");
        ref2.setValue("");



    }
    @Override
    protected void onStop() {
        super.onStop();
      //  if(!isLoggingout){
        //    disconnectDriver();
       // }


    }



    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {

        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative routes
            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onRoutingCancelled() {

    }

    private void erasePolylines(){
        for (Polyline line:polylines){
            line.remove();
        }
        polylines.clear();
    }
    private void endRide(){
        mRideStatus.setVisibility(View.GONE);
        erasePolylines();

      //  String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String userid= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("VictimRequest");
        ref.child(customerId).child("l").child("0").removeValue();
        ref.child(customerId).child("l").child("1").removeValue();
        ref.child(customerId).child("g").removeValue();
        GeoFire geofire=new GeoFire(ref);
        geofire.removeLocation(customerId);

        DatabaseReference ref2=FirebaseDatabase.getInstance().getReference().child("Users").child("Ambulance").child(userid).child("victimRideId");
        ref2.setValue("");



        customerId="";

        if(mdriverMarker != null){
            mdriverMarker.remove();
        }
        if (assignedCustomerPickUPLocationRefListerner != null){
            assignedCustomerPickUPLocationRef.removeEventListener(assignedCustomerPickUPLocationRefListerner);
        }
        mVictimInfo.setVisibility(View.GONE);
        mVictimName.setText("");
        mVictimPhone.setText("");
        mVictimAllergy.setText("");

        mVictimProfileImage.setImageResource(R.mipmap.camera);
    }

}
