package com.typical_coderr.banknote_assit;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.loader.content.CursorLoader;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import java.io.ByteArrayOutputStream;
import java.io.File;

import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import com.typical_coderr.banknote_assit.service.FlaskClient;
import com.typical_coderr.banknote_assit.service.RetrofitClientInstance;


import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private String filePath = "";
    private Button gallery, mSubmit;
    private TextView imgName, responseTxt;
    private Uri selectedImage;
    private ProgressDialog mProgressDialog;
    private Boolean imageSelected;
    private Bitmap bitmap;
    private Button btnCapture;
    private ImageView imageView;



    /*
        private int IMG_REQUEST = 21;
        private static final String IMAGE_DIRECTORY = "/banknote_gallery";
        private String mediaPath;
        public static final int PICK_IMAGE = 100;
    */

    private final FlaskClient flaskClient = RetrofitClientInstance.getRetrofitInstance().create(FlaskClient.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProgressDialog = new ProgressDialog(this);

        imageSelected = false;

        isStoragePermissionGranted();
        imgName = findViewById(R.id.imgName);
        imageView = findViewById(R.id.imageView);
        responseTxt = findViewById(R.id.responseText);
        gallery = findViewById(R.id.gallery);
        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 3);
            }
        });

        btnCapture = findViewById(R.id.btnCapture);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(intent, 1);
                dispatchTakePictureIntent();

            }
        });

        mSubmit = findViewById(R.id.upload);
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadImage();
            }
        });
    }

    //read & write storage permission
    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
//                Log.v(TAG,"Permission is granted");
                return true;
            } else if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
//                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }


        } else { //permission is automatically granted on sdk<23 upon installation
//            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 3 && resultCode == RESULT_OK && data != null) {
            selectedImage = data.getData();


            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
                imageView.setImageBitmap(bitmap);
                imgName.setVisibility(View.GONE);
                filePath = getRealPathFromURI(selectedImage);
                imageSelected = true;


            } catch (IOException e) {
                e.printStackTrace();
            }


        } else if (requestCode == 1 && resultCode == RESULT_OK && data != null) {

            bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImage);
                imageView.setImageBitmap(bitmap);
                imgName.setVisibility(View.GONE);
                imageSelected = true;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//            Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
//
//            imgName.setVisibility(View.GONE);
//            filePath = getRealPathFromURI(selectedImage);
//            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
//            thumbnail.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
//            imageView.setImageBitmap(thumbnail);
//
//            File destination = new File(Environment.getExternalStorageDirectory(),
//                    System.currentTimeMillis() + ".jpg");
//
//            FileOutputStream fo;
//            System.out.println(destination);
//            try {
//                destination.createNewFile();
//                fo = new FileOutputStream(destination);
//                fo.write(bytes.toByteArray());
//                fo.close();
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            filePath = destination.toString();
//            imageSelected = true;





    }

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
            // Continue only if the File was successfully created
            if (photoFile != null) {
                selectedImage = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, selectedImage);
                startActivityForResult(takePictureIntent, 1);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "PNG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".png",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        filePath = image.getAbsolutePath();
        System.out.println(filePath);
        return image;
    }

    //fetching the absolute path of the image file
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    //upload the image
    private void uploadImage() {

        if (imageSelected == true) {

            //creating a file
            File file = new File(filePath);
            if (file.exists()) {


                // create RequestBody instance from file
                RequestBody filePart = RequestBody.create(MediaType.parse("image/*"), file);
                // MultipartBody.Part is used to send the actual file name
                MultipartBody.Part part = MultipartBody.Part.createFormData("image", file.getName(), filePart);

                //Show progress
                mProgressDialog.setMessage("Verifying the image...");
                mProgressDialog.show();

                // finally, execute the request
                Call<ResponseBody> call = flaskClient.uploadBanknote(part);
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        //Successfully classified
                        if (response.code() == 200) {

                            try {

                                // Capture an display specific messages
                                responseTxt.setText(response.body().string());

                            } catch (Exception e) {
                                Toast.makeText(MainActivity.this, "An error occurred", Toast.LENGTH_SHORT).show();
                            }

                        } else {
                            Toast.makeText(MainActivity.this, "Something went Wrong!", Toast.LENGTH_SHORT).show();
                        }
                        mProgressDialog.dismiss();
                    }

                    @Override
                    public void onFailure(Call call, Throwable t) {
                        Toast.makeText(MainActivity.this, "Something went Wrong", Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss();
                        System.out.println(t.toString());
                    }
                });

            } else {
                Toast.makeText(this, "File is not setup properly.", Toast.LENGTH_LONG).show();
            }


        } else {
            Toast.makeText(this, "You haven't Selected any Image.", Toast.LENGTH_LONG).show();
        }
    }
}