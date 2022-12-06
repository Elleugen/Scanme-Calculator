package com.calculator.scanme;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    Button bCapture, bCopy, bUpload, bClear;
    TextView tvConsole;
    ImageView ivCaptureScreen;
    Activity activity;
    private static final int CAMERA_REQUEST = 10;
    private static final int PERMISSION_CODE = 100;

    private static final int GALLERY_REQUEST = 1000;

    List<String> expressionList = new ArrayList<String>();

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = MainActivity.this;


        bClear = findViewById(R.id.clear);
        bCapture = findViewById(R.id.capture);
        bCopy = findViewById(R.id.copy);
        bUpload = findViewById(R.id.upload);
        tvConsole = findViewById(R.id.console);
        ivCaptureScreen = findViewById(R.id.captureScreen);


        //CAPTURE
        bCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_CODE);
                } else {
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });

//        UPLOAD
        bUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                galleryIntent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
//                Toast.makeText(MainActivity.this, "Feature not available yet!", Toast.LENGTH_SHORT).show();
            }
        });
        //COPY TEXT
        bCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String scannedText = tvConsole.getText().toString();
                copyTextToClipboard(scannedText);
            }
        });

        //CLEAR DATA
        bClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Bitmap bitmap = null;
//                ivCaptureScreen.setImageBitmap(null);
//                image.recycle();
                expressionList.clear();
                tvConsole.setText("");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CODE){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(activity, "Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "Not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK){
            Bitmap image = (Bitmap) data.getExtras().get("data");
            ivCaptureScreen.setImageBitmap(image);
            textDetect();
        }

        if(requestCode == GALLERY_REQUEST && resultCode == Activity.RESULT_OK){
            ivCaptureScreen.setImageURI(data.getData());
            textDetect();
        }
    }

    public void textDetect(){
        TextRecognizer textRecognizer = new TextRecognizer.Builder(activity).build();
        Bitmap bitmap = ((BitmapDrawable) ivCaptureScreen.getDrawable()).getBitmap();

        Frame frame = new Frame.Builder().setBitmap(bitmap).build();
        SparseArray<TextBlock> sparseArray = textRecognizer.detect(frame);
        StringBuilder stringBuilder = new StringBuilder();

        for(int i = 0; i < sparseArray.size(); i++){
            TextBlock textBlock = sparseArray.get(i);
            String string = textBlock.getValue();
            stringBuilder.append(string);
        }

//        tvConsole.setText(stringBuilder);

//        CALCULATOR BEGIN
//        String dummy = "9*922+3";
        String dummy = stringBuilder.toString();

        if(dummy.equals("")){
            Toast.makeText(MainActivity.this, "Can't process empty string. Please try again!", Toast.LENGTH_SHORT).show();
        } else {
            try {
                String a = Character.toString(dummy.charAt(0));
                String argument = Character.toString(dummy.charAt(1));
                String b = Character.toString(dummy.charAt(2));
                System.out.println(a + argument + b);

                int x = Integer.parseInt(a);
                int y = Integer.parseInt(b);
                int value = 0;
                if(argument.equals("+")){
                    value = x+y;
                }else if(argument.equals("-")){
                    value = x-y;
                }else if(argument.equals("*")){
                    value = x*y;
                }else if(argument.equals("/")){
                    value = x/y;
                }
                //CALCULATOR ARGUMENT VALIDATOR
                if(argument.equals("+") || argument.equals("-") || argument.equals("*") || argument.equals("/")){
                    String expression = a + argument + b + " = " + value;
                    System.out.println(expression);
                    expressionList.add(expression);
                    tvConsole.setText("");
                    for(int i = 0; i < expressionList.size(); i++){
                        int ctr = i+1;
                        System.out.println("List[" + ctr + "] : " +expressionList.get(i));
                        tvConsole.append("Expression ["+ ctr +"] : " + expressionList.get(i) + "\n\n");
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Can't calculate the expression. Please try again!", Toast.LENGTH_SHORT).show();
                }
            } catch(NumberFormatException e) {
                Toast.makeText(MainActivity.this, "No valid string detected. Please try again!", Toast.LENGTH_SHORT).show();
            }
        }


        //UI SWITCHING
//        tvConsole.setText(stringBuilder.toString());
        bCapture.setText("Recapture");
        bCopy.setVisibility(View.VISIBLE);
        bClear.setVisibility(View.VISIBLE);
    }

    private void copyTextToClipboard(String text){
        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("Copied Data", text);
        clipboardManager.setPrimaryClip(clipData);
        Toast.makeText(MainActivity.this, "Copied to clipboard!", Toast.LENGTH_SHORT).show();
    }
}