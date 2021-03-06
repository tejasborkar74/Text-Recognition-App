package com.example.textrecognition;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextDetector;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button captureButton , detectButton , toGallery;
    ImageView imageView;
    TextView textView;
    ScrollView scrollView;


    static final int FOR_GALLERY = 1 , FOR_CLICK = 5;
    Bitmap imageBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        captureButton = findViewById(R.id.capture);
        detectButton = findViewById(R.id.detectText);
        toGallery = findViewById(R.id.toGallery);

        imageView = findViewById(R.id.image);
        textView = findViewById(R.id.textView);
      //  scrollView = findViewById(R.id.scrollView);

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                textView.setText("");
                dispatchTakePictureIntent();
            }
        });

        toGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textView.setText("");
                picturefromgallery();
            }
        });



        detectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                detectTextFromImage();
            }
        });

    }

    void picturefromgallery()
    {
        Intent fromGallery = new Intent();
        fromGallery.setType("image/*");
        fromGallery.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(fromGallery , "Select Picture"),FOR_GALLERY);

    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, FOR_CLICK);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FOR_GALLERY && resultCode == RESULT_OK) {

            try {
                InputStream  inputStream = getContentResolver().openInputStream(data.getData());
                imageBitmap = BitmapFactory.decodeStream(inputStream);
                imageView.setImageBitmap(imageBitmap);
            }catch (FileNotFoundException e)
            {
                e.printStackTrace();
                Toast.makeText(MainActivity.this , "Error: "+ e.getMessage() , Toast.LENGTH_LONG).show();
            }
        }
        else if(requestCode == FOR_CLICK && resultCode == RESULT_OK)
        {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
    }

    void detectTextFromImage()
    {
        final FirebaseVisionImage firebaseVisionImage = FirebaseVisionImage.fromBitmap(imageBitmap);
        FirebaseVisionTextDetector firebaseVisionTextDetector = FirebaseVision.getInstance().getVisionTextDetector();

        firebaseVisionTextDetector.detectInImage(firebaseVisionImage).addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
            @Override
            public void onSuccess(FirebaseVisionText firebaseVisionText)
            {
                displayTextFromImage(firebaseVisionText);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this , "Error: "+ e.getMessage() , Toast.LENGTH_LONG).show();
            }
        });
    }

    void displayTextFromImage(FirebaseVisionText firebaseVisionText)
    {
        List<FirebaseVisionText.Block> blockList = firebaseVisionText.getBlocks();

        if(blockList.size() == 0)
        {
            Toast.makeText(MainActivity.this,"No Text Found in Image" , Toast.LENGTH_LONG).show();
        }
        else
        {
            String text="";
            for(FirebaseVisionText.Block block : firebaseVisionText.getBlocks())
            {
                text = text + block.getText();

            }
            textView.setText(text);
        }

    }

}
