package com.vtm.solanya.imgtrt;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.InputType;
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

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

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
    public ProgressBar progressLoadBarControl = null;

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
    public int paramMatrixNorme;
    public boolean isMatrixValid;

    public boolean cancelled = false;
    public int[][] pixelsCurrent;
    public int[][] pixelsOld;
    public int[] pixelsTemp;
    public Bitmap.Config bitmapConfig;

    public Target target;

    public int xDelta;

    public int imageProcess = R.string.emptyProcess;
    public AlertDialog.Builder processCategoryChooser;
    public AlertDialog.Builder processHistogramChooser;
    public AlertDialog.Builder processMorphologyChooser;
    public AlertDialog.Builder processFilterChooser;
    public AlertDialog.Builder processCosmeticChooser;

    public AsyncApplyTask applyTask;



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

        messageBox = (TextView) findViewById(R.id.messageBox);
        messageBox.setTextColor(Color.WHITE);
        messageBox.setText(R.string.welcome);

        progressBox = (TextView) findViewById(R.id.progressText);
        progressBox.setTextColor(Color.WHITE);

        displayBox = (ImageView) findViewById(R.id.imageDisplay);

        progressBarControl = (ProgressBar) findViewById(R.id.progressBar);
        progressLoadBarControl = (ProgressBar) findViewById(R.id.imageLoadProgress);

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
        processHistogramChooser = new AlertDialog.Builder(this);
        processMorphologyChooser = new AlertDialog.Builder(this);
        processFilterChooser = new AlertDialog.Builder(this);
        processCosmeticChooser = new AlertDialog.Builder(this);

        processCategoryChooser.setTitle(R.string.processCategoryChooserTitle);
        //CharSequence processes[] = new CharSequence[] {this.getString(R.string.processSeuil), this.getString(R.string.processFlou), this.getString(R.string.processTest)};
        CharSequence processCategories[] = new CharSequence[] {
                getString(R.string.processCategoryHistogram),
                getString(R.string.processCategoryMorphology),
                getString(R.string.processCategoryFilter),
                getString(R.string.processCategoryCosmetic)
        };
        processCategoryChooser.setItems(processCategories, new DialogInterface.OnClickListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onClick(DialogInterface dialog, int whichCategory) {

                // Modification d'histogramme
                if (whichCategory == 0) {

                    processHistogramChooser.setTitle(R.string.processHistogramChooserTitle);
                    CharSequence processesHistogram[] = new CharSequence[] {
                            getString(R.string.processHistogramSeuil),
                            getString(R.string.processHistogramSpecification),
                            getString(R.string.processHistogramEgalisation),
                            getString(R.string.processHistogramExpansion)
                    };
                    processHistogramChooser.setItems(processesHistogram, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichProcess) {

                            // Seuil
                            if (whichProcess == 0) {

                                imageProcess = R.string.processHistogramSeuil;

                                enableParamBar1(255, "#ff0000");
                                enableParamBar2(255, "#00ff00");
                                enableParamBar3(255, "#6666ff");

                                disableParamImg();
                                disableParamInput();
                                disableParamMatrix();
                            }
                            // Spécification
                            else if (whichProcess == 1) {

                                imageProcess = R.string.processHistogramSpecification;

                                disableParamBar1();
                                disableParamBar2();
                                disableParamBar3();

                                enableParamImg();

                                disableParamInput();
                                disableParamMatrix();
                            }
                            // Egalisation
                            else if (whichProcess == 2) {

                                imageProcess = R.string.processHistogramEgalisation;

                                disableParamBar1();
                                disableParamBar2();
                                disableParamBar3();
                                disableParamImg();
                                disableParamInput();
                                disableParamMatrix();
                            }
                            // Expansion
                            else if (whichProcess == 3) {

                                imageProcess = R.string.processHistogramExpansion;

                                enableParamBar1(49, "#ffffff");

                                disableParamBar2();
                                disableParamBar3();
                                disableParamImg();
                                disableParamInput();
                                disableParamMatrix();
                            }

                            messageBox.setText(String.format(getString(R.string.choice), getString(imageProcess)));
                            ((ViewFlipper) findViewById(R.id.menuFlipper)).showNext();
                        }
                    });
                    processHistogramChooser.show();

                }
                // Morphologie
                else if (whichCategory == 1) {

                    processMorphologyChooser.setTitle(R.string.processMorphologyChooserTitle);
                    CharSequence processesMorphology[] = new CharSequence[] {
                            getString(R.string.processMorphologyDilatation),
                            getString(R.string.processMorphologyErosion)
                    };
                    processMorphologyChooser.setItems(processesMorphology, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichProcess) {
                            // Dilatation
                            if (whichProcess == 0) {
                                imageProcess = R.string.processMorphologyDilatation;

                                disableParamBar1();
                                disableParamBar2();
                                disableParamBar3();
                                disableParamImg();
                                disableParamInput();
                                disableParamMatrix();
                            }
                            // Erosion
                            else if (whichProcess == 1) {
                                imageProcess = R.string.processMorphologyErosion;

                                disableParamBar1();
                                disableParamBar2();
                                disableParamBar3();
                                disableParamImg();
                                disableParamInput();
                                disableParamMatrix();
                            }

                            messageBox.setText(String.format(getString(R.string.choice), getString(imageProcess)));
                            ((ViewFlipper) findViewById(R.id.menuFlipper)).showNext();
                        }
                    });
                    processMorphologyChooser.show();

                }
                // Filtres
                else if (whichCategory == 2) {

                    processFilterChooser.setTitle(R.string.processFilterChooserTitle);
                    CharSequence processesFilter[] = new CharSequence[] {
                            getString(R.string.processFilterConvolution),
                            getString(R.string.processFilterMoyenneur),
                            getString(R.string.processFilterGaussien),
                            getString(R.string.processFilterKirsch)
                    };
                    processFilterChooser.setItems(processesFilter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichProcess) {
                            // Filtre de convolution
                            if (whichProcess == 0) {
                                imageProcess = R.string.processFilterConvolution;

                                disableParamBar1();
                                disableParamBar2();
                                disableParamBar3();
                                disableParamImg();
                                disableParamInput();

                                enableParamMatrix(R.string.matrixConvolutionText);

                            }
                            // Filtre moyenneur
                            else if (whichProcess == 1) {
                                imageProcess = R.string.processFilterMoyenneur;

                                disableParamBar1();
                                disableParamBar2();
                                disableParamBar3();
                                disableParamImg();
                                disableParamInput();
                                disableParamMatrix();

                            }
                            // Filtre gaussien
                            else if (whichProcess == 2) {
                                imageProcess = R.string.processFilterGaussien;

                                disableParamBar1();
                                disableParamBar2();
                                disableParamBar3();
                                disableParamImg();
                                disableParamInput();
                                disableParamMatrix();

                            }
                            // Filtre détecteur de contours (Kirsch)
                            else if (whichProcess == 3) {
                                imageProcess = R.string.processFilterKirsch;

                                disableParamBar1();
                                disableParamBar2();
                                disableParamBar3();
                                disableParamImg();
                                disableParamInput();
                                disableParamMatrix();

                            }

                            messageBox.setText(String.format(getString(R.string.choice), getString(imageProcess)));
                            ((ViewFlipper) findViewById(R.id.menuFlipper)).showNext();
                        }
                    });
                    processFilterChooser.show();

                }
                // Filtres cosmétiques
                else if (whichCategory == 3) {

                    processCosmeticChooser.setTitle(R.string.processCosmeticChooserTitle);
                    CharSequence processesCosmetic[] = new CharSequence[] {
                            getString(R.string.processCosmeticGrey),
                            getString(R.string.processCosmeticSepia),
                            getString(R.string.processCosmeticNegative),
                            getString(R.string.processCosmeticMirror)
                    };
                    processCosmeticChooser.setItems(processesCosmetic, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int whichProcess) {
                            if (whichProcess == 0) {
                                imageProcess = R.string.processCosmeticGrey;

                                disableParamBar1();
                                disableParamBar2();
                                disableParamBar3();
                                disableParamImg();
                                disableParamInput();
                                disableParamMatrix();
                            } else if (whichProcess == 1) {
                                imageProcess = R.string.processCosmeticSepia;

                                disableParamBar1();
                                disableParamBar2();
                                disableParamBar3();
                                disableParamImg();
                                disableParamInput();
                                disableParamMatrix();
                            } else if (whichProcess == 2) {
                                imageProcess = R.string.processCosmeticNegative;

                                disableParamBar1();
                                disableParamBar2();
                                disableParamBar3();
                                disableParamImg();
                                disableParamInput();
                                disableParamMatrix();
                            } else if (whichProcess == 3) {
                                imageProcess = R.string.processCosmeticMirror;

                                disableParamBar1();
                                disableParamBar2();
                                disableParamBar3();
                                disableParamImg();
                                disableParamInput();
                                disableParamMatrix();
                            }

                            messageBox.setText(String.format(getString(R.string.choice), getString(imageProcess)));
                            ((ViewFlipper) findViewById(R.id.menuFlipper)).showNext();
                        }
                    });
                    processCosmeticChooser.show();

                }

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

        // Bouton pour défaire/refaire le traitement

        findViewById(R.id.btUndo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btUndoClick(v);
            }
        });

        // Bouton pour annuler le traitement en cours

        findViewById(R.id.btCancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btCancelClick(v);
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

    @SuppressLint("SetTextI18n")
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

    @SuppressLint("SetTextI18n")
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
        findViewById(R.id.btApply).setVisibility(View.VISIBLE);
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

    public void btSaveClick() {

        Calendar c = Calendar.getInstance();
        String fileName = "otTER_"+c.get(Calendar.YEAR)+"-"+c.get(Calendar.MONTH)+"-"+c.get(Calendar.DAY_OF_MONTH)
                          +"-"+c.get(Calendar.HOUR_OF_DAY)+"-"+c.get(Calendar.MINUTE)+"-"+c.get(Calendar.SECOND)+".png";

        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + fileName;
        Bitmap bitmap = ((BitmapDrawable) displayBox.getDrawable()).getBitmap();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes);

        File file = new File(filePath);

        FileOutputStream fOut;

        try {
            file.createNewFile();
            fOut = new FileOutputStream(file);
            fOut.write(bytes.toByteArray());

            ContentResolver cr = getContentResolver();
            String imagePath = file.getAbsolutePath();
            String name = file.getName();
            String desc = getString(R.string.saveDesc);
            String savedURL = MediaStore.Images.Media.insertImage(cr, imagePath, name, desc);

            fOut.flush();
            fOut.close();

            messageBox.setText(R.string.saveSuccess);
        }
        catch (IOException e) {
            messageBox.setText(R.string.saveStoError);
        }
    }

    public void btChooseClick(View v) {
        ((ViewFlipper) findViewById(R.id.menuFlipper)).setInAnimation(this, R.anim.slide_in_from_right);
        ((ViewFlipper) findViewById(R.id.menuFlipper)).setOutAnimation(this, R.anim.slide_out_to_left);
        processCategoryChooser.show();
    }

    public void btCancelClick(View v) {
        applyTask.cancel(true);
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

            applyTask = new AsyncApplyTask();
            applyTask.execute();
        }

        findViewById(R.id.btUndo).setVisibility(View.VISIBLE);
        findViewById(R.id.btUndo).setBackgroundResource(R.mipmap.ic_undo);
        cancelled = false;
    }


    /*********************************************************




                        SECTION TRAITEMENTS




     ********************************************************/



    // Fonction principale


    private class AsyncApplyTask extends AsyncTask<Void, Void, Void>
    {
        public boolean running;

        @Override
        protected Void doInBackground(Void... params) {
            running = true;
            // Modification d'histogramme
            if (imageProcess == R.string.processHistogramSeuil) {
                applySeuil();
            }
            else if (imageProcess == R.string.processHistogramSpecification) {
                applySpecification();
            }
            else if (imageProcess == R.string.processHistogramEgalisation) {
                applyEgalisation();
            }
            else if (imageProcess == R.string.processHistogramExpansion) {
                applyExpansion();
            }
            // Morphologie
            else if (imageProcess == R.string.processMorphologyDilatation) {
                applyDilatation();
            }
            else if (imageProcess == R.string.processMorphologyErosion) {
                applyErosion();
            }
            // Filtres
            else if (imageProcess == R.string.processFilterConvolution) {
                applyConvolution();
            }
            else if (imageProcess == R.string.processFilterMoyenneur) {
                applyMoyenneur();
            }
            else if (imageProcess == R.string.processFilterGaussien) {
                applyGaussien();
            }
            else if (imageProcess == R.string.processFilterKirsch) {
                applyKirsch();
            }
            // Cosmétique
            else if (imageProcess == R.string.processCosmeticGrey) {
                applyGrey();
            }
            else if (imageProcess == R.string.processCosmeticSepia) {
                applySepia();
            }
            else if (imageProcess == R.string.processCosmeticNegative) {
                applyNegative();
            }
            else if (imageProcess == R.string.processCosmeticMirror) {
                applyMirror();
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            running = false;
            Bitmap bitmapNew = Bitmap.createBitmap(pixelsTemp, imageWidth, imageHeight, bitmapConfig);
            displayBox.setImageBitmap(bitmapNew);
            ((ViewFlipper) findViewById(R.id.menuFlipper)).showPrevious();
        }

        @Override
        protected void onCancelled(Void result){
            running = false;
            messageBox.setText(R.string.cancelledProcess);
            findViewById(R.id.btUndo).setVisibility(View.INVISIBLE);
            ((ViewFlipper) findViewById(R.id.menuFlipper)).showPrevious();
        }
    }


    // **********  Fonction Seuil  **********


    public void applySeuil(){

        int seuilValueR = paramBarValue1;
        int seuilValueG = paramBarValue2;
        int seuilValueB = paramBarValue3;

        // pixelsTemp est la table de l'image à retourner

        int red, green, blue, RSeuil, GSeuil, BSeuil;

        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {
                if (!applyTask.isCancelled()) {
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
                else return;
            }
        }

    }


    // **********  Fonction Specification  **********


    public void applySpecification(){
        float[][] ddpImg = new float[256][3];
        float[][] ddpRef = new float[256][3];

        int nbPixel = imageWidth * imageHeight;
        int nbPixelRef = referenceWidth * referenceHeight;
        int specR,specG,specB;

        boolean NotFoundR, NotFoundG, NotFoundB;
        int IndiceMinR, IndiceMaxR, IndiceR, IndiceMinG, IndiceMaxG, IndiceG, IndiceMinB, IndiceMaxB, IndiceB;

        for(int x = 0 ; x < imageWidth ; x++)
            for(int y = 0 ; y < imageHeight ; y++)
            {
                ddpImg[Color.red(pixelsOld[x][y])][0]++;
                ddpImg[Color.green(pixelsOld[x][y])][1]++;
                ddpImg[Color.blue(pixelsOld[x][y])][2]++;
            }

        for(int x = 0 ; x < referenceWidth ; x++)
            for(int y = 0 ; y < referenceHeight ; y++)
            {
                ddpRef[Color.red(pixelsReference[x][y])][0]++;
                ddpRef[Color.green(pixelsReference[x][y])][1]++;
                ddpRef[Color.blue(pixelsReference[x][y])][2]++;
            }

        for(int j = 0 ; j < 3 ; j++) {
            ddpImg[0][j] = ddpImg[0][j] / (float)nbPixel;
            ddpRef[0][j] = ddpRef[0][j] / (float)nbPixelRef;
        }

        for(int i = 1 ; i < 256 ; i++)
            for(int j = 0 ; j < 3 ; j++) {
                ddpImg[i][j] = ddpImg[i][j] / (float)nbPixel;
                ddpRef[i][j] = ddpRef[i][j] / (float)nbPixelRef;
                ddpImg[i][j] += ddpImg[i - 1][j];
                ddpRef[i][j] += ddpRef[i - 1][j];
            }

        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {

                specR = (int)(ddpImg[Color.red(pixelsOld[x][y])][0] * 255);
                specG = (int)(ddpImg[Color.green(pixelsOld[x][y])][1] * 255);
                specB = (int)(ddpImg[Color.blue(pixelsOld[x][y])][2] * 255);
                toPixelTempRGB(x,y,specR,specG,specB);
            }
        }

        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {

                IndiceMinR = 0;
                IndiceMaxR = 255;

                while (true) {
                    if (!applyTask.isCancelled()) {
                        IndiceR = IndiceMinR + (IndiceMaxR - IndiceMinR + 1) / 2;
                        if ((((255 * ddpRef[IndiceR][0]) > Color.red(pixelsCurrent[x][y])) && ((255 * ddpRef[IndiceR - 1][0]) <= Color.red(pixelsCurrent[x][y]))) || (IndiceR == 255 || IndiceR == 1)) {
                            specR = IndiceR;
                            break;
                        } else if ((255 * ddpRef[IndiceR][0]) <= Color.red(pixelsCurrent[x][y]))
                            IndiceMinR = IndiceR;
                        else IndiceMaxR = IndiceR;
                    }
                    else return;
                 }

                IndiceMinG = 0;
                IndiceMaxG = 255;

                while (true) {
                    if (!applyTask.isCancelled()) {
                        IndiceG = IndiceMinG + (IndiceMaxG - IndiceMinG + 1) / 2;
                        if ((((255 * ddpRef[IndiceG][1]) > Color.green(pixelsCurrent[x][y])) && ((255 * ddpRef[IndiceG - 1][1]) <= Color.green(pixelsCurrent[x][y]))) || (IndiceG == 255 || IndiceG == 1)) {
                            specG = IndiceG;
                            break;
                        } else if ((255 * ddpRef[IndiceG][1]) <= Color.green(pixelsCurrent[x][y]))
                            IndiceMinG = IndiceG;
                        else IndiceMaxG = IndiceG;
                    }
                    else return;
                }

                IndiceMinB = 0;
                IndiceMaxB = 255;

                while (true){
                    if (!applyTask.isCancelled()) {
                        IndiceB = IndiceMinB + (IndiceMaxB - IndiceMinB + 1) / 2;
                        if ((((255 * ddpRef[IndiceB][2]) > Color.blue(pixelsCurrent[x][y])) && ((255 * ddpRef[IndiceB -1 ][2]) <= Color.blue(pixelsCurrent[x][y]))) || (IndiceB == 255 || IndiceB == 1)) {
                            specB = IndiceB;
                            break;
                        }
                        else if((255 * ddpRef[IndiceB][2]) <= Color.blue(pixelsCurrent[x][y])) IndiceMinB = IndiceB;
                        else IndiceMaxB = IndiceB;
                    }
                    else return;
                }

                toPixelRGB(x, y, specR, specG, specB);

            }
        }

    }


    // **********  Fonction Egalisation  **********


    public void applyEgalisation(){
        float[][] ddp = new float[256][3];

        int nbPixel = imageWidth * imageHeight;
        int egalR, egalG, egalB;

        for (int x = 0 ; x < imageWidth ; x++)
            for (int y = 0 ; y < imageHeight ; y++)
            {
                ddp[Color.red(pixelsOld[x][y])][0]++;
                ddp[Color.green(pixelsOld[x][y])][1]++;
                ddp[Color.blue(pixelsOld[x][y])][2]++;
            }

        for (int j = 0 ; j < 3 ; j++)
            ddp[0][j] = ddp[0][j] / (float)nbPixel;

        for (int i = 1 ; i < 256 ; i++)
            for (int j = 0 ; j < 3 ; j++)
            {
                ddp[i][j] = ddp[i][j] / (float)nbPixel;
                ddp[i][j] += ddp[i-1][j];
            }

        for(int x = 0 ; x < imageWidth ; x++)
            for(int y = 0 ; y < imageHeight ; y++)
            {
                if (!applyTask.isCancelled()) {
                    egalR = (int) (255 * ddp[Color.red(pixelsOld[x][y])][0]);
                    egalG = (int) (255 * ddp[Color.green(pixelsOld[x][y])][1]);
                    egalB = (int) (255 * ddp[Color.blue(pixelsOld[x][y])][2]);
                    toPixelRGB(x, y, egalR, egalG, egalB);
                }
                else return;
            }
    }


    // **********  Fonction Expansion  **********


    public void applyExpansion(){
        float alphaR,alphaG,alphaB,betaR,betaG,betaB;
        int seuil = (int)(imageWidth * imageHeight * paramBarValue1 * 0.01);
        int sum,max_r,min_r,max_g,min_g,max_b,min_b;
        progressBarControl.setMax(imageHeight * imageWidth * 2);

        min_r = 0;
        max_r = 0;
        min_g = 0;
        max_g = 0;
        min_b = 0;
        max_b = 0;

        int[][] hist = new int[256][3];

        for(int x = 0;x < imageWidth;x++)
            for(int y = 0;y < imageHeight;y++)
            {
                hist[Color.red(pixelsOld[x][y])][0]++;
                hist[Color.green(pixelsOld[x][y])][1]++;
                hist[Color.blue(pixelsOld[x][y])][2]++;
            }


        sum = 0;
        for(int i = 0;i < 256;i++)
        {
            sum += hist[i][0];
            if(sum > seuil)
            {
                min_r = i;
                break;
            }
        }

        sum = 0;
        for(int i = 255;i > -1;i--)
        {
            sum += hist[i][0];
            if(sum > seuil)
            {
                max_r = i;
                break;
            }
        }

        sum = 0;
        for(int i = 0;i < 256;i++)
        {
            sum += hist[i][1];
            if(sum > seuil)
            {
                min_g = i;
                break;
            }
        }

        sum = 0;
        for(int i = 255;i > -1;i--)
        {
            sum += hist[i][1];
            if(sum > seuil)
            {
                max_g = i;
                break;
            }
        }

        sum = 0;
        for(int i = 0;i < 256;i++)
        {
            sum += hist[i][2];
            if(sum > seuil)
            {
                min_b = i;
                break;
            }
        }

        sum = 0;
        for(int i = 255;i > -1;i--)
        {
            sum += hist[i][2];
            if(sum > seuil)
            {
                max_b = i;
                break;
            }
        }


        for(int x = 0;x < imageWidth;x++)
            for(int y = 0;y < imageHeight;y++)
            {
                if (!applyTask.isCancelled()) {
                    if (Color.red(pixelsCurrent[x][y]) < min_r)
                        toPixelTempRGB(x, y, min_r, Color.green(pixelsCurrent[x][y]), Color.blue(pixelsCurrent[x][y]));
                    if (Color.red(pixelsCurrent[x][y]) > max_r)
                        toPixelTempRGB(x, y, max_r, Color.green(pixelsCurrent[x][y]), Color.blue(pixelsCurrent[x][y]));

                    if (Color.green(pixelsCurrent[x][y]) < min_g)
                        toPixelTempRGB(x, y, Color.red(pixelsCurrent[x][y]), min_g, Color.blue(pixelsCurrent[x][y]));
                    if (Color.green(pixelsCurrent[x][y]) > max_g)
                        toPixelTempRGB(x, y, Color.red(pixelsCurrent[x][y]), max_g, Color.blue(pixelsCurrent[x][y]));

                    if (Color.blue(pixelsCurrent[x][y]) < min_b)
                        toPixelTempRGB(x, y, Color.red(pixelsCurrent[x][y]), Color.green(pixelsCurrent[x][y]), min_b);
                    if (Color.blue(pixelsCurrent[x][y]) > max_b)
                        toPixelTempRGB(x, y, Color.red(pixelsCurrent[x][y]), Color.green(pixelsCurrent[x][y]), max_b);

                    progressUpdate();
                }
                else return;
            }


        alphaR = (-255f * min_r) / (float)(max_r - min_r);
        alphaG = (-255f * min_g) / (float)(max_g - min_g);
        alphaB = (-255f * min_b) / (float)(max_b - min_b);

        betaR = 255f / (float)(max_r - min_r);
        betaG = 255f / (float)(max_g - min_g);
        betaB = 255f / (float)(max_b - min_b);

        for(int x = 0;x < imageWidth;x++)
            for(int y = 0;y < imageHeight;y++)
                toPixelRGB(x, y, (int)(Color.red(pixelsCurrent[x][y]) * betaR + alphaR), (int)(Color.green(pixelsCurrent[x][y]) * betaG + alphaG), (int)(Color.blue(pixelsCurrent[x][y]) * betaB + alphaB));


    }


    // **********  Fonction Dilatation  **********

    public void applyDilatation(){
        int min,indx,indy,sum;

        for (int x = 0; x < imageWidth; x++)
            for (int y = 0; y < imageHeight; y++)
            {
                if (!applyTask.isCancelled()) {
                    min = 255 * 3;
                    indx = x;
                    indy = y;
                    for (int i = x - 1; i < x + 2; i++)
                        for (int j = y - 1; j < y + 2; j++) {
                            if (i != -1 && i != imageWidth && j != -1 && j != imageHeight) {
                                sum = Color.red(pixelsOld[i][j]) + Color.green(pixelsOld[i][j]) + Color.blue(pixelsOld[i][j]);
                                if (sum < min) {
                                    min = sum;
                                    indx = i;
                                    indy = j;
                                }
                            }
                        }
                    toPixelRGB(x, y, Color.red(pixelsOld[indx][indy]), Color.green(pixelsOld[indx][indy]), Color.blue(pixelsOld[indx][indy]));
                }
                else return;
            }
    }


    // **********  Fonction Erosion  **********

    public void applyErosion(){
        int max,indx,indy,sum;

        for (int x = 0; x < imageWidth; x++)
            for (int y = 0; y < imageHeight; y++)
            {
                if (!applyTask.isCancelled()) {
                    max = 0;
                    indx = x;
                    indy = y;
                    for (int i = x - 1; i < x + 2; i++)
                        for (int j = y - 1; j < y + 2; j++) {
                            if (i != -1 && i != imageWidth && j != -1 && j != imageHeight) {
                                sum = Color.red(pixelsOld[i][j]) + Color.green(pixelsOld[i][j]) + Color.blue(pixelsOld[i][j]);
                                if (sum > max) {
                                    max = sum;
                                    indx = i;
                                    indy = j;
                                }
                            }
                        }
                    toPixelRGB(x, y, Color.red(pixelsOld[indx][indy]), Color.green(pixelsOld[indx][indy]), Color.blue(pixelsOld[indx][indy]));
                }
                else return;
            }
    }


    // **********  Fonction Convolution  **********


    public void applyConvolution() {
        int sum_r,sum_g,sum_b;
        int convR, convG, convB;

        for (int x = 0; x < imageWidth; x++)
            for (int y = 0; y < imageHeight; y++)
            {
                toPixelCopy(x,y,pixelsOld[x][y]);
            }

        for(int x = 2;x < imageWidth - 2;x++)
            for(int y = 2;y < imageHeight - 2;y++)
            {
                if (!applyTask.isCancelled()) {
                    sum_r = 0;
                    sum_g = 0;
                    sum_b = 0;
                    for (int i = x - 2; i < x + 3; i++)
                        for (int j = y - 2; j < y + 3; j++) {
                            sum_r += paramMatrixValue[i - x + 2][j - y + 2] * Color.red(pixelsOld[i][j]);
                            sum_g += paramMatrixValue[i - x + 2][j - y + 2] * Color.green(pixelsOld[i][j]);
                            sum_b += paramMatrixValue[i - x + 2][j - y + 2] * Color.blue(pixelsOld[i][j]);
                        }

                    convR = (int) (sum_r / (float) paramMatrixNorme);
                    convR = max_2(convR, 0);
                    convR = min_2(convR, 255);

                    convG = (int) (sum_g / (float) paramMatrixNorme);
                    convG = max_2(convG, 0);
                    convG = min_2(convG, 255);

                    convB = (int) (sum_b / (float) paramMatrixNorme);
                    convB = max_2(convB, 0);
                    convB = min_2(convB, 255);

                    toPixelRGB(x, y, convR, convG, convB);
                }
                else return;
            }
    }



    // **********  Fonction Filtre moyenneur  **********


    public void applyMoyenneur() {
        setParamMatrixValue(1, 1, 1, 1, 1,
                            1, 1, 1, 1, 1,
                            1, 1, 1, 1, 1,
                            1, 1, 1, 1, 1,
                            1, 1, 1, 1, 1);
        paramMatrixNorme = 25;

        int sum_r, sum_g, sum_b;

        for (int x = 0; x < imageWidth; x++)
            for (int y = 0; y < imageHeight; y++)
            {
                toPixelCopy(x,y,pixelsOld[x][y]);
            }

        for(int x = 2;x < imageWidth - 2;x++)
            for(int y = 2;y < imageHeight - 2;y++)
            {
                if (!applyTask.isCancelled()) {
                    sum_r = 0;
                    sum_g = 0;
                    sum_b = 0;

                    for (int i = x - 2; i < x + 3; i++)
                        for (int j = y - 2; j < y + 3; j++) {
                            sum_r += paramMatrixValue[i - x + 2][j - y + 2] * Color.red(pixelsOld[i][j]);
                            sum_g += paramMatrixValue[i - x + 2][j - y + 2] * Color.green(pixelsOld[i][j]);
                            sum_b += paramMatrixValue[i - x + 2][j - y + 2] * Color.blue(pixelsOld[i][j]);
                        }

                    toPixelRGB(x, y, sum_r / paramMatrixNorme, sum_g / paramMatrixNorme, sum_b / paramMatrixNorme);
                }
                else return;
            }
    }


    // **********  Fonction Filtre gaussien  **********


    public void applyGaussien() {
        setParamMatrixValue(1, 4,  6,  4,  1,
                            4, 18, 30, 18, 4,
                            6, 30, 48, 30, 6,
                            4, 18, 30, 18, 4,
                            1, 4,  6,  4,  1);
        paramMatrixNorme = 300;

        int sum_r, sum_g, sum_b;

        for (int x = 0; x < imageWidth; x++)
            for (int y = 0; y < imageHeight; y++)
            {
                toPixelCopy(x, y, pixelsOld[x][y]);
            }

        for(int x = 2;x < imageWidth - 2;x++)
            for(int y = 2;y < imageHeight - 2;y++)
            {
                if (!applyTask.isCancelled()) {
                    sum_r = 0;
                    sum_g = 0;
                    sum_b = 0;

                    for (int i = x - 2; i < x + 3; i++)
                        for (int j = y - 2; j < y + 3; j++) {
                            sum_r += paramMatrixValue[i - x + 2][j - y + 2] * Color.red(pixelsOld[i][j]);
                            sum_g += paramMatrixValue[i - x + 2][j - y + 2] * Color.green(pixelsOld[i][j]);
                            sum_b += paramMatrixValue[i - x + 2][j - y + 2] * Color.blue(pixelsOld[i][j]);
                        }

                    toPixelRGB(x, y, sum_r / paramMatrixNorme, sum_g / paramMatrixNorme, sum_b / paramMatrixNorme);
                }
                else return;
            }
    }



    // **********  Fonction Detection de contours (Kirsch)  **********


    public void applyKirsch() {
        int max,indx,indy,sum_r,sum_g,sum_b;

        for (int x = 0; x < imageWidth; x++)
            for (int y = 0; y < imageHeight; y++)
            {
                toPixelCopy(x,y,pixelsOld[x][y]);
            }

        setParamMatrixValue(2, 4,  5,  4,  2,
                            4, 9,  12, 9,  4,
                            5, 12, 15, 12, 5,
                            4, 9,  12, 9,  4,
                            2, 4,  5,  4,  2);
        paramMatrixNorme = 159;

        int[][][] grad_temp = new int[imageWidth][imageHeight][3];

        progressBarControl.setMax(imageHeight*imageWidth*2);

        for(int x = 2;x < imageWidth - 2;x++)
            for(int y = 2;y < imageHeight - 2;y++)
            {
                if (!applyTask.isCancelled()) {
                    sum_r = 0;
                    sum_g = 0;
                    sum_b = 0;
                    for (int i = x - 2; i < x + 3; i++)
                        for (int j = y - 2; j < y + 3; j++) {
                            sum_r += paramMatrixValue[i - x + 2][j - y + 2] * Color.red(pixelsOld[i][j]);
                            sum_g += paramMatrixValue[i - x + 2][j - y + 2] * Color.green(pixelsOld[i][j]);
                            sum_b += paramMatrixValue[i - x + 2][j - y + 2] * Color.blue(pixelsOld[i][j]);
                        }
                    toPixelRGB(x, y, sum_r / paramMatrixNorme, sum_g / paramMatrixNorme, sum_b / paramMatrixNorme);
                }
                else return;
            }

        for(int x = 1;x < imageWidth - 1;x++)
            for(int y = 1;y < imageHeight - 1;y++) {
                if (!applyTask.isCancelled()) {
                    for (int k = 0; k < 3; k++)
                        grad_temp[x][y][k] = kirsch(x, y, k);
                    progressUpdate();
                }
                else return;
            }

        for(int x = 1;x < imageWidth - 1;x++)
            for(int y = 1;y < imageHeight - 1;y++)
                toPixelTempRGB(x,y,grad_temp[x][y][0],grad_temp[x][y][1],grad_temp[x][y][2]);
    }


    int kirsch(int i,int j,int color)
    {
        int k = 5 * (getPixelsCurrentColor(i - 1, j - 1, color) + getPixelsCurrentColor(i, j - 1, color) + getPixelsCurrentColor(i + 1, j - 1, color));
        k += -3 * (getPixelsCurrentColor(i - 1, j, color) + getPixelsCurrentColor(i + 1, j, color) + getPixelsCurrentColor(i - 1, j + 1, color) + getPixelsCurrentColor(i, j + 1, color) + getPixelsCurrentColor(i + 1, j + 1, color));

        int g1,g2,g3,g4,g5,g6,g7,g8;

        g1 = k;

        g2 = k + -8 * getPixelsCurrentColor(i + 1, j - 1, color) + 8 * getPixelsCurrentColor(i - 1, j, color);

        g3 = k + -8 * getPixelsCurrentColor(i + 1, j - 1, color) + 8 * getPixelsCurrentColor(i - 1, j, color) + -8 * getPixelsCurrentColor(i, j - 1, color) + 8 * getPixelsCurrentColor(i - 1, j + 1, color);

        g4 = k +  -8*(getPixelsCurrentColor(i + 1, j - 1, color) + getPixelsCurrentColor(i, j - 1, color) + getPixelsCurrentColor(i - 1, j - 1, color)) + 8*(getPixelsCurrentColor(i - 1, j, color) + getPixelsCurrentColor(i - 1, j + 1, color) + getPixelsCurrentColor(i, j + 1, color));

        g5 = k +  -8*(getPixelsCurrentColor(i + 1, j - 1, color) + getPixelsCurrentColor(i, j - 1, color) + getPixelsCurrentColor(i - 1, j - 1, color)) + 8*(getPixelsCurrentColor(i - 1, j + 1, color) + getPixelsCurrentColor(i, j + 1, color)+ getPixelsCurrentColor(i + 1, j + 1, color));

        g6 = k +  -8*(getPixelsCurrentColor(i + 1, j - 1, color) + getPixelsCurrentColor(i, j - 1, color) + getPixelsCurrentColor(i - 1, j - 1, color)) + 8*(getPixelsCurrentColor(i + 1, j, color) + getPixelsCurrentColor(i, j + 1, color)+ getPixelsCurrentColor(i + 1, j + 1, color));

        g7 = k +  -8*(getPixelsCurrentColor(i, j - 1, color) + getPixelsCurrentColor(i - 1, j - 1, color)) + 8*(getPixelsCurrentColor(i + 1, j, color) + getPixelsCurrentColor(i + 1, j + 1, color));

        g8 = k +  -8*(getPixelsCurrentColor(i - 1, j - 1, color)) + 8*(getPixelsCurrentColor(i + 1, j, color) );

        return max_8(g1,g2,g3,g4,g5,g6,g7,g8);
    }

    int getPixelsCurrentColor(int i, int j, int color)
    {
        switch(color)
        {
            case 0 :
                return Color.red(pixelsCurrent[i][j]);
            case 1 :
                return Color.green(pixelsCurrent[i][j]);
            case 2 :
                return Color.blue(pixelsCurrent[i][j]);
            default :
                return 0;
        }
    }



    // **********  Fonction Niveaux de gris  **********


    public void applyGrey() {
        int val;

        for (int x = 0; x < imageWidth; x++)
            for (int y = 0; y < imageHeight; y++)
            {
                if (!applyTask.isCancelled()) {
                    val = (int) ((float) Color.red(pixelsOld[x][y]) * 0.2989 + (float) Color.green(pixelsOld[x][y]) * 0.587 + (float) Color.blue(pixelsOld[x][y]) * 0.114);
                    toPixelRGB(x, y, val, val, val);
                }
                else return;
            }
    }


    // **********  Fonction Sepia  **********


    public void applySepia() {
        int red,green,blue;

        for (int x = 0; x < imageWidth; x++)
            for (int y = 0; y < imageHeight; y++)
            {
                if (!applyTask.isCancelled()) {
                    red = (int) ((float) Color.red(pixelsOld[x][y]) * 0.393 + (float) Color.green(pixelsOld[x][y]) * 0.769 + (float) Color.blue(pixelsOld[x][y]) * 0.189);
                    green = (int) ((float) Color.red(pixelsOld[x][y]) * 0.349 + (float) Color.green(pixelsOld[x][y]) * 0.686 + (float) Color.blue(pixelsOld[x][y]) * 0.168);
                    blue = (int) ((float) Color.red(pixelsOld[x][y]) * 0.272 + (float) Color.green(pixelsOld[x][y]) * 0.534 + (float) Color.blue(pixelsOld[x][y]) * 0.131);

                    toPixelRGB(x, y, (red > 255 ? 255 : red), (green > 255 ? 255 : green), (blue > 255 ? 255 : blue));
                }
                else return;
            }
    }


    // **********  Fonction Negatif  **********


    public void applyNegative() {
        for (int x = 0; x < imageWidth; x++)
            for (int y = 0; y < imageHeight; y++)
                if (!applyTask.isCancelled()) {
                    toPixelRGB(x, y, 255 - Color.red(pixelsOld[x][y]), 255 - Color.green(pixelsOld[x][y]), 255 - Color.blue(pixelsOld[x][y]));
                }
                else return;
    }


    // **********  Fonction Mirroir  **********


    public void applyMirror() {
        for (int x = 0; x < imageWidth; x++)
            for (int y = 0; y < imageHeight; y++)
                if (!applyTask.isCancelled()) {
                    toPixelRGB(x, y, Color.red(pixelsOld[imageWidth - 1 - x][y]), Color.green(pixelsOld[imageWidth - 1 - x][y]), Color.blue(pixelsOld[imageWidth - 1 - x][y]));
                }
                else return;
    }

    /**************************************************************





                    FIN DE LA SECTION TRAITEMENTS





     *************************************************************/


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

    public void toPixelTempRGB(int x, int y, int R, int G, int B){
        pixelsTemp[imageWidth * y + x] = Color.rgb(R,G,B);
        pixelsCurrent[x][y] = pixelsTemp[imageWidth * y + x];
        // NO PROGRESS UPDATE !
    }

    public void progressUpdate(){
        progressCount += 1;
        if ((progressCount%10000 == 0) | (progressCount == progressBarControl.getMax())) {
            progressBarControl.setProgress(progressCount);
        }
    }

    int max_2(int a,int b)
    {
        if (a > b)
            return a;
        else
            return b;
    }

    int min_2(int a,int b)
    {
        if (a < b)
            return a;
        else
            return b;
    }

    int max_8(int a,int b,int c,int d,int e,int f,int g,int h)
    {
        return max_2(max_2(max_2(a,b),max_2(c,d)),max_2(max_2(e,f),max_2(g,h)));
    }

    public void setParamMatrixValue(int m00, int m10, int m20, int m30, int m40,
                                    int m01, int m11, int m21, int m31, int m41,
                                    int m02, int m12, int m22, int m32, int m42,
                                    int m03, int m13, int m23, int m33, int m43,
                                    int m04, int m14, int m24, int m34, int m44)
    {
        paramMatrixValue = new int[5][5];
        paramMatrixValue[0][0] = m00; paramMatrixValue[1][0] = m10; paramMatrixValue[2][0] = m20; paramMatrixValue[3][0] = m30; paramMatrixValue[4][0] = m40;
        paramMatrixValue[0][1] = m01; paramMatrixValue[1][1] = m11; paramMatrixValue[2][1] = m21; paramMatrixValue[3][1] = m31; paramMatrixValue[4][1] = m41;
        paramMatrixValue[0][2] = m02; paramMatrixValue[1][2] = m12; paramMatrixValue[2][2] = m22; paramMatrixValue[3][2] = m32; paramMatrixValue[4][2] = m42;
        paramMatrixValue[0][3] = m03; paramMatrixValue[1][3] = m13; paramMatrixValue[2][3] = m23; paramMatrixValue[3][3] = m33; paramMatrixValue[4][3] = m43;
        paramMatrixValue[0][4] = m04; paramMatrixValue[1][4] = m14; paramMatrixValue[2][4] = m24; paramMatrixValue[3][4] = m34; paramMatrixValue[4][4] = m44;
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

                        if (m_int < 100) {
                            paramMatrixTemp[i][j] = m_int;

                        } else {
                            messageBox.setText(String.format(getString(R.string.valueMatrixBoundsError), i + 1, j + 1));
                            isMatrixValid = false;
                            break;
                        }
                    }
                }

                if (isMatrixValid) {

                    paramMatrixNorme = 0;
                    for (int i = 0; i<5; i++)
                        for (int j=0; j<5; j++)
                            paramMatrixNorme += paramMatrixTemp[i][j];

                    if (paramMatrixNorme != 0) {
                        for (int i = 0; i < 5; i++) {
                            System.arraycopy(paramMatrixTemp[i], 0, paramMatrixValue[i], 0, 5);
                        }
                        messageBox.setText(R.string.valueMatrixSuccess);
                    } else {
                        messageBox.setText(R.string.valueMatrixNormError);
                    }
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
            messageBox.setText(R.string.loading);
            displayBox.setVisibility(View.GONE);
            (findViewById(R.id.imageLoading)).setVisibility(View.VISIBLE);
            switch (requestCode) {
                case SELECT_PICTURE:
                    new AsyncImageLoadTask().execute(new loadParams(this, data));
                    break;
                case SELECT_REFERENCE_PICTURE:
                    new AsyncReferenceLoadTask().execute(new loadParams(this, data));
                    break;
                case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
                    new AsyncCamLoadTask().execute(new loadParams(this, data));
                    break;
            }
        }
        else if (resultCode == RESULT_CANCELED) {
            switch (requestCode) {
                case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
                    messageBox.setText(R.string.camCancelled);
                default:
                    messageBox.setText(R.string.loadCancelled);
            }
        } else {
                switch (requestCode) {
                    case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
                        messageBox.setText(R.string.camError);
                    default:
                        messageBox.setText(R.string.loadImgError);
                }
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

    public class loadParams {
        Context context;
        Intent data;

        loadParams(Context context, Intent data) {
            this.context = context;
            this.data = data;
        }
    }

    private class AsyncImageLoadTask extends AsyncTask<loadParams, Void, Void>
    {
        Bitmap bitmap;
        int tempWidth;
        int tempHeight;

        @Override
        protected Void doInBackground(loadParams... params) {
            try {
                bitmap = Picasso.with(params[0].context).load(params[0].data.getData()).get();
            } catch (IOException e) {
                return null;
            }

            tempWidth = imageWidth;
            tempHeight = imageHeight;

            imageWidth = bitmap.getWidth();
            imageHeight = bitmap.getHeight();

            if (imageWidth <= 4096 && imageHeight <= 4096) {

                setProgressLoadMax(imageHeight * imageWidth);
                progressCount = 0;

                pixelsCurrent = new int[imageWidth][imageHeight];
                pixelsOld = new int[imageWidth][imageHeight];
                pixelsTemp = new int[imageWidth * imageHeight];

                for (int x = 0; x < imageWidth; x++) {
                    for (int y = 0; y < imageHeight; y++) {
                        pixelsCurrent[x][y] = bitmap.getPixel(x, y);
                        progressLoadUpdate();
                    }
                }
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            if (bitmap != null) {
                if (imageWidth <= 4096 && imageHeight <= 4096) {
                    displayBox.setImageBitmap(bitmap);
                    messageBox.setText(R.string.loadSuccess);
                    findViewById(R.id.btUndo).setBackgroundResource(R.mipmap.ic_undo);
                    findViewById(R.id.btUndo).setVisibility(View.INVISIBLE);
                }
                else {
                    imageWidth = tempWidth;
                    imageHeight = tempHeight;
                    messageBox.setText(R.string.loadSizeError);
                }
            }
            else
                messageBox.setText(R.string.loadImgError);
            displayBox.setVisibility(View.VISIBLE);
            (findViewById(R.id.imageLoading)).setVisibility(View.GONE);
        }
    }

    private class AsyncReferenceLoadTask extends AsyncTask<loadParams, Void, Void>
    {
        Bitmap bitmap;
        String[] pathSplit;

        @Override
        protected Void doInBackground(loadParams... params) {
            try {
                bitmap = Picasso.with(params[0].context).load(params[0].data.getData()).get();
            } catch (IOException e) {
                return null;
            }

            referenceWidth = bitmap.getWidth();
            referenceHeight = bitmap.getHeight();

            setProgressLoadMax(referenceHeight * referenceWidth);
            progressCount = 0;

            pixelsReference = new int[referenceWidth][referenceHeight];

            for (int x = 0; x < referenceWidth; x++) {
                for (int y = 0; y < referenceHeight; y++) {
                    pixelsReference[x][y] = bitmap.getPixel(x, y);
                    progressLoadUpdate();
                }
            }

            pathSplit = params[0].data.getData().getPath().split("/");

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            if (bitmap != null) {
                messageBox.setText(R.string.loadSuccess);
                paramImgControlText.setText(pathSplit[pathSplit.length - 1]);
                findViewById(R.id.btApply).setVisibility(View.VISIBLE);
            }
            else {
                messageBox.setText(R.string.loadImgError);
            }
            displayBox.setVisibility(View.VISIBLE);
            (findViewById(R.id.imageLoading)).setVisibility(View.GONE);
        }
    }

    private class AsyncCamLoadTask extends AsyncTask<loadParams, Void, Void>
    {
        Bitmap bitmap;
        int tempWidth;
        int tempHeight;

        @Override
        protected Void doInBackground(loadParams... params) {
            try {
                bitmap = Picasso.with(params[0].context).load(params[0].data.getData()).get();
            } catch (IOException e) {
                return null;
            }

            tempWidth = imageWidth;
            tempHeight = imageHeight;

            imageWidth = bitmap.getWidth();
            imageHeight = bitmap.getHeight();

            if (imageWidth <= 4096 && imageHeight <= 4096) {

                setProgressLoadMax(imageHeight * imageWidth);
                progressCount = 0;

                pixelsCurrent = new int[imageWidth][imageHeight];
                pixelsOld = new int[imageWidth][imageHeight];
                pixelsTemp = new int[imageWidth * imageHeight];

                for (int x = 0; x < imageWidth; x++) {
                    for (int y = 0; y < imageHeight; y++) {
                        pixelsCurrent[x][y] = bitmap.getPixel(x, y);
                        progressLoadUpdate();
                    }
                }
            }

            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            if (bitmap != null) {
                if (imageWidth <= 4096 && imageHeight <= 4096) {
                    displayBox.setImageBitmap(bitmap);
                    messageBox.setText(R.string.loadSuccess);
                    findViewById(R.id.btUndo).setBackgroundResource(R.mipmap.ic_undo);
                    findViewById(R.id.btUndo).setVisibility(View.INVISIBLE);
                }
                else {
                    imageWidth = tempWidth;
                    imageHeight = tempHeight;
                    messageBox.setText(R.string.loadSizeError);
                }
            }
            else
                messageBox.setText(R.string.loadImgError);
            displayBox.setVisibility(View.VISIBLE);
            (findViewById(R.id.imageLoading)).setVisibility(View.GONE);
        }
    }

    public void setProgressLoadMax(int max){
        progressLoadBarControl.setMax(max);
    }

    public void progressLoadUpdate() {
        progressCount += 1;
        if ((progressCount % 25000 == 0) | (progressCount == progressLoadBarControl.getMax())) {
            progressLoadBarControl.setProgress(progressCount);
        }
    }

    @Override
    public void onBackPressed(){
        new AlertDialog.Builder(this)
                .setIcon(R.mipmap.ic_exitconfirm)
                .setTitle(R.string.exitTitle)
                .setMessage(R.string.exitMessage)
                .setPositiveButton(R.string.exitYes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (applyTask != null && applyTask.running)
                            applyTask.cancel(true);
                        finish();
                    }
                })
                .setNegativeButton(R.string.exitNo, null)
                .show();
    }
}
