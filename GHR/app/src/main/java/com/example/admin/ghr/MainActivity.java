package com.example.admin.ghr;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;

import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private Button mAmbulance,mVictim;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener firebaseAuthhListener;
   // private int checkAmb=0;   //T--Ambulance//F-Victim

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        if(user!=null)
        {
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            //Toast.makeText(MainActivity.this, userId, Toast.LENGTH_SHORT).show();
                    DatabaseReference ref= FirebaseDatabase.getInstance().getReference().child("Users").child("Ambulance").child(userId);
                    ref.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0)
                            {
                                Intent intent = new Intent(MainActivity.this, AmbulanceMapActivity.class);
                                startActivity(intent);
                                finish();
                                return;
                            }

                            else
                            {
                                Intent intent=new Intent(MainActivity.this,VictimMapActivity.class);
                                startActivity(intent);
                                finish();
                                return;
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError)
                        {

                        }
                    });


        }



        mAuth= FirebaseAuth.getInstance();
        firebaseAuthhListener =new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

            }
        };

        mAmbulance=(Button) findViewById(R.id.ambulance_button);
        mVictim=(Button) findViewById(R.id.victim_button);

        mAmbulance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,AmbulanceLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mVictim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(MainActivity.this,VictimLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

    }
}
