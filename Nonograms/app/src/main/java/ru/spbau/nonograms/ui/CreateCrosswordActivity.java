package ru.spbau.nonograms.ui;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import ru.spbau.nonograms.R;
import ru.spbau.nonograms.controller.Controller;

public class CreateCrosswordActivity extends AppCompatActivity {

    private static final int RESULT_LOAD_IMAGE = 1;
    private ImageView givenImage;
    private ImageView resultImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_crossword);

        givenImage = (ImageView)findViewById(R.id.givenImage);
        resultImage = (ImageView)findViewById(R.id.resultImage);

        Button uploadPictureButton = (Button)findViewById(R.id.uploadPictureButton);
        uploadPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        Button makePuzzleButton = (Button) findViewById(R.id.makeCrosswordButton);
        makePuzzleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bitmap result = Controller.makeCrosswordViewFromImage(givenImage);
                resultImage.setImageBitmap(result);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri imageUri = data.getData();
            givenImage.setImageURI(imageUri);
        }
    }
}