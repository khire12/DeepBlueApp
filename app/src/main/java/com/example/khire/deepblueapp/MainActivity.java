package com.example.khire.deepblueapp;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient mFusedLocationClient;
    private Button btnChoose, btnUpload, btntake;
    private ImageView imageView;
    protected LocationManager locationManager;
    Location loc;
    String lat,lon;
    private Uri filePath;
    private static final int CAMERA_REQUEST_CODE = 1;
    private final int PICK_IMAGE_REQUEST = 10;
    private FirebaseDatabase firebaseDatabase;

    FirebaseStorage storage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseDatabase = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                           loc=location; // Logic to handle location object
                        }
                    }
                });
        //Initialize Views
        btnChoose = (Button) findViewById(R.id.btnChoose);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        btntake = findViewById(R.id.btnTakepicture);
        imageView = (ImageView) findViewById(R.id.imgView);

        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });


        btntake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


   /* private void takepicture(){
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //startActivity(intent);
        try {
            filePath = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(getApplicationContext(),filePath.toString(),Toast.LENGTH_LONG).show();
        intent.putExtra(MediaStore.EXTRA_OUTPUT,filePath);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent,CAMERA_REQUEST_CODE);
        }
    }*/

    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            Toast.makeText(getApplicationContext(),photoFile.toString(),Toast.LENGTH_LONG).show();
            // Continue only if the File was successfully created
            if (photoFile != null) {

                filePath = FileProvider.getUriForFile(this,
                        "com.example.khire.deepblueapp",
                        photoFile);
                //filePath = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() +
                     //   ".my.package.name.provider", createImageFile());
                if(filePath!=null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, filePath);
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                }
                else {
                    Toast.makeText(getApplicationContext(), "filePath is null", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    String mCurrentPhotoPath;
    String imageFileName;

    private File createImageFile() throws IOException {
        // Create an image file names

        lat=String.valueOf(loc.getLatitude());
        lon=String.valueOf(loc.getLongitude());
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
         imageFileName = lat+" "+lon;
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return (image);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
       // Toast.makeText(getApplicationContext(),resultCode,Toast.LENGTH_LONG).show();
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null ) {
            filePath = data.getData();
            Toast.makeText(getApplicationContext(),data.getData().toString(),Toast.LENGTH_LONG).show();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) {
            galleryAddPic();
             //filePath = data.getData();
//             Bundle extras = data.getExtras();
//             Bitmap bitmap = (Bitmap) extras.get(MediaStore.EXTRA_OUTPUT);
//             Toast.makeText(getApplicationContext(),extras.get(MediaStore.EXTRA_OUTPUT).toString(),Toast.LENGTH_LONG).show();
//             imageView.setImageBitmap(bitmap);
//                StorageReference filepath = storageReference.child(filePath.getLastPathSegment());
//                filepath.putFile(filePath).addOnFailureListener(new OnFailureListener() {
//                 @Override
//                 public void onFailure(@NonNull Exception exception) {
//                     // Handle unsuccessful uploads
//                 }
//             }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
//                 @Override
//                 public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                     // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
//                     // ...
//
//                     Toast.makeText(getApplicationContext(),taskSnapshot.getMetadata().getContentType(),Toast.LENGTH_LONG).show();
//                 }
//             });

        }
    }



    private void uploadImage() {

        final HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("downloadStatus",0);
        hashMap.put("latitude",Float.parseFloat(lat));
        hashMap.put("longitude",Float.parseFloat(lon));

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            String uuid = imageFileName+".jpg";


            StorageReference ref = storageReference.child(uuid);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                            firebaseDatabase.getReference().push().setValue(hashMap);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

}







