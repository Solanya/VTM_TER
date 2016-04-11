package com.vtm.solanya.imgtrt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class Main extends Activity {
    //constante pour définir l'id du type image

    final static int SELECT_PICTURE = 1;

    private SeekBar paramControl1 = null;
    private SeekBar paramControl2 = null;
    private SeekBar paramControl3 = null;

    int paramValue1 = 0;
    int paramValue2 = 0;
    int paramValue3 = 0;

    boolean imageRead = true;
    boolean cancelled = false;
    int[][] pixelsCurrent;
    int[][] pixelsOld;

    CharSequence imageProcess = "Seuil";
    CharSequence processes[] = new CharSequence[] {"Seuil", "Flou" , "Dilatation", "Erosion"};
    AlertDialog.Builder processChooser;

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;

    // TODO : Add file permissions to save images for Android 6.0+
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

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        paramControl1 = (SeekBar) findViewById(R.id.paramBar1);
        paramControl2 = (SeekBar) findViewById(R.id.paramBar2);
        paramControl3 = (SeekBar) findViewById(R.id.paramBar3);

        processChooser = new AlertDialog.Builder(this);
        processChooser.setTitle("Traitements");
        processChooser.setItems(processes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TextView textView = (TextView) findViewById(R.id.textBox);
                textView.setTextColor(Color.BLACK);
                paramValue1 = 0;
                paramValue2 = 0;
                paramValue3 = 0;

                if (which == 0) {
                    imageProcess = "Seuil";

                    paramControl1.setVisibility(View.VISIBLE);
                    paramControl1.setProgress(0);
                    paramControl1.setMax(255);
                    paramControl1.setBackgroundColor(Color.parseColor("#40ff0000"));

                    paramControl2.setVisibility(View.VISIBLE);
                    paramControl2.setProgress(0);
                    paramControl2.setMax(255);
                    paramControl2.setBackgroundColor(Color.parseColor("#4000ff00"));

                    paramControl3.setVisibility(View.VISIBLE);
                    paramControl3.setProgress(0);
                    paramControl3.setMax(255);
                    paramControl3.setBackgroundColor(Color.parseColor("#400000ff"));
                } else if (which == 1) {
                    imageProcess = "Flou";

                    paramControl1.setVisibility(View.VISIBLE);
                    paramControl1.setMax(5);
                    paramControl1.setProgress(0);
                    paramControl1.setBackgroundColor(Color.parseColor("#40000000"));

                    paramControl2.setVisibility(View.GONE);

                    paramControl3.setVisibility(View.GONE);
                } else if (which == 2) {
                    imageProcess = "Dilatation";

                    paramControl1.setVisibility(View.GONE);

                    paramControl2.setVisibility(View.GONE);

                    paramControl3.setVisibility(View.GONE);
                } else if (which == 3) {
                    imageProcess = "Erosion";

                    paramControl1.setVisibility(View.GONE);

                    paramControl2.setVisibility(View.GONE);

                    paramControl3.setVisibility(View.GONE);
                }

                textView.setText("Choix : " + imageProcess);
            }
        });


        // Bouton de chargement depuis la gallerie

        ((Button) findViewById(R.id.btLoad)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btLoadClick(v);
            }
        });

        // Bouton d'appel de l'appareil photo

        ((Button) findViewById(R.id.btCam)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btCamClick(v);
            }
        });

        // Bouton de sauvegarde de l'image

        ((Button) findViewById(R.id.btSave)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btSaveClick(v);
            }
        });

        // Bouton pour choisir le traitement

        ((Button) findViewById(R.id.btChoose)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btChooseClick(v);
            }
        });

        // Bouton pour appliquer le traitement

        ((Button) findViewById(R.id.btApply)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btApplyClick(v);
            }
        });

        // Bouton pour annuler/refaire le traitement

        ((Button) findViewById(R.id.btCancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btCancelClick(v);
            }
        });

        // Sliders de contrôle de seuil


        paramControl1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                paramValue1 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                TextView textView = (TextView) findViewById(R.id.textBox);
                textView.setTextColor(Color.RED);
                textView.setText(Integer.toString(paramValue1));
            }
        });

        paramControl2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                paramValue2 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                TextView textView = (TextView) findViewById(R.id.textBox);
                textView.setTextColor(Color.GREEN);
                textView.setText(Integer.toString(paramValue2));
            }
        });

        paramControl3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                paramValue3 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                TextView textView = (TextView) findViewById(R.id.textBox);
                textView.setTextColor(Color.BLUE);
                textView.setText(Integer.toString(paramValue3));
            }
        });
    }


    /**
     * Evenement du click bouton
     *
     * @param v
     */
    public void btLoadClick(View v) {
        //Création puis ouverture de la boite de dialogue
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, ""), SELECT_PICTURE);
    }

    public void btCamClick(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
    }

    public boolean btSaveClick(View v) {
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
            fullPath = getFilesDir().getAbsolutePath() + "/Saved Images";

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
                TextView textView = (TextView) findViewById(R.id.textBox);
                textView.setTextColor(Color.BLACK);
                textView.setText("Error saving.");
                return false;
            }
        }

        try {
            ImageView iv = (ImageView) findViewById(R.id.imageView1);

            if (iv.getDrawable() == null) {
                TextView textView = (TextView) findViewById(R.id.textBox);
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

            TextView textView = (TextView) findViewById(R.id.textBox);
            textView.setTextColor(Color.BLACK);
            textView.setText("Image saved.");

            return true;


        } catch (Exception e) {
            Log.e("Save Image", e.getMessage());
            TextView textView = (TextView) findViewById(R.id.textBox);
            textView.setTextColor(Color.BLACK);
            textView.setText("Error saving.");
            return false;
        }
    }

    public void btChooseClick(View v) {
        processChooser.show();
    }

    public void btApplyClick(View v) {
        if (imageProcess == "Seuil") {
            applySeuil();
        }
        else if (imageProcess == "Flou") {
            applyFlou();
        }

        findViewById(R.id.btCancel).setVisibility(View.VISIBLE);
        ((Button) findViewById(R.id.btCancel)).setText("Annuler");
        cancelled = false;
    }

    public void applySeuil(){

        int seuilValueR = paramValue1;
        int seuilValueG = paramValue2;
        int seuilValueB = paramValue3;

        // On sélectionne l'image affichée et on la lit en bitmap.

        ImageView iv = (ImageView) findViewById(R.id.imageView1);

        if (iv.getDrawable() == null) {
            TextView textView = (TextView) findViewById(R.id.textBox);
            textView.setTextColor(Color.BLACK);
            textView.setText("No image.");

            return;
        }

        BitmapDrawable bitmapDrawable = ((BitmapDrawable) iv.getDrawable());
        Bitmap bitmapCurrent = bitmapDrawable.getBitmap();

        // Si l'image a déjà été rentrée dans la table pixelsCurrent, on ne la relit pas.

        if (!imageRead) {
            pixelsCurrent = new int[bitmapCurrent.getWidth()][bitmapCurrent.getHeight()];
            pixelsOld = new int[bitmapCurrent.getWidth()][bitmapCurrent.getHeight()];
        }

        // pixelsTemp est la table de l'image à retourner

        int[] pixelsTemp = new int[bitmapCurrent.getHeight() * bitmapCurrent.getWidth()];

        int red, green, blue, RSeuil, GSeuil, BSeuil;

        for (int x = 0; x < bitmapCurrent.getWidth(); x++) {
            for (int y = 0; y < bitmapCurrent.getHeight(); y++) {

                // Si l'image a déjà été rentrée dans la table pixelsCurrent, on ne la relit pas.

                if (!imageRead)
                    pixelsCurrent[x][y] = bitmapCurrent.getPixel(x, y);

                // On lit les 3 couleurs en [0..255]

                red = Color.red(pixelsCurrent[x][y]);
                green = Color.green(pixelsCurrent[x][y]);
                blue = Color.blue(pixelsCurrent[x][y]);

                // On applique le seuil.

                if (red < seuilValueR) {
                    RSeuil = 0;
                } else {
                    RSeuil = 255;
                }

                if (green < seuilValueG) {
                    GSeuil = 0;
                } else {
                    GSeuil = 255;
                }

                if (blue < seuilValueB) {
                    BSeuil = 0;
                } else {
                    BSeuil = 255;
                }

                pixelsTemp[bitmapCurrent.getWidth() * y + x] = Color.rgb(RSeuil, GSeuil, BSeuil);

                pixelsOld[x][y] = pixelsCurrent[x][y];
                pixelsCurrent[x][y] = pixelsTemp[bitmapCurrent.getWidth() * y + x];
            }
        }

        // Dans tous les cas on considère la table rentrée dans pixelsCurrent.

        imageRead = true;

        // On remplace l'image affichée par l'image traitée.

        Bitmap bitmapNew = Bitmap.createBitmap(pixelsTemp, bitmapCurrent.getWidth(), bitmapCurrent.getHeight(), bitmapCurrent.getConfig());
        iv.setImageBitmap(bitmapNew);

        /*ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
        byte[] imageInByte = stream.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(imageInByte);*/
    }

    public void applyFlou(){

        int flouWindow = paramValue1;
        System.out.println(flouWindow);

        // On sélectionne l'image affichée et on la lit en bitmap.

        ImageView iv = (ImageView) findViewById(R.id.imageView1);

        if (iv.getDrawable() == null) {
            TextView textView = (TextView) findViewById(R.id.textBox);
            textView.setTextColor(Color.BLACK);
            textView.setText("No image.");

            return;
        }

        BitmapDrawable bitmapDrawable = ((BitmapDrawable) iv.getDrawable());
        Bitmap bitmapCurrent = bitmapDrawable.getBitmap();

        // Si l'image a déjà été rentrée dans la table pixelsCurrent, on ne la relit pas.

        if (!imageRead) {
            pixelsCurrent = new int[bitmapCurrent.getWidth()][bitmapCurrent.getHeight()];
            pixelsOld = new int[bitmapCurrent.getWidth()][bitmapCurrent.getHeight()];
        }

        // pixelsTemp est la table de l'image à retourner

        int[] pixelsTemp = new int[bitmapCurrent.getHeight() * bitmapCurrent.getWidth()];

        int rFlou, gFlou, bFlou;

        for (int x = 0; x < bitmapCurrent.getWidth(); x++) {
            for (int y = 0; y < bitmapCurrent.getHeight(); y++) {

                // Si l'image a déjà été rentrée dans la table pixelsCurrent, on ne la relit pas.

                if (!imageRead)
                    pixelsCurrent[x][y] = bitmapCurrent.getPixel(x, y);
                    pixelsTemp[bitmapCurrent.getWidth() * y + x] = pixelsCurrent[x][y];
            }
        }

        for (int x = flouWindow; x < bitmapCurrent.getWidth()-flouWindow; x++) {
            for (int y = flouWindow; y < bitmapCurrent.getHeight()-flouWindow; y++) {

                rFlou = 0;
                gFlou = 0;
                bFlou = 0;

                // On applique le flou en lisant les couleurs en [0..255]
                for (int i = -flouWindow; i < flouWindow + 1; i++){
                    for (int j = -flouWindow; j < flouWindow + 1; j++){
                        rFlou += Color.red(pixelsCurrent[x+i][y+j]);
                        gFlou += Color.green(pixelsCurrent[x+i][y+j]);
                        bFlou += Color.blue(pixelsCurrent[x+i][y+j]);
                    }
                }

                rFlou /= java.lang.Math.pow(2 * flouWindow + 1, 2);
                gFlou /= java.lang.Math.pow(2 * flouWindow + 1, 2);
                bFlou /= java.lang.Math.pow(2 * flouWindow + 1, 2);

                pixelsTemp[bitmapCurrent.getWidth() * y + x] = Color.rgb(rFlou, gFlou, bFlou);

                pixelsOld[x][y] = pixelsCurrent[x][y];
                pixelsCurrent[x][y] = pixelsTemp[bitmapCurrent.getWidth() * y + x];
            }
        }

        // Dans tous les cas on considère la table rentrée dans pixelsCurrent.

        imageRead = true;

        // On remplace l'image affichée par l'image traitée.

        Bitmap bitmapNew = Bitmap.createBitmap(pixelsTemp, bitmapCurrent.getWidth(), bitmapCurrent.getHeight(), bitmapCurrent.getConfig());
        iv.setImageBitmap(bitmapNew);

        /*ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream);
        byte[] imageInByte = stream.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(imageInByte);*/
    }

    public void btCancelClick(View v) {
        ImageView iv = (ImageView) findViewById(R.id.imageView1);

        /*if (iv.getDrawable() == null) {
            TextView textView = (TextView) findViewById(R.id.textBox);
            textView.setTextColor(Color.BLACK);
            textView.setText("No image.");

            return;
        }*/

        int pxlSize = (pixelsCurrent.length) * (pixelsCurrent[0].length);
        int[] pixelsTemp = new int[pxlSize];

        System.out.println(pixelsCurrent.length);System.out.println(pixelsCurrent[0].length);

        for (int x=0; x<pixelsCurrent.length; x++){
            for (int y=0; y<pixelsCurrent[0].length; y++){
                pixelsTemp[pixelsCurrent.length * y + x] = pixelsOld[x][y];
                pixelsOld[x][y] = pixelsCurrent[x][y];
                pixelsCurrent[x][y] = pixelsTemp[pixelsCurrent.length * y + x];
            }
        }

        if (!cancelled){
            ((Button) findViewById(R.id.btCancel)).setText("Refaire");
            cancelled = true;
        }
        else {
            ((Button) findViewById(R.id.btCancel)).setText("Annuler");
            cancelled = false;
        }

        Bitmap bitmapNew = Bitmap.createBitmap(pixelsTemp, pixelsCurrent.length, pixelsCurrent[0].length, ((BitmapDrawable) iv.getDrawable()).getBitmap().getConfig());
        iv.setImageBitmap(bitmapNew);
    }


    /**
     * Retour de la boite de dialogue
     *
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
                    ((Button) findViewById(R.id.btCancel)).setText("Annuler");
                    ((Button) findViewById(R.id.btCancel)).setVisibility(View.GONE);
                    break;
                case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
                    path = getRealPathFromURI(data.getData());
                    Log.d("Choose Picture", path);
                    //Transformer la photo en Bitmap
                    bitmap = BitmapFactory.decodeFile(path);
                    //Afficher le Bitmap
                    mImageView.setImageBitmap(bitmap);
                    imageRead = false;
                    ((Button) findViewById(R.id.btCancel)).setText("Annuler");
                    ((Button) findViewById(R.id.btCancel)).setVisibility(View.GONE);
                    break;
            }
        }
        else if (resultCode == RESULT_CANCELED) {
            TextView textView = (TextView) findViewById(R.id.textBox);
            textView.setTextColor(Color.BLACK);
            textView.setText("Cancelled.");
        } else {
            TextView textView = (TextView) findViewById(R.id.textBox);
            textView.setTextColor(Color.BLACK);
            textView.setText("Error taking the picture.");
        }

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
