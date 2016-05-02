package com.vtm.solanya.imgtrt;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
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
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class Main extends Activity {

    final static int SELECT_PICTURE = 1;
    final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    private static Context appContext;
    private static Activity appActivity;

    public static Context getContext(){
        return appContext;
    }

    public static Activity getActivity(){
        return appActivity;
    }

    public TextView messageBox = null;
    public TextView progressBox = null;
    public ImageView displayBox = null;

    public Image currentImage = null;
    public Traitement currentProcess = null;

    public ProgressBar progressBarControl = null;

    public TableRow paramBarControlRow1 = null;
    public TableRow paramBarControlRow2 = null;
    public TableRow paramBarControlRow3 = null;

    public SeekBar paramBarControl1 = null;
    public SeekBar paramBarControl2 = null;
    public SeekBar paramBarControl3 = null;

    public TextView paramBarControlText1 = null;
    public TextView paramBarControlText2 = null;
    public TextView paramBarControlText3 = null;

    public int xDelta;

    public CharSequence imageProcess = "";
    public CharSequence processes[] = new CharSequence[] {"Seuil", "Flou" , "Dilatation", "Erosion"};
    public AlertDialog.Builder processChooser;


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

        appContext = this;
        appActivity = this;

        messageBox = (TextView) findViewById(R.id.textBox);
        messageBox.setTextColor(Color.BLACK);
        messageBox.setText("Bienvenue !");

        progressBox = (TextView) findViewById(R.id.progressText);
        progressBox.setTextColor(Color.BLACK);

        displayBox = (ImageView) findViewById(R.id.imageDisplay);

        progressBarControl = (ProgressBar) findViewById(R.id.progressBar);

        paramBarControlRow1 = (TableRow) findViewById(R.id.paramBarRow1);
        paramBarControlRow2 = (TableRow) findViewById(R.id.paramBarRow2);
        paramBarControlRow3 = (TableRow) findViewById(R.id.paramBarRow3);
        paramBarControlRow1.setVisibility(View.GONE);
        paramBarControlRow2.setVisibility(View.GONE);
        paramBarControlRow3.setVisibility(View.GONE);

        paramBarControl1 = (SeekBar) findViewById(R.id.paramBar1);
        paramBarControl2 = (SeekBar) findViewById(R.id.paramBar2);
        paramBarControl3 = (SeekBar) findViewById(R.id.paramBar3);

        paramBarControlText1 = (TextView) findViewById(R.id.paramBarText1);
        paramBarControlText2 = (TextView) findViewById(R.id.paramBarText2);
        paramBarControlText3 = (TextView) findViewById(R.id.paramBarText3);

        processChooser = new AlertDialog.Builder(this);
        processChooser.setTitle("Traitements");
        processChooser.setItems(processes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {

                    imageProcess = "Seuil";
                    currentProcess = new Seuil(appContext, appActivity, currentImage);
                    currentProcess.paramBarValue1 = 0;
                    currentProcess.paramBarValue2 = 0;
                    currentProcess.paramBarValue3 = 0;

                    paramBarControlRow1.setVisibility(View.VISIBLE);
                    paramBarControl1.setProgress(currentProcess.paramBarValue1);
                    paramBarControl1.setMax(255);
                    paramBarControl1.setBackgroundColor(Color.parseColor("#40ff0000"));
                    paramBarControlText1.setTextColor(Color.RED);
                    paramBarControlText1.setText(Integer.toString(currentProcess.paramBarValue1));

                    paramBarControlRow2.setVisibility(View.VISIBLE);
                    paramBarControl2.setProgress(currentProcess.paramBarValue2);
                    paramBarControl2.setMax(255);
                    paramBarControl2.setBackgroundColor(Color.parseColor("#4000ff00"));
                    paramBarControlText2.setTextColor(Color.GREEN);
                    paramBarControlText2.setText(Integer.toString(currentProcess.paramBarValue2));

                    paramBarControlRow3.setVisibility(View.VISIBLE);
                    paramBarControl3.setProgress(currentProcess.paramBarValue3);
                    paramBarControl3.setMax(255);
                    paramBarControl3.setBackgroundColor(Color.parseColor("#400000ff"));
                    paramBarControlText3.setTextColor(Color.BLUE);
                    paramBarControlText3.setText(Integer.toString(currentProcess.paramBarValue3));

                } else if (which == 1) {

                    imageProcess = "Flou";
                    currentProcess = new Flou(appContext, appActivity, currentImage);
                    currentProcess.paramBarValue1 = 0;

                    paramBarControlRow1.setVisibility(View.VISIBLE);
                    paramBarControl1.setProgress(currentProcess.paramBarValue1);
                    paramBarControl1.setMax(5);
                    paramBarControl1.setBackgroundColor(Color.parseColor("#40000000"));
                    paramBarControlText1.setTextColor(Color.BLACK);
                    paramBarControlText1.setText(Integer.toString(currentProcess.paramBarValue1));

                    paramBarControlRow2.setVisibility(View.GONE);

                    paramBarControlRow3.setVisibility(View.GONE);

                } else if (which == 2) {

                    imageProcess = "Dilatation";

                    paramBarControlRow1.setVisibility(View.GONE);

                    paramBarControlRow2.setVisibility(View.GONE);

                    paramBarControlRow3.setVisibility(View.GONE);

                } else if (which == 3) {

                    imageProcess = "Erosion";

                    paramBarControlRow1.setVisibility(View.GONE);

                    paramBarControlRow2.setVisibility(View.GONE);

                    paramBarControlRow3.setVisibility(View.GONE);

                }

                ((Button) findViewById(R.id.btApply)).setVisibility(View.VISIBLE);
                messageBox.setText("Choix : " + imageProcess);
                ((ViewFlipper) findViewById(R.id.menuFlipper)).showNext();
            }
        });

        ((ViewFlipper) findViewById(R.id.menuFlipper)).showNext();
        ((ViewFlipper) findViewById(R.id.menuFlipper)).showPrevious();
        ((ViewFlipper) findViewById(R.id.menuFlipper)).setInAnimation(this, R.anim.slide_in_from_right);
        ((ViewFlipper) findViewById(R.id.menuFlipper)).setOutAnimation(this, R.anim.slide_out_to_left);
        ((ViewFlipper) findViewById(R.id.menuFlipper)).setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return menuFlip(v, event);
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

        // Réglage direct des paramètres

        ((TextView) findViewById(R.id.paramBarText1)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeParamBar1(v);
            }
        });

        // Sliders de contrôle de seuil


        paramBarControl1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentProcess.paramBarValue1 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                paramBarControlText1.setText(Integer.toString(currentProcess.paramBarValue1));
            }
        });

        paramBarControl2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentProcess.paramBarValue2 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                paramBarControlText2.setText(Integer.toString(currentProcess.paramBarValue2));
            }
        });

        paramBarControl3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                currentProcess.paramBarValue3 = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                paramBarControlText3.setText(Integer.toString(currentProcess.paramBarValue3));
            }
        });
    }

    public boolean menuFlip(View v, MotionEvent event) {
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
                    if (imageProcess == ""){
                        messageBox.setText("Pas de traitement sélectionné.");
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

    /**
     * Evenement du click bouton
     *
     * @param v
     */
    public void btLoadClick(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, ""), SELECT_PICTURE);
    }

    public void btCamClick(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
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

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SELECT_PICTURE:
                    currentImage = new Image(appContext, appActivity);
                    currentImage.loadImage(data);
                    break;
                case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
                    currentImage = new Image(appContext, appActivity);
                    currentImage.loadCamImage(data);
                    break;
            }
        }
        else if (resultCode == RESULT_CANCELED) {
            messageBox.setText("Cancelled.");
        } else {
            messageBox.setText("Error taking the picture.");
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

    public boolean btSaveClick(View v) {
        return currentImage.saveImage();
    }

    public void btChooseClick(View v) {
        ((ViewFlipper) findViewById(R.id.menuFlipper)).setInAnimation(this, R.anim.slide_in_from_right);
        ((ViewFlipper) findViewById(R.id.menuFlipper)).setOutAnimation(this, R.anim.slide_out_to_left);
        processChooser.show();
    }



    public void btApplyClick(View v) {
        if (imageProcess == ""){
            messageBox.setText("Aucun traitement sélectionné.");
            return;
        }
        else{
            progressBox.setText(imageProcess + " en cours...");
            currentProcess.initializeProcess();
        }
    }

    public void btCancelClick(View v) {
        currentImage.cancelProcess();
    }

    public void changeParamBar1(View v){

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Change value");

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        final TextView error = new TextView(this);
        error.setText("Value must be between 0 and " + Integer.toString(paramBarControl1.getMax()));
        error.setGravity(Gravity.CENTER);
        error.setVisibility(View.VISIBLE);
        error.setTextColor(Color.WHITE);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);

        layout.addView(error);
        layout.addView(input);
        builder.setView(layout);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_Text = input.getText().toString();
                int m_int = Integer.parseInt(m_Text);

                if (m_int <= paramBarControl1.getMax()) {
                    paramBarControlText1.setText(m_Text);
                    currentProcess.paramBarValue1 = m_int;
                    paramBarControl1.setProgress(m_int);
                    messageBox.setText("Value changed.");
                } else {
                    messageBox.setText("Error : Value must be between 0 and " + Integer.toString(paramBarControl1.getMax()));
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }





}
