package com.example.myapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import com.example.myapplication.com.example.myapplication.utility.CameraActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Mail will be sent", Snackbar.LENGTH_LONG)
                        .setAction("Send Message", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                sendMail();
                            }
                        }).show();
            }

            private void sendMail() {
                Intent i = new Intent(Intent.ACTION_SEND);
//i.setType("text/plain"); //use this line for testing in the emulator
//                i.setType("message/rfc822") ; // use from live device
//                i.setClassName("com.google.android.gm", "com.google.android.gm.ComposeActivityGmail");//sending email via gmail
                i.setType("plain/text");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{"dasavik@gmail.com"});
                i.putExtra(Intent.EXTRA_SUBJECT,"EMI Details");
                String emiText = getfieldDetails();
                i.putExtra(Intent.EXTRA_TEXT,emiText);
                startActivity(i);
            }
        });
        FloatingActionButton share = findViewById(R.id.share);
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Share WhatsApp", Snackbar.LENGTH_LONG)
                        .setAction("Share", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Snackbar.make(view, "Message shared", Snackbar.LENGTH_LONG)
                                        .show();
                            }
                        }).show();
            }
        });
        //camera button click
        FloatingActionButton camera = findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takeSnap();
        }

            });
        //store file button
        FloatingActionButton storeFile = findViewById(R.id.loadFile);
        storeFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    storeFile();
                }catch(Exception e)
                {
                    e.printStackTrace();
                    Snackbar.make(view, e.getLocalizedMessage(), Snackbar.LENGTH_LONG).show();
                }
            }

        });
        Button calcButton = findViewById(R.id.calc_button);
        calcButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView intRateView = findViewById(R.id.interestRate);
                TextView loanAmountView = findViewById(R.id.loanAmount);
                TextView tenureView = findViewById(R.id.tenure);
                TextView emiText = findViewById(R.id.emi_value);
                double principal = 0.0;
                double rate = 0.0;
                double time = 0.0;
                if(!loanAmountView.getText().toString().equals("")) {
                    principal = new Double(loanAmountView.getText().toString()).doubleValue();
                    if(!intRateView.getText().toString().equals("")) {
                        rate = new Double(intRateView.getText().toString()).doubleValue();
                        if(!tenureView.getText().toString().equals("")) {
                            time = new Double(tenureView.getText().toString()).doubleValue();
                            rate=rate/(12*100);
                            time=time*12;
                            double emi= (principal*rate*Math.pow(1+rate,time))/(Math.pow(1+rate,time)-1);
                            String emistr = "Loan Amount: "+principal+". Interest Amount: "+rate*12*100+". Tenure: "+time/12+". EMI Amount is: "+emi+" ";
                            emiText.setText(emistr);
                        }else{
                            emiText.setText("Please select Loan Tenaure");
                        }
                    }else{
                        emiText.setText("Please select interest Rate");
                    }
                }else{
                    emiText.setText("Please select loan Amount");
                }


            }
        });
    }

    private String getfieldDetails() {
        TextView emiText = findViewById(R.id.emi_value);
        return emiText.getText().toString();
    }

    private void storeFile() throws Exception {
        // Get the directory for the app's private pictures directory.
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String jsonFileName = "File_" + timeStamp + "_LOANCALC";
        String emiText = getfieldDetails();
        if(isExternalStorageWritable()){
            File file = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            File file1 = File.createTempFile(jsonFileName,
                    ".txt",file);
            FileOutputStream fOut = new FileOutputStream(file1);
            OutputStreamWriter osw = new OutputStreamWriter(fOut);
            // Write the string to the file
            osw.write(emiText);
            /* ensure that everything is
             * really written out and close */
            osw.flush();
            osw.close();
            System.out.println("file created: "+file);
        }
    }
    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }
    private void takeSnap() {
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String directory = Environment.DIRECTORY_PICTURES;
        if (i.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            CameraActivity cameraActivity = new CameraActivity();
            try {
//                photoFile = cameraActivity.createImageFile();
                photoFile =createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.myapplication",
                        photoFile);
                i.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(i, 1);
                galleryAddPic();
            }
        }}
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_LOANCALC";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.action_about:
                Intent aboutIntent = new Intent(this, DisplayMessageActivity.class);
                aboutIntent.putExtra("message","test message");
                startActivity(aboutIntent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }
}
