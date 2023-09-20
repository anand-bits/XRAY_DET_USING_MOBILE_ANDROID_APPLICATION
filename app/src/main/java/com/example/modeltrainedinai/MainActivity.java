package com.example.modeltrainedinai;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;

import com.example.modeltrainedinai.ml.Model;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

public class MainActivity extends AppCompatActivity {
    Button selectbtn, predictbtn, capturebtn;
    ImageView imageview;
    TextView Result;
    Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPermission();

        setContentView(R.layout.activity_main);
        selectbtn = findViewById(R.id.selectbutton);
        predictbtn = findViewById(R.id.predict);
        capturebtn = findViewById(R.id.capture);
        Result = findViewById(R.id.result);
        imageview = findViewById(R.id.imageview);

        selectbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 10);
            }
        });

        capturebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 12);
            }
        });
        predictbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bitmap != null) {
                    try {
                        Model model = Model.newInstance(MainActivity.this);

                        // Resize the bitmap to the required input dimensions
                        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 64, 64, true);

                        // Convert resized bitmap to normalized TensorImage
                        TensorImage tensorImage = TensorImage.fromBitmap(resizedBitmap);

                        //tensorImage.normalize(0, 255); // Assuming input range is [0, 255]

                        // Convert TensorImage to TensorBuffer
                        TensorBuffer inputBuffer = tensorImage.getTensorBuffer();

                        // Runs model inference and gets result
                        Model.Outputs outputs = model.process(inputBuffer);
                        TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

                        // Display the prediction result in the Result TextView
                        String predictionResult = "Prediction: " + outputFeature0.getFloatValue(0); // Assuming output is a single value
                        Result.setText(predictionResult);

                        // Releases model resources if no longer used.
                        model.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        // TODO Handle the exception
                    }
                }
            }
        });



    }

    void getPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 11);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 11) {
            if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                this.getPermission();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && data != null) {
            Uri uri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                imageview.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else if (requestCode == 12 && data != null) {
            bitmap = (Bitmap) data.getExtras().get("data");
            imageview.setImageBitmap(bitmap);
        }
    }
}
