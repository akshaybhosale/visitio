package com.example.admin.ghr;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class MyPage extends AppCompatActivity {
    String customerId;
    private TextView mCustomerName, mCustomerPhone, mCustomerAllergy;

    private LinearLayout mCustomerInfo;

    private ImageView mCustomerProfileImage;
    private ImageView mCustomerPhoto;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_page);
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        customerId = bundle.getString("customerId");

        mCustomerInfo = (LinearLayout) findViewById(R.id.customerInfo);


        mCustomerProfileImage = (ImageView) findViewById(R.id.customerProfileImage);
        mCustomerPhoto = (ImageView) findViewById(R.id.customerphoto);

        mCustomerName = (TextView) findViewById(R.id.customerName);
        mCustomerAllergy = (TextView) findViewById(R.id.customerAllergy);
        mCustomerPhone = (TextView) findViewById(R.id.customerPhone);
        getAssignedCustomerInfo();

    }


    private void getAssignedCustomerInfo() {

        if (customerId != null) {
            mCustomerInfo.setVisibility(View.VISIBLE);
            DatabaseReference mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child("Victim").child(customerId);
            mCustomerDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists() && dataSnapshot.getChildrenCount() > 0) {
                        Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                        if (map.get("name") != null) {
                            mCustomerName.setText(map.get("name").toString());
                        }
                        if (map.get("phone") != null) {
                            mCustomerPhone.setText(map.get("phone").toString());
                        }
                        if (map.get("allergy") != null) {
                            mCustomerAllergy.setText(map.get("allergy").toString());
                        }
                        if (map.get("profileImageUrl") != null) {
                            Glide.with(getApplication()).load(map.get("profileImageUrl").toString()).into(mCustomerProfileImage);
                        }

                        if (map.get("VictimImageUrl") != null) {
                            Glide.with(getApplication()).load(map.get("VictimImageUrl").toString()).into(mCustomerPhoto);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
        } else {
            mCustomerName.setText("none");
        }
    }
}