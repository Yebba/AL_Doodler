package com.example.al_doodler;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.al_doodler.Interface.ToolsListener;
import com.example.al_doodler.adapters.ToolsAdapters;
import com.example.al_doodler.common.Common;
import com.example.al_doodler.model.ToolsItem;
import com.example.al_doodler.widget.DoodleView;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.UUID;

// This is the file that brings everything together.
public class MainActivity extends AppCompatActivity implements ToolsListener {

    private static final int REQUEST_PERMISSION = 1001;
    private static final int PICK_IMAGE = 1000;
    DoodleView mDoodleView;
    int colorBackground, colorBrush;
    int brushSize, eraserSize;
    private Object canvas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initTools();
    }

//  Initialize the tool bar and canvas in the app
    private void initTools() {

        colorBackground = Color.WHITE;
        colorBrush = Color.BLACK;

        eraserSize = 12;
        brushSize = 12;
        mDoodleView = findViewById(R.id.paint_view);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_tools);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        ToolsAdapters toolsAdapters = new ToolsAdapters(loadTools(), this);
        recyclerView.setAdapter(toolsAdapters);

    }

//  This is where we make all the tool bar elements and add them to visible tool bar.
    private List<ToolsItem> loadTools() {

        List<ToolsItem> result = new ArrayList<>();

        result.add(new ToolsItem(R.drawable.baseline_brush_24, Common.BRUSH));
        result.add(new ToolsItem(R.drawable.eraser_white, Common.ERASER));
        result.add(new ToolsItem(R.drawable.icons8_rubber_stamp_48, Common.IMAGE));
        result.add(new ToolsItem(R.drawable.baseline_palette_24, Common.COLORS));
        result.add(new ToolsItem(R.drawable.paint_white, Common.BACKGROUND));
        result.add(new ToolsItem(R.drawable.baseline_undo_24, Common.RETURN));
        result.add(new ToolsItem(R.drawable.baseline_clear_24, Common.CLEAR));

        return result;
    }

//  When the user is done with the doodle app, then they can his the 'back' button in the bottom left of the app to leave the app.
    public void finishPaint(View view) {
        finish();
    }

//  This empty functions are for later functionality. I'll choose one of them for IA09.
    public void shareApp(View view) {
       Intent intent = new Intent(Intent.ACTION_SEND);
       intent.setType("text/plain");
       String bodyText = "http://play.google.com/store/apps/details?id="+getPackageName();
       intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
       intent.putExtra(Intent.EXTRA_TEXT, bodyText);
       startActivity(Intent.createChooser(intent,"share this app"));
    }

    public void showFiles(View view) {
        startActivity(new Intent(this, ListFilesAct.class));
    }

    public void saveFile(View view) {
        saveBitmap();
//        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
//            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
//        }else {
//            saveBitmap();
//        }
    }

    private void saveBitmap() {
        Bitmap bitmap = mDoodleView.getBitmap();
        String file_name = UUID.randomUUID() + ".png";

        File folder = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + File.separator+getString(R.string.app_name));

        if(!folder.exists()){
            folder.mkdirs();
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(folder+File.separator+file_name);
            bitmap.compress(Bitmap.CompressFormat.PNG,100,fileOutputStream);
            Toast.makeText(this, "doodle saved!", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == REQUEST_PERMISSION && grantResults.length >0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            saveBitmap();
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    //  This function decides what to do depending on which tool is selected.
    @Override
    public void onSelected(String name) {

        switch (name){
            case Common.BRUSH:
                mDoodleView.toMove = false;
                mDoodleView.disableEraser();
                mDoodleView.invalidate();
                showDialogSize(false);
                break;

            case Common.ERASER:
                mDoodleView.toMove = false;
                mDoodleView.enableEraser();
                mDoodleView.invalidate();
                showDialogSize(true);
                break;

            case Common.RETURN:
                mDoodleView.toMove = false;
                mDoodleView.returnLastAction();
                mDoodleView.invalidate();
                break;

            case Common.BACKGROUND:
                mDoodleView.toMove = false;
                updateColor(name);
                mDoodleView.invalidate();
                break;

            case Common.COLORS:
                mDoodleView.toMove = false;
                updateColor(name);
                mDoodleView.invalidate();
                break;

            case Common.CLEAR:
                mDoodleView.clearCanvas();
                break;

            case Common.IMAGE:
                getImage();
                break;
        }

    }

    private void getImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent,"Select picture"), PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if (requestCode == PICK_IMAGE && data != null && resultCode == RESULT_OK) {
            Uri pickedImage = data.getData();
            String[] filePath = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(pickedImage, filePath, null, null, null);
            cursor.moveToFirst();
            @SuppressLint("Range") String imagePath = cursor.getString(cursor.getColumnIndex(filePath[0]));

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            Bitmap bitmap = BitmapFactory.decodeFile(imagePath,options);

            mDoodleView.setImage(bitmap);

            cursor.close();
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    //  Change the color of either the brush or the background, depending on which is currently selected.
    private void updateColor(String name) {

        int color;

        if (name.equals(Common.BACKGROUND)){
            color = colorBackground;
        }else {
            color = colorBrush;
        }

//      Big block of code to interact with the color picker (https://github.com/QuadFlask/colorpicker)
        ColorPickerDialogBuilder
                .with(this)
                .setTitle("Choose color")
                .initialColor(color)
                .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                .density(12)
                .setPositiveButton("OK", new ColorPickerClickListener() {
                    @Override
                    public void onClick(DialogInterface d, int lastSelectedColor, Integer[] allColors) {
                        if (name.equals(Common.BACKGROUND)){
                            colorBackground = lastSelectedColor;
                            mDoodleView.setColorBackground(colorBackground);
                        }else {
                            colorBrush = lastSelectedColor;
                            mDoodleView.setBrushColor(colorBrush);
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).build()
                        .show();
    }

//  This block of code is for the seek bar when selecting the brush and eraser size
    private void showDialogSize(boolean isEraser) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_dialog,null, false);

        TextView toolsSelected = view.findViewById(R.id.status_tools_selected);
        TextView statusSize = view.findViewById(R.id.status_size);
        ImageView ivTools = view.findViewById(R.id.iv_tools);
        SeekBar seekBar = view.findViewById(R.id.seekbar_size);
        seekBar.setMax(99);

        if (isEraser){

            toolsSelected.setText("Eraser Size");
            ivTools.setImageResource(R.drawable.eraser_black);
            statusSize.setText("Selected Size : "+eraserSize);
            seekBar.setProgress(eraserSize);

        }else {

            toolsSelected.setText("Brush Size");
            ivTools.setImageResource(R.drawable.baseline_brush);
            statusSize.setText("Selected Size : "+brushSize);
            seekBar.setProgress(brushSize);
        }

//      Track the user changing the value with the seek bar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b){

                if(isEraser){
                    eraserSize = i+1;
                    statusSize.setText("Selected Size : "+eraserSize);
                }else {
                    brushSize = i+1;
                    statusSize.setText("Selected Size : "+brushSize);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar){

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar){

            }

        });


//      When the user selects ok, update the size of the brush / eraser tool
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(isEraser){
                    mDoodleView.setSizeEraser(eraserSize);
                }else {
                    mDoodleView.setSizeBrush(brushSize);
                }
                dialogInterface.dismiss();
            }
        });

        builder.setView(view);
        builder.show();
    }
}