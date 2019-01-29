package com.example.admin.ghr;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class GenerateQr extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String Name;
    private String Phone;
    private String Allergy;
    private String Bg;
    private String Phone1;
    private String Phone2;



    private FirebaseAuth.AuthStateListener firebaseAuthhListener;
    //private DatabaseReference mCustomerDatabase;
    // private String userId;

    String text="";
    Button back_button;
    ImageView image;
    String text2Qr="";
    // String mName="",mPhone="",mBloodGroup="",mAllergy="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_qr);
        call();

        back_button=(Button)findViewById(R.id.back);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(GenerateQr.this,VictimMapActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

    }


    /** Create a File for saving an image or video */
    private File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");
        // "/DCIM/Facebook/");
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
//        String mImageName="GHR_QR_"+timeStamp+".jpg";
        String mImageName="GHR_QR.jpg";

        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }


    private void storeImage(Bitmap image) {
        File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Toast.makeText(GenerateQr.this,"Error creating media file, check storage permissions: ",Toast.LENGTH_LONG).show();
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            Toast.makeText(GenerateQr.this,"QR SAVED",Toast.LENGTH_LONG).show();
            fos.close();
        } catch (FileNotFoundException e) {
            Toast.makeText(GenerateQr.this,"File not found: " + e.getMessage(),Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Toast.makeText(GenerateQr.this,"Error accessing file: " + e.getMessage(),Toast.LENGTH_LONG).show();
        }
    }



    private  void call()
    {
        text="Save This Text";
        //final String RelativePhone;
        // gen_button=(Button)findViewById(R.id.generate);
        image=(ImageView)findViewById(R.id.qrcode);


        //  String RelativePhone;
///
        String victimId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference gg = FirebaseDatabase.getInstance().getReference().child("Users").child("Victim").child(victimId);
        gg.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //String url = "https://www.google.com/maps/search/?api=1&query=";


                Name= dataSnapshot.child("name").getValue(String.class);
                Phone= dataSnapshot.child("phone").getValue(String.class);
                Phone1= dataSnapshot.child("phone1").getValue(String.class);
                Phone2= dataSnapshot.child("phone2").getValue(String.class);
                Allergy= dataSnapshot.child("allergy").getValue(String.class);
                Bg= dataSnapshot.child("bloodGroup").getValue(String.class);

                text2Qr="NAME:"+Name+"\nPHONE:"+Phone+"\nRELATIVE_1:"+Phone1+"\nRELATIVE_2:"+Phone2+"\nALLERGY:"+Allergy+"\nBLOOD_GROUP:"+Bg;

                MultiFormatWriter multiFormatWriter=new MultiFormatWriter();
                try{
                    BitMatrix bitMatrix=multiFormatWriter.encode(text2Qr, BarcodeFormat.QR_CODE,200,50);
                    BitMatrix bitMatrix2=multiFormatWriter.encode(text2Qr, BarcodeFormat.QR_CODE,200,200);

                    BarcodeEncoder barcodeEncoder=new BarcodeEncoder();
                    BarcodeEncoder barcodeEncoder2=new BarcodeEncoder();

                    Bitmap bitmap =barcodeEncoder.createBitmap(bitMatrix);
                    Bitmap bitmap2 =barcodeEncoder2.createBitmap(bitMatrix2);

                    // saveImageToInternalStorage(bitmap);
                    storeImage(bitmap2);


                    Context context = getApplicationContext();
                    NotificationManager notificationManager = (NotificationManager)
                            context.getSystemService(Context.NOTIFICATION_SERVICE);
                    Intent notificationIntent = new Intent(context, MainActivity.class);

                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP);

                    PendingIntent intent =
                            PendingIntent.getActivity(context, 0,
                                    notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT);

                    String message="Scan This Qr Code";
                    String title="Golden Hour Response";
                    int icon = R.drawable.login_image;
                    long when = System.currentTimeMillis();

                    Notification notification = new NotificationCompat.Builder(context)
                            .setContentTitle(title)
                            .setContentText(message)
                            .setContentIntent(intent)
                            .setSmallIcon(icon)
                            .setWhen(when)
                            .setStyle(new NotificationCompat.BigPictureStyle()
                                    .bigPicture(bitmap).setSummaryText(message))
                            .build();

                    notification.flags |= Notification.FLAG_NO_CLEAR;

// Play default notification sound
                    notification.defaults |= Notification.DEFAULT_SOUND;
                    notification.defaults |= Notification.DEFAULT_VIBRATE;
                    notificationManager.notify(0, notification);




                    image.setImageBitmap(bitmap2);
                }
                catch(WriterException e)
                {
                    e.printStackTrace();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });////
    }





}



              /*   text2Qr=mName.trim();

                 MultiFormatWriter multiFormatWriter=new MultiFormatWriter();
                 try{
                     BitMatrix bitMatrix=multiFormatWriter.encode(text2Qr, BarcodeFormat.QR_CODE,200,200);
                     BarcodeEncoder barcodeEncoder=new BarcodeEncoder();
                     Bitmap bitmap =barcodeEncoder.createBitmap(bitMatrix);
                     image.setImageBitmap(bitmap);
                 }
                 catch(WriterException e)
                 {
                     e.printStackTrace();
                 }

             }
         });  */

