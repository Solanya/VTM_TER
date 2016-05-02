package com.vtm.solanya.imgtrt;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class Image {
    Context context;
    Activity activity;
    public Image(Context context, Activity activity){
        this.context = context;
        this.activity = activity;
    }

    public int imageWidth;
    public int imageHeight;
    public int[][] pixelsCurrent;
    public int[][] pixelsOld;
    public Bitmap.Config bitmapConfig;
    public boolean cancelled = false;

    TextView messageBox;
    ImageView displayBox;

    public void loadImage(Intent data){
        displayBox = (ImageView) this.activity.findViewById(R.id.imageDisplay);
        String path = getRealPathFromURI(data.getData());
        Log.d("Choose Picture", path);
        //Transformer la photo en Bitmap
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        //Afficher le Bitmap
        displayBox.setImageBitmap(bitmap);
        imageWidth = bitmap.getWidth();
        imageHeight = bitmap.getHeight();

        pixelsCurrent = new int[imageWidth][imageHeight];
        pixelsOld = new int[imageWidth][imageHeight];
        //pixelsTemp = new int[imageWidth * imageHeight];

        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {
                pixelsCurrent[x][y] = bitmap.getPixel(x, y);
            }
        }

        ((Button) ((Activity)context).findViewById(R.id.btCancel)).setText("Annuler");
        ((Button) ((Activity)context).findViewById(R.id.btCancel)).setVisibility(View.GONE);
    }

    public void loadCamImage(Intent data){
        displayBox = (ImageView) this.activity.findViewById(R.id.imageDisplay);
        String path = getRealPathFromURI(data.getData());
        Log.d("Choose Picture", path);
        //Transformer la photo en Bitmap
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        //Afficher le Bitmap
        displayBox.setImageBitmap(bitmap);
        imageWidth = bitmap.getWidth();
        imageHeight = bitmap.getHeight();

        pixelsCurrent = new int[imageWidth][imageHeight];
        pixelsOld = new int[imageWidth][imageHeight];
        //pixelsTemp = new int[imageWidth * imageHeight];

        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {
                pixelsCurrent[x][y] = bitmap.getPixel(x, y);
            }
        }

        ((Button) ((Activity)context).findViewById(R.id.btCancel)).setText("Annuler");
        ((Button) ((Activity)context).findViewById(R.id.btCancel)).setVisibility(View.GONE);
    }

    public boolean saveImage(){
        displayBox = (ImageView) this.activity.findViewById(R.id.imageDisplay);
        messageBox = (TextView) this.activity.findViewById(R.id.textBox);
        String fullPath = "";
        OutputStream fOut = null;
        File file;

        try {
            fullPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "Saved Images";

            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            file = new File(fullPath, "image.png");
            file.createNewFile();
            fOut = new FileOutputStream(file);
        } catch (Exception e) {
            fullPath = context.getFilesDir().getAbsolutePath() + "/Saved Images";

            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            file = new File(fullPath, "image.png");
            try {
                file.createNewFile();
                fOut = new FileOutputStream(file);
            } catch (Exception e2) {
                Log.e("Save Image", e.getMessage());
                messageBox.setText("Error saving.");
                return false;
            }
        }

        try {
            if (displayBox.getDrawable() == null) {
                messageBox.setText("No image.");

                return false;
            }

            BitmapDrawable bitmapDrawable = ((BitmapDrawable) displayBox.getDrawable());
            Bitmap bitmap = bitmapDrawable.getBitmap();

            // 100 means no compression, the lower you go, the stronger the compression
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();

            MediaStore.Images.Media.insertImage(context.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

            messageBox.setText("Image saved.");

            return true;


        } catch (Exception e) {
            Log.e("Save Image", e.getMessage());
            messageBox.setText("Error saving.");
            return false;
        }
    }

    public void cancelProcess(){
        displayBox = (ImageView) this.activity.findViewById(R.id.imageDisplay);
        int pxlSize = imageWidth * imageHeight;
        int[] pixelsTemp = new int[pxlSize];

        for (int x=0; x<imageWidth; x++){
            for (int y=0; y<imageHeight; y++){
                pixelsTemp[imageWidth * y + x] = pixelsOld[x][y];
                pixelsOld[x][y] = pixelsCurrent[x][y];
                pixelsCurrent[x][y] = pixelsTemp[imageWidth * y + x];
            }
        }

        if (!cancelled){
            ((Button) ((Activity)context).findViewById(R.id.btCancel)).setText("Refaire");
            cancelled = true;
        }
        else {
            ((Button) ((Activity)context).findViewById(R.id.btCancel)).setText("Annuler");
            cancelled = false;
        }

        Bitmap bitmapNew = Bitmap.createBitmap(pixelsTemp, imageWidth, imageHeight, bitmapConfig);
        displayBox.setImageBitmap(bitmapNew);
    }

    /**
     * Obtenir le chemin vers une ressource
     * la fonction a été trouvé sur Stackoverflow
     *
     * @param contentURI
     * @return
     */
    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = context.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            result = contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }
}
