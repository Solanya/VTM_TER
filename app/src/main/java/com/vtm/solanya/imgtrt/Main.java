package com.vtm.solanya.imgtrt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class Main extends Activity {
    //constante pour définir l'id du type image

    final static int SELECT_PICTURE = 1;
    final static int SELECT_REFERENCE_PICTURE = 10;
    final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    public TextView messageBox = null;
    public TextView progressBox = null;
    public ImageView displayBox = null;
    public int imageWidth;
    public int imageHeight;
    public int referenceWidth;
    public int referenceHeight;

    public int progressCount = 0;

    public ProgressBar progressBarControl = null;

    public TableRow paramBarControlRow1 = null;
    public TableRow paramBarControlRow2 = null;
    public TableRow paramBarControlRow3 = null;
    public TableRow paramImgControlRow = null;
    public TableRow paramInputControlRow = null;
    public TableRow paramMatrixControlRow = null;

    public SeekBar paramBarControl1 = null;
    public SeekBar paramBarControl2 = null;
    public SeekBar paramBarControl3 = null;
    public EditText paramInputControl = null;

    public TextView paramBarControlText1 = null;
    public TextView paramBarControlText2 = null;
    public TextView paramBarControlText3 = null;
    public TextView paramImgControlText = null;
    public TextView paramInputControlText = null;
    public TextView paramMatrixControlText = null;

    public int paramBarValue1 = 0;
    public int paramBarValue2 = 0;
    public int paramBarValue3 = 0;
    public int[][] pixelsReference;
    public String paramInputValue;
    public int[][] paramMatrixValue;
    public int[][] paramMatrixTemp;
    public boolean isMatrixValid;

    public boolean cancelled = false;
    public int[][] pixelsCurrent;
    public int[][] pixelsOld;
    public int[] pixelsTemp;
    public Bitmap.Config bitmapConfig;

    public int xDelta;

    public int imageProcess = R.string.emptyProcess;
    public AlertDialog.Builder processCategoryChooser;



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

        messageBox = (TextView) findViewById(R.id.textBox);
        messageBox.setTextColor(Color.WHITE);
        messageBox.setText(R.string.welcome);

        progressBox = (TextView) findViewById(R.id.progressText);
        progressBox.setTextColor(Color.WHITE);

        displayBox = (ImageView) findViewById(R.id.imageDisplay);

        progressBarControl = (ProgressBar) findViewById(R.id.progressBar);

        paramBarControlRow1 = (TableRow) findViewById(R.id.paramBarRow1);
        paramBarControlRow2 = (TableRow) findViewById(R.id.paramBarRow2);
        paramBarControlRow3 = (TableRow) findViewById(R.id.paramBarRow3);
        paramImgControlRow = (TableRow) findViewById(R.id.paramImgRow);
        paramInputControlRow = (TableRow) findViewById(R.id.paramInputRow);
        paramMatrixControlRow = (TableRow) findViewById(R.id.paramMatrixRow);
        paramBarControlRow1.setVisibility(View.GONE);
        paramBarControlRow2.setVisibility(View.GONE);
        paramBarControlRow3.setVisibility(View.GONE);
        paramImgControlRow.setVisibility(View.GONE);
        paramInputControlRow.setVisibility(View.GONE);
        paramMatrixControlRow.setVisibility(View.GONE);

        paramBarControl1 = (SeekBar) findViewById(R.id.paramBar1);
        paramBarControl2 = (SeekBar) findViewById(R.id.paramBar2);
        paramBarControl3 = (SeekBar) findViewById(R.id.paramBar3);
        paramInputControl = (EditText) findViewById(R.id.paramInput);

        paramBarControlText1 = (TextView) findViewById(R.id.paramBarText1);
        paramBarControlText2 = (TextView) findViewById(R.id.paramBarText2);
        paramBarControlText3 = (TextView) findViewById(R.id.paramBarText3);
        paramImgControlText = (TextView) findViewById(R.id.paramImgText);
        paramInputControlText = (TextView) findViewById(R.id.paramInputText);
        paramMatrixControlText = (TextView) findViewById(R.id.paramMatrixText);

        processCategoryChooser = new AlertDialog.Builder(this);
        processCategoryChooser.setTitle(R.string.processChooserTitle);
        CharSequence processes[] = new CharSequence[] {this.getString(R.string.processSeuil), this.getString(R.string.processFlou), this.getString(R.string.processTest)};
        processCategoryChooser.setItems(processes, new DialogInterface.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {

                    imageProcess = R.string.processSeuil;
                    paramBarValue1 = 0;
                    paramBarValue2 = 0;
                    paramBarValue3 = 0;

                    paramBarControlRow1.setVisibility(View.VISIBLE);
                    paramBarControl1.setProgress(paramBarValue1);
                    paramBarControl1.setMax(255);
                    paramBarControl1.setBackgroundColor(Color.parseColor("#40ff0000"));
                    paramBarControlText1.setTextColor(Color.RED);
                    paramBarControlText1.setText(Integer.toString(paramBarValue1));

                    paramBarControlRow2.setVisibility(View.VISIBLE);
                    paramBarControl2.setProgress(paramBarValue2);
                    paramBarControl2.setMax(255);
                    paramBarControl2.setBackgroundColor(Color.parseColor("#4000ff00"));
                    paramBarControlText2.setTextColor(Color.GREEN);
                    paramBarControlText2.setText(Integer.toString(paramBarValue2));

                    paramBarControlRow3.setVisibility(View.VISIBLE);
                    paramBarControl3.setProgress(paramBarValue3);
                    paramBarControl3.setMax(255);
                    paramBarControl3.setBackgroundColor(Color.parseColor("#400000ff"));
                    paramBarControlText3.setTextColor(Color.parseColor("#8888ff"));
                    paramBarControlText3.setText(Integer.toString(paramBarValue3));

                } else if (which == 1) {

                    imageProcess = R.string.processFlou;
                    paramBarValue1 = 0;

                    paramBarControlRow1.setVisibility(View.VISIBLE);
                    paramBarControl1.setProgress(paramBarValue1);
                    paramBarControl1.setMax(5);
                    paramBarControl1.setBackgroundColor(Color.parseColor("#40000000"));
                    paramBarControlText1.setTextColor(Color.WHITE);
                    paramBarControlText1.setText(Integer.toString(paramBarValue1));

                    paramBarControlRow2.setVisibility(View.GONE);

                    paramBarControlRow3.setVisibility(View.GONE);

                } else if (which == 2) {
                    imageProcess = R.string.processTest;
                    paramImgControlRow.setVisibility(View.VISIBLE);
                    paramImgControlText.setTextColor(Color.WHITE);
                    paramImgControlText.setText(R.string.imageReferenceEmpty);

                    paramInputControl.setText("");
                    paramInputControl.setTextColor(Color.LTGRAY);
                    paramInputControlRow.setVisibility(View.VISIBLE);
                    paramInputControlText.setTextColor(Color.WHITE);

                    paramMatrixControlRow.setVisibility(View.VISIBLE);
                    paramMatrixControlText.setText(R.string.matrixConvolutionText);
                    paramMatrixControlText.setTextColor(Color.WHITE);
                    paramMatrixValue = new int[5][5];
                    for (int i = 0; i < 5; i++) {
                        for (int j = 0; j < 5; j++) {
                            paramMatrixValue[i][j] = 0;
                        }
                    }
                }

                findViewById(R.id.btApply).setVisibility(View.VISIBLE);
                messageBox.setText(String.format(getString(R.string.choice), getString(imageProcess)));
                ((ViewFlipper) findViewById(R.id.menuFlipper)).showNext();
            }
        });

        ((ViewFlipper) findViewById(R.id.menuFlipper)).showNext();
        ((ViewFlipper) findViewById(R.id.menuFlipper)).showPrevious();
        ((ViewFlipper) findViewById(R.id.menuFlipper)).setInAnimation(this, R.anim.slide_in_from_right);
        ((ViewFlipper) findViewById(R.id.menuFlipper)).setOutAnimation(this, R.anim.slide_out_to_left);
        findViewById(R.id.menuFlipper).setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return menuFlip(event);
            }
        });


        // Bouton de chargement depuis la gallerie

        findViewById(R.id.btLoad).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btLoadClick(v);
            }
        });

        // Bouton d'appel de l'appareil photo

        findViewById(R.id.btCam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btCamClick(v);
            }
        });

        // Bouton de sauvegarde de l'image

        findViewById(R.id.btSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btSaveClick();
            }
        });

        // Bouton pour choisir le traitement

        findViewById(R.id.btChoose).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btChooseClick(v);
            }
        });

        // Bouton pour appliquer le traitement

        findViewById(R.id.btApply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btApplyClick(v);
            }
        });

        // Bouton pour annuler/refaire le traitement

        findViewById(R.id.btUndo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btUndoClick(v);
            }
        });

        // Bouton pour choisir une image de référence

        findViewById(R.id.btReference).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                btReferenceClick(v);
            }
        });

        // Bouton pour éditer la matrice

        findViewById(R.id.btMatrix).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                btMatrixClick(v);
            }
        });

        // Réglage direct des paramètres

        findViewById(R.id.paramBarText1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeParamBar1(v);
            }
        });

        findViewById(R.id.paramBarText2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeParamBar2(v);
            }
        });

        findViewById(R.id.paramBarText3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeParamBar3(v);
            }
        });

        // Sliders de contrôle de seuil


        paramBarControl1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                paramBarValue1 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                paramBarControlText1.setText(Integer.toString(paramBarValue1));
            }
        });

        paramBarControl2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                paramBarValue2 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                paramBarControlText2.setText(Integer.toString(paramBarValue2));
            }
        });

        paramBarControl3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                paramBarValue3 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                paramBarControlText3.setText(Integer.toString(paramBarValue3));
            }
        });

        if(getWindow().getDecorView().getRootView().getViewTreeObserver().isAlive()){
            getWindow().getDecorView().getRootView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    View[] viewTable = {findViewById(R.id.btLoad),
                                        findViewById(R.id.btCam),
                                        findViewById(R.id.btSave),
                                        findViewById(R.id.btChoose),
                                        findViewById(R.id.btApply),
                                        findViewById(R.id.btUndo)};
                    for (View v:viewTable) {
                        v.setLayoutParams(new TableRow.LayoutParams(v.getMeasuredHeight(), v.getMeasuredHeight()));
                    }

                    getWindow().getDecorView().getRootView().getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
            });
        }
    }

    @SuppressLint("SetTextI18n")
    public void enableParamBar1(int max, String textColor){
        paramBarValue1 = 0;
        paramBarControlRow1.setVisibility(View.VISIBLE);
        paramBarControl1.setProgress(paramBarValue1);
        paramBarControl1.setMax(max);
        //paramBarControl1.setBackgroundColor(Color.parseColor("#40ff0000"));
        paramBarControlText1.setTextColor(Color.parseColor(textColor));
        paramBarControlText1.setText(Integer.toString(paramBarValue1));
    }

    public void disableParamBar1(){
        paramBarControlRow1.setVisibility(View.GONE);
    }

    public void enableParamBar2(int max, String textColor){
        paramBarValue2 = 0;
        paramBarControlRow2.setVisibility(View.VISIBLE);
        paramBarControl2.setProgress(paramBarValue2);
        paramBarControl2.setMax(max);
        //paramBarControl2.setBackgroundColor(Color.parseColor("#40ff0000"));
        paramBarControlText2.setTextColor(Color.parseColor(textColor));
        paramBarControlText2.setText(Integer.toString(paramBarValue2));
    }

    public void disableParamBar2(){
        paramBarControlRow2.setVisibility(View.GONE);
    }

    public void enableParamBar3(int max, String textColor){
        paramBarValue3 = 0;
        paramBarControlRow3.setVisibility(View.VISIBLE);
        paramBarControl3.setProgress(paramBarValue3);
        paramBarControl3.setMax(max);
        //paramBarControl2.setBackgroundColor(Color.parseColor("#40ff0000"));
        paramBarControlText3.setTextColor(Color.parseColor(textColor));
        paramBarControlText3.setText(Integer.toString(paramBarValue3));
    }

    public void disableParamBar3(){
        paramBarControlRow3.setVisibility(View.GONE);
    }

    public void enableParamImg(){
        pixelsReference = null;
        paramImgControlRow.setVisibility(View.VISIBLE);
        paramImgControlText.setTextColor(Color.WHITE);
        paramImgControlText.setText(R.string.imageReferenceEmpty);
        findViewById(R.id.btApply).setVisibility(View.INVISIBLE);
    }

    public void disableParamImg(){
        paramImgControlRow.setVisibility(View.GONE);
    }

    public void enableParamInput(){
        paramInputControl.setText("");
        paramInputControl.setTextColor(Color.LTGRAY);
        paramInputControlRow.setVisibility(View.VISIBLE);
        paramInputControlText.setTextColor(Color.WHITE);
    }

    public void disableParamInput(){
        paramInputControlRow.setVisibility(View.GONE);
    }

    public void enableParamMatrix(int matrixNameID){
        paramMatrixControlRow.setVisibility(View.VISIBLE);
        paramMatrixControlText.setText(matrixNameID);
        paramMatrixControlText.setTextColor(Color.WHITE);
        paramMatrixValue = new int[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                paramMatrixValue[i][j] = 0;
            }
        }
    }

    public void enableParamMatrix(String matrixNameStr){
        paramMatrixControlRow.setVisibility(View.VISIBLE);
        paramMatrixControlText.setText(matrixNameStr);
        paramMatrixControlText.setTextColor(Color.WHITE);
        paramMatrixValue = new int[5][5];
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                paramMatrixValue[i][j] = 0;
            }
        }
    }

    public void disableParamMatrix(){
        paramMatrixControlRow.setVisibility(View.GONE);
    }

    public boolean menuFlip(MotionEvent event) {
        final int X = (int) event.getRawX();
        switch (event.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:
                xDelta = X;
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                break;
            case MotionEvent.ACTION_MOVE:
                if (((ViewFlipper) findViewById(R.id.menuFlipper)).getDisplayedChild() != 1
                        & (X-xDelta) < -Resources.getSystem().getDisplayMetrics().widthPixels/3){
                    ((ViewFlipper) findViewById(R.id.menuFlipper)).setInAnimation(this,R.anim.slide_in_from_right);
                    ((ViewFlipper) findViewById(R.id.menuFlipper)).setOutAnimation(this, R.anim.slide_out_to_left);
                    ((ViewFlipper) findViewById(R.id.menuFlipper)).showNext();
                    if (imageProcess == R.string.emptyProcess){
                        messageBox.setText(R.string.emptyProcess);
                    }
                }
                else if (((ViewFlipper) findViewById(R.id.menuFlipper)).getDisplayedChild() != 0
                        & (X-xDelta) > Resources.getSystem().getDisplayMetrics().widthPixels/3){
                    ((ViewFlipper) findViewById(R.id.menuFlipper)).setInAnimation(this,R.anim.slide_in_from_left);
                    ((ViewFlipper) findViewById(R.id.menuFlipper)).setOutAnimation(this,R.anim.slide_out_to_right);
                    ((ViewFlipper) findViewById(R.id.menuFlipper)).showPrevious();
                }
                break;
        }
        return true;
    }

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

    public boolean btSaveClick() {
        String fullPath;
        OutputStream fOut;
        File file;

        try {
            fullPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "Saved Images";

            File dir = new File(fullPath);
            if (!dir.exists()) {
                boolean checkDirCreation = dir.mkdirs();
                if (!checkDirCreation){
                    messageBox.setText(R.string.saveError);
                    return false;
                }
            }

            file = new File(fullPath, "image.png");
            boolean checkFileCreation = file.createNewFile();
            if (!checkFileCreation){
                messageBox.setText(R.string.saveError);
                return false;
            }
            fOut = new FileOutputStream(file);
        } catch (Exception e) {
            fullPath = getFilesDir().getAbsolutePath() + "/Saved Images";

            File dir = new File(fullPath);
            if (!dir.exists()) {
                boolean checkDirCreation = dir.mkdirs();
                if (!checkDirCreation){
                    messageBox.setText(R.string.saveError);
                    return false;
                }
            }

            file = new File(fullPath, "image.png");
            try {
                boolean checkFileCreation = file.createNewFile();
                if (!checkFileCreation){
                    messageBox.setText(R.string.saveError);
                    return false;
                }
                fOut = new FileOutputStream(file);
            } catch (Exception e2) {
                Log.e("Save Image", e.getMessage());
                messageBox.setText(R.string.saveError);
                return false;
            }
        }

        try {
            if (displayBox.getDrawable() == null) {
                messageBox.setText(R.string.imageEmpty);

                return false;
            }

            BitmapDrawable bitmapDrawable = ((BitmapDrawable) displayBox.getDrawable());
            Bitmap bitmap = bitmapDrawable.getBitmap();

            // 100 means no compression, the lower you go, the stronger the compression
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();

            MediaStore.Images.Media.insertImage(this.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());

            messageBox.setText(R.string.saveSuccess);

            return true;


        } catch (Exception e) {
            Log.e("Save Image", e.getMessage());
            messageBox.setText(R.string.saveError);
            return false;
        }
    }

    public void btChooseClick(View v) {
        ((ViewFlipper) findViewById(R.id.menuFlipper)).setInAnimation(this, R.anim.slide_in_from_right);
        ((ViewFlipper) findViewById(R.id.menuFlipper)).setOutAnimation(this, R.anim.slide_out_to_left);
        processCategoryChooser.show();
    }

    private class AsyncApplyTask extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected Void doInBackground(Void... params) {
            if (imageProcess == R.string.processSeuil) {
                applySeuil();
            }
            else if (imageProcess == R.string.processFlou) {
                applyFlou();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            Bitmap bitmapNew = Bitmap.createBitmap(pixelsTemp, imageWidth, imageHeight, bitmapConfig);
            displayBox.setImageBitmap(bitmapNew);
            ((ViewFlipper) findViewById(R.id.menuFlipper)).showPrevious();
        }
    }

    public void btApplyClick(View v) {

        if (imageProcess == R.string.emptyProcess){
            messageBox.setText(R.string.emptyProcess);

            return;
        }
        else{
            if (displayBox.getDrawable() == null) {
                messageBox.setText(R.string.imageEmpty);

                return;
            }

            BitmapDrawable bitmapDrawable = ((BitmapDrawable) displayBox.getDrawable());
            Bitmap bitmapCurrent = bitmapDrawable.getBitmap();
            bitmapConfig = bitmapCurrent.getConfig();


            for (int x=0; x<imageWidth; x++){
                /*for (int y=0; y<imageHeight; y++){
                    pixelsOld[x][y] = pixelsCurrent[x][y];
                }*/
                System.arraycopy(pixelsCurrent[x],0,pixelsOld[x],0,imageHeight);
            }

            progressBox.setText(String.format(getString(R.string.applyingProcess), getString(imageProcess)));
            progressCount = 0;
            progressBarControl.setProgress(0);
            progressBarControl.setMax(imageHeight * imageWidth);
            ((ViewFlipper) findViewById(R.id.menuFlipper)).setInAnimation(this, R.anim.slide_in_from_bottom);
            ((ViewFlipper) findViewById(R.id.menuFlipper)).setOutAnimation(this, R.anim.slide_out_to_top);
            ((ViewFlipper) findViewById(R.id.menuFlipper)).showNext();
            ((ViewFlipper) findViewById(R.id.menuFlipper)).setInAnimation(this, R.anim.slide_in_from_top);
            ((ViewFlipper) findViewById(R.id.menuFlipper)).setOutAnimation(this, R.anim.slide_out_to_bottom);

            new AsyncApplyTask().execute();
        }

        findViewById(R.id.btUndo).setVisibility(View.VISIBLE);
        findViewById(R.id.btUndo).setBackgroundResource(R.mipmap.ic_undo);
        cancelled = false;
    }

    public void applySeuil(){

        int seuilValueR = paramBarValue1;
        int seuilValueG = paramBarValue2;
        int seuilValueB = paramBarValue3;

        // pixelsTemp est la table de l'image à retourner

        int red, green, blue, RSeuil, GSeuil, BSeuil;

        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {

                // On lit les 3 couleurs en [0..255]

                red = Color.red(pixelsOld[x][y]);
                green = Color.green(pixelsOld[x][y]);
                blue = Color.blue(pixelsOld[x][y]);

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

                toPixelRGB(x, y, RSeuil, GSeuil, BSeuil);
            }
        }

    }

    public void applyFlou(){

        int flouWindow = paramBarValue1;

        int rFlou, gFlou, bFlou;

        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {

                // Si l'image a déjà été rentrée dans la table pixelsCurrent, on ne la relit pas.

                toPixelCopy(x,y,pixelsOld[x][y]);
            }
        }

        for (int x = flouWindow; x < imageWidth-flouWindow; x++) {
            for (int y = flouWindow; y < imageHeight-flouWindow; y++) {

                rFlou = 0;
                gFlou = 0;
                bFlou = 0;

                // On applique le flou en lisant les couleurs en [0..255]
                for (int i = -flouWindow; i < flouWindow + 1; i++){
                    for (int j = -flouWindow; j < flouWindow + 1; j++){
                        rFlou += Color.red(pixelsOld[x + i][y + j]);
                        gFlou += Color.green(pixelsOld[x + i][y + j]);
                        bFlou += Color.blue(pixelsOld[x + i][y + j]);
                    }
                }

                rFlou /= java.lang.Math.pow(2 * flouWindow + 1, 2);
                gFlou /= java.lang.Math.pow(2 * flouWindow + 1, 2);
                bFlou /= java.lang.Math.pow(2 * flouWindow + 1, 2);

                toPixelRGB(x, y, rFlou, gFlou, bFlou);
            }
        }
    }

    public void toPixelCopy(int x, int y, int C){
        pixelsTemp[imageWidth * y + x] = C;
        pixelsCurrent[x][y] = pixelsTemp[imageWidth * y + x];
        //progressUpdate();
    }

    public void toPixelRGB(int x, int y, int R, int G, int B){
        pixelsTemp[imageWidth * y + x] = Color.rgb(R,G,B);
        pixelsCurrent[x][y] = pixelsTemp[imageWidth * y + x];
        progressUpdate();
    }

    public void progressUpdate(){
        progressCount += 1;
        if ((progressCount%1000 == 0) | (progressCount == imageHeight*imageWidth)) {
            progressBarControl.setProgress(progressCount);
        }
    }

    public void btUndoClick(View v) {

        int pxlSize = (pixelsCurrent.length) * (pixelsCurrent[0].length);
        int[] pixelsTemp = new int[pxlSize];

        for (int x=0; x<pixelsCurrent.length; x++){
            for (int y=0; y<pixelsCurrent[0].length; y++){
                pixelsTemp[pixelsCurrent.length * y + x] = pixelsOld[x][y];
                pixelsOld[x][y] = pixelsCurrent[x][y];
                pixelsCurrent[x][y] = pixelsTemp[pixelsCurrent.length * y + x];
            }
        }

        if (!cancelled){
            findViewById(R.id.btUndo).setBackgroundResource(R.mipmap.ic_redo);
            cancelled = true;
        }
        else {
            findViewById(R.id.btUndo).setBackgroundResource(R.mipmap.ic_undo);
            cancelled = false;
        }

        Bitmap bitmapNew = Bitmap.createBitmap(pixelsTemp, pixelsCurrent.length, pixelsCurrent[0].length, ((BitmapDrawable) displayBox.getDrawable()).getBitmap().getConfig());
        displayBox.setImageBitmap(bitmapNew);
    }

    public void btReferenceClick(View v) {
        //Création puis ouverture de la boite de dialogue
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, ""), SELECT_REFERENCE_PICTURE);
    }

    @SuppressLint("SetTextI18n")
    public void btMatrixClick(View v) {
        paramMatrixTemp = new int[5][5];
        isMatrixValid = true;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.matrixConvolutionTitle);

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        layout.setGravity(Gravity.CENTER);

        final TextView warning = new TextView(this);
        warning.setText(R.string.valueMatrixWarning);
        warning.setGravity(Gravity.CENTER);
        warning.setVisibility(View.VISIBLE);
        warning.setTextColor(Color.WHITE);

        final LinearLayout layoutMatrixColumns = new LinearLayout(this);
        layoutMatrixColumns.setOrientation(GridLayout.HORIZONTAL);
        layoutMatrixColumns.setGravity(Gravity.CENTER);

        final EditText[][] input = new EditText[5][5];
        for (int i=0;i<5;i++){
            final LinearLayout layoutMatrixRow = new LinearLayout(this);
            layoutMatrixRow.setOrientation(GridLayout.VERTICAL);
            for (int j=0;j<5;j++){
                input[i][j] = new EditText(this);
                input[i][j].setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
                input[i][j].setTextColor(Color.LTGRAY);
                input[i][j].setLayoutParams(new LinearLayout.LayoutParams((int)((getResources().getDisplayMetrics().widthPixels)*0.15),LinearLayout.LayoutParams.WRAP_CONTENT));
                if (paramMatrixValue[i][j] != 0) {
                    input[i][j].setText(Integer.toString(paramMatrixValue[i][j]));
                } else {
                    input[i][j].setText("");
                }
                layoutMatrixRow.addView(input[i][j]);
            }
            layoutMatrixColumns.addView(layoutMatrixRow);
        }

        layout.addView(warning);
        layout.addView(layoutMatrixColumns);
        builder.setView(layout);
        builder.setPositiveButton(R.string.dialogOK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for (int i = 0; i < 5; i++) {
                    for (int j = 0; j < 5; j++) {
                        String m_Text = input[i][j].getText().toString();
                        int m_int;
                        if (!m_Text.equals("")) {
                            m_int = Integer.parseInt(m_Text);
                        } else {
                            m_int = 0;
                        }

                        if (m_int < 1000) {
                            paramMatrixTemp[i][j] = m_int;

                        } else {
                            messageBox.setText(String.format(getString(R.string.valueMatrixError), i + 1, j + 1));
                            isMatrixValid = false;
                            break;
                        }
                    }
                }

                if (isMatrixValid) {
                    for (int i = 0; i < 5; i++) {
                        System.arraycopy(paramMatrixTemp[i], 0, paramMatrixValue[i], 0, 5);
                    }
                    messageBox.setText(R.string.valueMatrixSuccess);
                }
            }
        });
        builder.setNegativeButton(R.string.dialogCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void changeParamBar1(View v){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.valueChangeTitle);

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView warning = new TextView(this);
        warning.setText(String.format(getString(R.string.valueBarWarning), paramBarControl1.getMax()));
        warning.setGravity(Gravity.CENTER);
        warning.setVisibility(View.VISIBLE);
        warning.setTextColor(Color.WHITE);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        layout.addView(warning);
        layout.addView(input);
        builder.setView(layout);

        builder.setPositiveButton(R.string.dialogOK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_Text = input.getText().toString();
                int m_int = Integer.parseInt(m_Text);

                if (m_int <= paramBarControl1.getMax()) {
                    paramBarControlText1.setText(m_Text);
                    paramBarValue1 = m_int;
                    paramBarControl1.setProgress(m_int);
                    messageBox.setText(R.string.valueBarSuccess);
                } else {
                    messageBox.setText(String.format(getString(R.string.valueBarError), paramBarControl1.getMax()));
                }
            }
        });
        builder.setNegativeButton(R.string.dialogCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void changeParamBar2(View v){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.valueChangeTitle);

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView warning = new TextView(this);
        warning.setText(String.format(getString(R.string.valueBarWarning), paramBarControl2.getMax()));
        warning.setGravity(Gravity.CENTER);
        warning.setVisibility(View.VISIBLE);
        warning.setTextColor(Color.WHITE);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        layout.addView(warning);
        layout.addView(input);
        builder.setView(layout);

        builder.setPositiveButton(R.string.dialogOK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_Text = input.getText().toString();
                int m_int = Integer.parseInt(m_Text);

                if (m_int <= paramBarControl2.getMax()) {
                    paramBarControlText2.setText(m_Text);
                    paramBarValue2 = m_int;
                    paramBarControl2.setProgress(m_int);
                    messageBox.setText(R.string.valueBarSuccess);
                } else {
                    messageBox.setText(String.format(getString(R.string.valueBarError), paramBarControl2.getMax()));
                }
            }
        });
        builder.setNegativeButton(R.string.dialogCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public void changeParamBar3(View v){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.valueChangeTitle);

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView warning = new TextView(this);
        warning.setText(String.format(getString(R.string.valueBarWarning), paramBarControl3.getMax()));
        warning.setGravity(Gravity.CENTER);
        warning.setVisibility(View.VISIBLE);
        warning.setTextColor(Color.WHITE);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        layout.addView(warning);
        layout.addView(input);
        builder.setView(layout);

        builder.setPositiveButton(R.string.dialogOK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_Text = input.getText().toString();
                int m_int = Integer.parseInt(m_Text);

                if (m_int <= paramBarControl3.getMax()) {
                    paramBarControlText3.setText(m_Text);
                    paramBarValue3 = m_int;
                    paramBarControl3.setProgress(m_int);
                    messageBox.setText(R.string.valueBarSuccess);
                } else {
                    messageBox.setText(String.format(getString(R.string.valueBarError), paramBarControl3.getMax()));
                }
            }
        });
        builder.setNegativeButton(R.string.dialogCancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    public String getParamInputValue(){
        paramInputValue = (paramInputControl.getText().toString());
        return paramInputValue;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        ImageView mImageView = (ImageView) findViewById(R.id.imageDisplay);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SELECT_PICTURE:
                    String path = getRealPathFromURI(data.getData());
                    Log.d("Choose Picture", path);
                    //Transformer la photo en Bitmap
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    //Afficher le Bitmap
                    mImageView.setImageBitmap(bitmap);
                    imageWidth = bitmap.getWidth();
                    imageHeight = bitmap.getHeight();

                    pixelsCurrent = new int[imageWidth][imageHeight];
                    pixelsOld = new int[imageWidth][imageHeight];
                    pixelsTemp = new int[imageWidth * imageHeight];

                    for (int x = 0; x < imageWidth; x++) {
                        for (int y = 0; y < imageHeight; y++) {
                            pixelsCurrent[x][y] = bitmap.getPixel(x, y);
                        }
                    }

                    findViewById(R.id.btUndo).setBackgroundResource(R.mipmap.ic_undo);
                    findViewById(R.id.btUndo).setVisibility(View.INVISIBLE);
                    break;
                case SELECT_REFERENCE_PICTURE:
                    path = getRealPathFromURI(data.getData());
                    Log.d("Choose Ref Picture", path);
                    //Transformer la photo en Bitmap
                    bitmap = BitmapFactory.decodeFile(path);
                    referenceWidth = bitmap.getWidth();
                    referenceHeight = bitmap.getHeight();

                    pixelsReference = new int[referenceWidth][referenceHeight];

                    //TODO : Histogram here ?

                    for (int x = 0; x < referenceWidth; x++) {
                        for (int y = 0; y < referenceHeight; y++) {
                            pixelsReference[x][y] = bitmap.getPixel(x, y);
                        }
                    }

                    String[] pathSplit = path.split("/");

                    paramImgControlText.setText(pathSplit[pathSplit.length-1]);
                    findViewById(R.id.btApply).setVisibility(View.VISIBLE);

                    break;
                case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
                    path = getRealPathFromURI(data.getData());
                    Log.d("Choose Picture", path);
                    //Transformer la photo en Bitmap
                    bitmap = BitmapFactory.decodeFile(path);
                    //Afficher le Bitmap
                    mImageView.setImageBitmap(bitmap);
                    imageWidth = bitmap.getWidth();
                    imageHeight = bitmap.getHeight();

                    pixelsCurrent = new int[imageWidth][imageHeight];
                    pixelsOld = new int[imageWidth][imageHeight];
                    pixelsTemp = new int[imageWidth * imageHeight];

                    for (int x = 0; x < imageWidth; x++) {
                        for (int y = 0; y < imageHeight; y++) {
                            pixelsCurrent[x][y] = bitmap.getPixel(x, y);
                        }
                    }

                    findViewById(R.id.btUndo).setBackgroundResource(R.mipmap.ic_undo);
                    findViewById(R.id.btUndo).setVisibility(View.INVISIBLE);
                    break;
            }
        }
        else if (resultCode == RESULT_CANCELED) {
            messageBox.setText(R.string.camCancelled);
        } else {
            messageBox.setText(R.string.camError);
        }

    }

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

    @Override
    public void onBackPressed(){
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.exitTitle)
                .setMessage(R.string.exitMessage)
                .setPositiveButton(R.string.exitYes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                })
                .setNegativeButton(R.string.exitNo, null)
                .show();
    }
}
