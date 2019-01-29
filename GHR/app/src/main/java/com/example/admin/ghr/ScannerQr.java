package com.example.admin.ghr;

import android.content.Intent;
import android.widget.Toast;
        import android.app.Activity;
        import android.content.Intent;
        import android.support.v7.app.AppCompatActivity;
        import android.os.Bundle;
        import android.view.View;
        import android.widget.Button;
        import android.widget.Toast;

        import com.google.zxing.integration.android.IntentIntegrator;
        import com.google.zxing.integration.android.IntentResult;

public class ScannerQr extends AppCompatActivity {
    private Button scan_btn,back_button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner_qr);

        scan_btn=(Button) findViewById(R.id.scan_btn);

        back_button=(Button)findViewById(R.id.back);

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(ScannerQr.this,VictimMapActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });
        final Activity activity=this;

        scan_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentIntegrator integrator =new IntentIntegrator(activity);
                integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
                integrator.setPrompt("Scan");
                integrator.setCameraId(0);
                integrator.setBeepEnabled(false);
                integrator.setBarcodeImageEnabled(false);
                integrator.initiateScan();

            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result=IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result!=null)
        {
            if(result.getContents()==null)
            {
                Toast.makeText(this,"YOU CANCELLED THE SCANNING",Toast.LENGTH_LONG).show();

            }
            else
            {
                Toast.makeText(this, result.getContents(), Toast.LENGTH_LONG).show();
                String []text=result.getContents().split("\n",5);
                String name=text[0].split(":")[1];
                String phone=text[1].split(":")[1];
                String phone1=text[2].split(":")[1];
                String phone2=text[3].split(":")[1];
                String allergy=text[2].split(":")[1];
                String blood_group=text[3].split(":")[1];

                Intent intent=new Intent(ScannerQr.this,VictimMapActivity.class);

                Bundle bundle = new Bundle();
                bundle.putString("NAME", name);
                bundle.putString("PHONE", phone);
                bundle.putString("PHONE1", phone1);
                bundle.putString("PHONE2", phone2);
                bundle.putString("ALLERGY", allergy);
                bundle.putString("BG", blood_group);

                intent.putExtras(bundle);
                startActivity(intent);
                finish();
                return;
            }
        }
        else
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}