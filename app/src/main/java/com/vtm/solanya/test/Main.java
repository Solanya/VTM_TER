package com.vtm.solanya.test;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Sélectionner une photo dans la gallerie
 * http://www.fobec.com/java/1161/selectionner-puis-afficher-une-image.html
 * @author Axel fevrier 2015
 */
public class Main extends Activity {
    //constante pour définir l'id du type image

    final static int SELECT_PICTURE = 1;
    private SeekBar seuilControlR = null;
    private SeekBar seuilControlG = null;
    private SeekBar seuilControlB = null;
    int seuilValueR = 0;
    int seuilValueG = 0;
    int seuilValueB = 0;
    boolean imageRead = true;
    int[][] pixels;


    /*public  boolean isStoragePermissionGranted() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v("Permission","Permission is granted");
                return true;
            } else {

                Log.v("Permission","Permission is revoked");
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("Permission","Permission is granted");
            return true;
        }


    }*/

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // Bouton de chargement depuis la gallerie

        ((Button) findViewById(R.id.btGallery)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btGalleryClick(v);
            }
        });

        // Bouton de sauvegarde de l'image

        ((Button) findViewById(R.id.btSave)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btSaveClick(v);
            }
        });

        // Bouton pour effectuer le seuil

        ((Button) findViewById(R.id.btSeuil)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btSeuilClick(v);
            }
        });

        // Sliders de contrôle de seuil

        seuilControlR = (SeekBar) findViewById(R.id.seuilBarR);
        seuilControlG = (SeekBar) findViewById(R.id.seuilBarG);
        seuilControlB = (SeekBar) findViewById(R.id.seuilBarB);



        seuilControlR.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seuilValueR = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                TextView textView = (TextView) findViewById(R.id.seuilText);
                textView.setTextColor(Color.RED);
                textView.setText(Integer.toString(seuilValueR));
            }
        });

        seuilControlG.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seuilValueG = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                TextView textView = (TextView) findViewById(R.id.seuilText);
                textView.setTextColor(Color.GREEN);
                textView.setText(Integer.toString(seuilValueG));
            }
        });

        seuilControlB.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                seuilValueB = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                TextView textView = (TextView) findViewById(R.id.seuilText);
                textView.setTextColor(Color.BLUE);
                textView.setText(Integer.toString(seuilValueB));
            }
        });
    }


    /**
     * Evenement du click boutton
     * @param v
     */
    public void btGalleryClick(View v) {
        //Création puis ouverture de la boite de dialogue
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, ""), SELECT_PICTURE);
    }

    public boolean btSaveClick(View v) {
        String fullPath = "";
        OutputStream fOut = null;
        File file;

        try {
            fullPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Saved Images";

            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            file = new File(fullPath, "image.png");
            file.createNewFile();
            fOut = new FileOutputStream(file);
        }
        catch (Exception e) {
            fullPath = getFilesDir().getAbsolutePath() + "/Saved Images";

            File dir = new File(fullPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            file = new File(fullPath, "image.png");
            try {
                file.createNewFile();
                fOut = new FileOutputStream(file);
            }
            catch (Exception e2){
                Log.e("Save Image", e.getMessage());
                TextView textView = (TextView) findViewById(R.id.seuilText);
                textView.setTextColor(Color.BLACK);
                textView.setText("Error saving.");
                return false;
            }
        }

        try {
            ImageView iv = (ImageView) findViewById(R.id.imageView1);

            if (iv.getDrawable() == null) {
                TextView textView = (TextView) findViewById(R.id.seuilText);
                textView.setTextColor(Color.BLACK);
                textView.setText("No image.");

                return false;
            }

            BitmapDrawable bitmapDrawable = ((BitmapDrawable) iv.getDrawable());
            Bitmap bitmap = bitmapDrawable.getBitmap();

            // 100 means no compression, the lower you go, the stronger the compression
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();

            MediaStore.Images.Media.insertImage(this.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

            return true;


        } catch (Exception e) {
            Log.e("Save Image", e.getMessage());
            TextView textView = (TextView) findViewById(R.id.seuilText);
            textView.setTextColor(Color.BLACK);
            textView.setText("Error saving.");
            return false;
        }
    }

    public void btSeuilClick(View v) {

        // On sélectionne l'image affichée et on la lit en bitmap.

        ImageView iv = (ImageView) findViewById(R.id.imageView1);

        if (iv.getDrawable() == null) {
            TextView textView = (TextView) findViewById(R.id.seuilText);
            textView.setTextColor(Color.BLACK);
            textView.setText("No image.");

            return;
        }

        BitmapDrawable bitmapDrawable = ((BitmapDrawable) iv.getDrawable());
        Bitmap bitmap = bitmapDrawable.getBitmap();

        // Si l'image a déjà été rentrée dans la table pixels, on ne la relit pas.

        if (!imageRead)
            pixels = new int[bitmap.getWidth()][bitmap.getHeight()];

        // pixels2 est la table de l'image à retourner

        int[] pixels2 = new int[bitmap.getHeight()*bitmap.getWidth()];

        int red,green,blue,RSeuil,GSeuil,BSeuil;

        for (int x=0;x<bitmap.getWidth();x++) {
            for (int y=0; y <bitmap.getHeight();y++) {

                // Si l'image a déjà été rentrée dans la table pixels, on ne la relit pas.

                if (!imageRead)
                    pixels[x][y] = bitmap.getPixel(x,y);

                // On lit les 3 couleurs en [0..255]

                red = Color.red(pixels[x][y]);
                green = Color.green(pixels[x][y]);
                blue = Color.blue(pixels[x][y]);

                // On applique le seuil.

                if (red < seuilValueR) {
                    RSeuil = 0;
                }
                else {
                    RSeuil = 255;
                }

                if (green < seuilValueG) {
                    GSeuil = 0;
                }
                else {
                    GSeuil = 255;
                }

                if (blue < seuilValueB) {
                    BSeuil = 0;
                }
                else {
                    BSeuil = 255;
                }

                pixels2[bitmap.getWidth()*y+x] = Color.rgb(RSeuil,GSeuil,BSeuil);
            }
        }

        // Dans tous les cas on considère la table rentrée dans pixels.

        imageRead = true;

        // On remplace l'image affichée par l'image traitée.

        Bitmap bitmap2 = Bitmap.createBitmap(pixels2,bitmap.getWidth(),bitmap.getHeight(),bitmap.getConfig());
        iv.setImageBitmap(bitmap2);

        /*ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
        byte[] imageInByte = stream.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(imageInByte);*/
    }

    /**
     * Retour de la boite de dialogue
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView mImageView = (ImageView) findViewById(R.id.imageView1);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SELECT_PICTURE:
                    String path = getRealPathFromURI(data.getData());
                    Log.d("Choose Picture", path);
                    //Transformer la photo en Bitmap
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    //Afficher le Bitmap
                    mImageView.setImageBitmap(bitmap);
                    imageRead = false;
                    break;
            }
        }
    }

    /**
     * Obtenir le chemin vers une ressource
     * la fonction a été trouvé sur Stackoverflow
     * @param contentURI
     * @return
     */
    private String getRealPathFromURI(Uri contentURI) {
        String result;
        Cursor cursor = getContentResolver().query(contentURI, null, null, null, null);
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
