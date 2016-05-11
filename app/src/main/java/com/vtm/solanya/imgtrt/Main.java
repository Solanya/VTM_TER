package com.vtm.solanya.imgtrt;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
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

/**
 * Classe de l'activité principale de l'application otTER, contient toute l'interface utilisateur / gestion des algorithmes.
 */
public class Main extends Activity {

    /**
     * Constantes pour définir le contexte de l'appel à onActivityResult
     */
    final static int SELECT_PICTURE = 1;
    final static int SELECT_REFERENCE_PICTURE = 10;
    final static int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;


    /**
     * Eléments de l'interface utilisateur
     */
    public TextView messageBox = null;
    public TextView progressBox = null;
    public ImageView displayBox = null;

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

    public int xDelta;


    /**
     * Eléments liés à l'image à traiter
     */
    public int imageWidth;
    public int imageHeight;
    public Bitmap.Config bitmapConfig;

    public boolean cancelled = false;
    public int progressCount = 0;
    public AsyncApplyTask applyTask;

    public int[][] pixelsCurrent;
    public int[][] pixelsOld;
    public int[] pixelsTemp;


    /**
     * Constantes liées aux paramètres réglables par l'utilisateur
     */
    public int paramBarValue1 = 0;
    public int paramBarValue2 = 0;
    public int paramBarValue3 = 0;

    public int referenceWidth;
    public int referenceHeight;
    public int[][] pixelsReference;

    public String paramInputValue;

    public int[][] paramMatrixValue;
    public int[][] paramMatrixTemp;
    public int paramMatrixNorme;
    public boolean isMatrixValid;


    /**
     * Cible pour la sauvegarde
     */
    public Target target;


    /**
     * Dialogues pour la sélection de l'algorithme à utiliser
     */
    public int imageProcess = R.string.emptyProcess;
    public AlertDialog.Builder processCategoryChooser;
    public AlertDialog.Builder processHistogramChooser;
    public AlertDialog.Builder processMorphologyChooser;
    public AlertDialog.Builder processFilterChooser;
    public AlertDialog.Builder processCosmeticChooser;


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
     * Fonction appelée au lancement de l'application
     * @param savedInstanceState Permet de sauvegarder des données à la destruction de l'activité (inutilisé dans notre cas)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Initialisation des éléments de l'interface
         */
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

        /**
         * Initialisation des dialogues permettant le choix du traitement
         * Commence par le choix de la catégorie.
         */
        processCategoryChooser.setTitle(R.string.processCategoryChooserTitle);
        CharSequence processCategories[] = new CharSequence[] {
                getString(R.string.processCategoryHistogram),
                getString(R.string.processCategoryMorphology),
                getString(R.string.processCategoryFilter),
                getString(R.string.processCategoryCosmetic)
        };
        processCategoryChooser.setItems(processCategories, new DialogInterface.OnClickListener() {
            /**
             * Action effectuée après le choix de la catégorie.
             * @param dialog Dialogue de choix de la catégorie
             * @param whichCategory Indice de la catégorie choisie (en commençant par 0)
             */
            @Override
            public void onClick(DialogInterface dialog, int whichCategory) {

                /**
                 * Initialisation des dialogues par catégorie
                 */
                if (whichCategory == 0) {   // Modification d'histogramme

                    processHistogramChooser.setTitle(R.string.processHistogramChooserTitle);
                    CharSequence processesHistogram[] = new CharSequence[]{
                            getString(R.string.processHistogramSeuil),
                            getString(R.string.processHistogramSpecification),
                            getString(R.string.processHistogramEgalisation),
                            getString(R.string.processHistogramExpansion)
                    };
                    processHistogramChooser.setItems(processesHistogram, new DialogInterface.OnClickListener() {
                        /**
                         * Action effectuée après le choix du traitement
                         * @param dialog Dialogue de choix du traitement
                         * @param whichProcess Indice du traitement choisi (en commençant par 0)
                         */
                        @Override
                        public void onClick(DialogInterface dialog, int whichProcess) {

                            if (whichProcess == 0) {    // Seuil

                                imageProcess = R.string.processHistogramSeuil;

                                enableParamBar1(255, "#ff0000");
                                enableParamBar2(255, "#00ff00");
                                enableParamBar3(255, "#6666ff");

                                disableParamImg();
                                disableParamInput();
                                disableParamMatrix();
                            }

                            else if (whichProcess == 1) {   // Spécification

                                imageProcess = R.string.processHistogramSpecification;

                                disableParamBar1();
                                disableParamBar2();
                                disableParamBar3();

                                enableParamImg();

                                disableParamInput();
                                disableParamMatrix();
                            }

                            else if (whichProcess == 2) {   // Egalisation

                                imageProcess = R.string.processHistogramEgalisation;

                                disableParamBar1();
                                disableParamBar2();
                                disableParamBar3();
                                disableParamImg();
                                disableParamInput();
                                disableParamMatrix();
                            }

                            else if (whichProcess == 3) {   // Expansion

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

                else if (whichCategory == 1) {  // Morphologie

                    processMorphologyChooser.setTitle(R.string.processMorphologyChooserTitle);
                    CharSequence processesMorphology[] = new CharSequence[]{
                            getString(R.string.processMorphologyDilatation),
                            getString(R.string.processMorphologyErosion)
                    };
                    processMorphologyChooser.setItems(processesMorphology, new DialogInterface.OnClickListener() {
                        /**
                         * Action effectuée après le choix du traitement
                         * @param dialog Dialogue de choix du traitement
                         * @param whichProcess Indice du traitement choisi (en commençant par 0)
                         */
                        @Override
                        public void onClick(DialogInterface dialog, int whichProcess) {

                            if (whichProcess == 0) {    // Dilatation
                                imageProcess = R.string.processMorphologyDilatation;

                                disableParamBar1();
                                disableParamBar2();
                                disableParamBar3();
                                disableParamImg();
                                disableParamInput();
                                disableParamMatrix();
                            }

                            else if (whichProcess == 1) {   // Erosion
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

                else if (whichCategory == 2) {  // Filtres

                    processFilterChooser.setTitle(R.string.processFilterChooserTitle);
                    CharSequence processesFilter[] = new CharSequence[]{
                            getString(R.string.processFilterConvolution),
                            getString(R.string.processFilterMoyenneur),
                            getString(R.string.processFilterGaussien),
                            getString(R.string.processFilterKirsch)
                    };
                    processFilterChooser.setItems(processesFilter, new DialogInterface.OnClickListener() {
                        /**
                         * Action effectuée après le choix du traitement
                         * @param dialog Dialogue de choix du traitement
                         * @param whichProcess Indice du traitement choisi (en commençant par 0)
                         */
                        @Override
                        public void onClick(DialogInterface dialog, int whichProcess) {

                            if (whichProcess == 0) {    // Filtre de convolution
                                imageProcess = R.string.processFilterConvolution;

                                disableParamBar1();
                                disableParamBar2();
                                disableParamBar3();
                                disableParamImg();
                                disableParamInput();

                                enableParamMatrix(R.string.matrixConvolutionText);

                            }

                            else if (whichProcess == 1) {   // Filtre moyenneur
                                imageProcess = R.string.processFilterMoyenneur;

                                disableParamBar1();
                                disableParamBar2();
                                disableParamBar3();
                                disableParamImg();
                                disableParamInput();
                                disableParamMatrix();

                            }

                            else if (whichProcess == 2) {   // Filtre gaussien
                                imageProcess = R.string.processFilterGaussien;

                                disableParamBar1();
                                disableParamBar2();
                                disableParamBar3();
                                disableParamImg();
                                disableParamInput();
                                disableParamMatrix();

                            }

                            else if (whichProcess == 3) {   // Filtre détecteur de contours (Kirsch)
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

                else if (whichCategory == 3) {  // Filtres cosmétiques

                    processCosmeticChooser.setTitle(R.string.processCosmeticChooserTitle);
                    CharSequence processesCosmetic[] = new CharSequence[]{
                            getString(R.string.processCosmeticGrey),
                            getString(R.string.processCosmeticSepia),
                            getString(R.string.processCosmeticNegative),
                            getString(R.string.processCosmeticMirror)
                    };
                    processCosmeticChooser.setItems(processesCosmetic, new DialogInterface.OnClickListener() {
                        /**
                         * Action effectuée après le choix du traitement
                         * @param dialog Dialogue de choix du traitement
                         * @param whichProcess Indice du traitement choisi (en commençant par 0)
                         */
                        @Override
                        public void onClick(DialogInterface dialog, int whichProcess) {

                            if (whichProcess == 0) {    // Niveaux de gris
                                imageProcess = R.string.processCosmeticGrey;

                                disableParamBar1();
                                disableParamBar2();
                                disableParamBar3();
                                disableParamImg();
                                disableParamInput();
                                disableParamMatrix();
                            }

                            else if (whichProcess == 1) {   // Sépia
                                imageProcess = R.string.processCosmeticSepia;

                                disableParamBar1();
                                disableParamBar2();
                                disableParamBar3();
                                disableParamImg();
                                disableParamInput();
                                disableParamMatrix();
                            }

                            else if (whichProcess == 2) {   // Négatif
                                imageProcess = R.string.processCosmeticNegative;

                                disableParamBar1();
                                disableParamBar2();
                                disableParamBar3();
                                disableParamImg();
                                disableParamInput();
                                disableParamMatrix();
                            }

                            else if (whichProcess == 3) {   // Miroir
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

        /**
         * L'action suivante présente peu d'intérêt mais initialise l'animation lors du déplacement horizontal des menus.
         * Sans elle, le premier déplacement se fait sans transition...
         */
        ((ViewFlipper) findViewById(R.id.menuFlipper)).showNext();
        ((ViewFlipper) findViewById(R.id.menuFlipper)).showPrevious();
        ((ViewFlipper) findViewById(R.id.menuFlipper)).setInAnimation(this, R.anim.slide_in_from_right);
        ((ViewFlipper) findViewById(R.id.menuFlipper)).setOutAnimation(this, R.anim.slide_out_to_left);
        findViewById(R.id.menuFlipper).setOnTouchListener(new View.OnTouchListener() {
            /**
             * Listener des évènements tactiles sur menuFlipper (la partie inférieure/menu de l'application)
             * @param v Vue en cours (ignorée)
             * @param event Enregistre les évènements tactiles sur menuFlipper
             * @return true (ignoré)
             */
            public boolean onTouch(View v, MotionEvent event) {
                return menuFlip(event);
            }
        });


        /**
         * Initialisation des listeners d'appui sur les différents boutons
         */
        findViewById(R.id.btLoad).setOnClickListener(new View.OnClickListener() {
            /**
             * Bouton de chargement d'une image depuis la gallerie
             */
            @Override
            public void onClick(View v) {
                btLoadClick(v);
            }
        });


        findViewById(R.id.btCam).setOnClickListener(new View.OnClickListener() {
            /**
             * Bouton de prise d'une image avec l'appareil photo
             */
            @Override
            public void onClick(View v) {
                btCamClick(v);
            }
        });


        findViewById(R.id.btSave).setOnClickListener(new View.OnClickListener() {
            /**
             * Bouton de sauvegarde d'une image dans la gallerie
             */
            @Override
            public void onClick(View v) {
                btSaveClick();
            }
        });



        findViewById(R.id.btChoose).setOnClickListener(new View.OnClickListener() {
            /**
             * Bouton de choix du traitement à effectuer
             */
            @Override
            public void onClick(View v) {
                btChooseClick(v);
            }
        });



        findViewById(R.id.btApply).setOnClickListener(new View.OnClickListener() {
            /**
             * Bouton d'application du traitement choisi
             */
            @Override
            public void onClick(View v) {
                btApplyClick(v);
            }
        });



        findViewById(R.id.btUndo).setOnClickListener(new View.OnClickListener() {
            /**
             * Bouton pour défaire/refaire un traitement (charge l'image en mémoire)
             */
            @Override
            public void onClick(View v) {
                btUndoClick(v);
            }
        });

        findViewById(R.id.btCancel).setOnClickListener(new View.OnClickListener() {
            /**
             * Bouton d'annulation d'un traitement en cours
             */
            @Override
            public void onClick(View v) {
                btCancelClick(v);
            }
        });



        findViewById(R.id.btReference).setOnClickListener(new View.OnClickListener(){
            /**
             * Bouton de chargement d'une image de référence depuis la galerie
             */
            @Override
            public void onClick(View v) {
                btReferenceClick(v);
            }
        });



        findViewById(R.id.btMatrix).setOnClickListener(new View.OnClickListener() {
            /**
             * Bouton d'édition de la matrice passée en paramètre
             */
            @Override
            public void onClick(View v) {
                btMatrixClick(v);
            }
        });


        /**
         * Initialisation des listeners d'interaction avec les sliders
         */
        paramBarControl1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            /**
             * Action effectuée lors du changement de la valeur du slider
             * @param seekBar Le slider dont la valeur est modifiée
             * @param progress La valeur en cours
             * @param fromUser Si la modification vient de l'utilisateur ou de l'application (ignoré)
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                paramBarValue1 = progress;
                paramBarControlText1.setText(Integer.toString(paramBarValue1));
            }

            /**
             * Action effectué lors du début de l'interaction avec le slider (inutilisée)
             * @param seekBar Le slider avec lequel on interagit
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            /**
             * Action effectué à la fin de l'interaction avec le slider (inutilisée)
             * @param seekBar Le slider avec lequel on interagit
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        paramBarControl2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            /**
             * Action effectuée lors du changement de la valeur du slider
             * @param seekBar Le slider dont la valeur est modifiée
             * @param progress La valeur en cours
             * @param fromUser Si la modification vient de l'utilisateur ou de l'application (ignoré)
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                paramBarValue2 = progress;
                paramBarControlText2.setText(Integer.toString(paramBarValue2));
            }

            /**
             * Action effectué lors du début de l'interaction avec le slider (inutilisée)
             * @param seekBar Le slider avec lequel on interagit
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            /**
             * Action effectué à la fin de l'interaction avec le slider (inutilisée)
             * @param seekBar Le slider avec lequel on interagit
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        paramBarControl3.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            /**
             * Action effectuée lors du changement de la valeur du slider
             * @param seekBar Le slider dont la valeur est modifiée
             * @param progress La valeur en cours
             * @param fromUser Si la modification vient de l'utilisateur ou de l'application (ignoré)
             */
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                paramBarValue3 = progress;
                paramBarControlText3.setText(Integer.toString(paramBarValue3));
            }

            /**
             * Action effectué lors du début de l'interaction avec le slider (inutilisée)
             * @param seekBar Le slider avec lequel on interagit
             */
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            /**
             * Action effectué à la fin de l'interaction avec le slider (inutilisée)
             * @param seekBar Le slider avec lequel on interagit
             */
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });



        /**
         * Initialisation des listeners supplémentaires pour l'édition directe des paramètres liés aux sliders
         */
        findViewById(R.id.paramBarText1).setOnClickListener(new View.OnClickListener() {
            /**
             * Edition directe du paramètre du premier slider
             */
            @Override
            public void onClick(View v) {
                changeParamBar1(v);
            }
        });

        findViewById(R.id.paramBarText2).setOnClickListener(new View.OnClickListener() {
            /**
             * Edition directe du paramètre du second slider
             */
            @Override
            public void onClick(View v) {
                changeParamBar2(v);
            }
        });

        findViewById(R.id.paramBarText3).setOnClickListener(new View.OnClickListener() {
            /**
             * Edition directe du paramètre du troisième slider
             */
            @Override
            public void onClick(View v) {
                changeParamBar3(v);
            }
        });


        /**
         * Une fois la vue chargée, on effectue un nouveau passage sur les boutons afin de les redimensionner suivant la taille de l'appareil.
         * Ceci est fait pour assurer que les boutons ne sortent pas des limites de l'écran.
         */
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

    /**
     * Fonction appelée à l'appui de la touche retour du téléphone.
     * Demande confirmation avant de quitter l'application, et demande l'arrêt du traitement si la confirmation est donnée.
     */
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

    /**
     * Fonctions d'activation/désactivation des paramètres (à utiliser lors de l'ajout d'un traitement dans les dialogues de choix plus haut.
     * Tout paramètre non utilisé DOIT être désactivé (au risque d'apparaître inutilement après choix d'un autre traitement y faisant appel)
     */

    /**
     * Fonction d'activation et initialisation du premier slider
     * @param max Valeur maximum du slider
     * @param textColor Couleur de l'affichage de la valeur
     */
    public void enableParamBar1(int max, String textColor){
        paramBarValue1 = 0;
        paramBarControlRow1.setVisibility(View.VISIBLE);
        paramBarControl1.setProgress(paramBarValue1);
        paramBarControl1.setMax(max);
        paramBarControlText1.setTextColor(Color.parseColor(textColor));
        paramBarControlText1.setText(Integer.toString(paramBarValue1));
    }

    /**
     * Fonction de désactivation du premier slider
     */
    public void disableParamBar1(){
        paramBarControlRow1.setVisibility(View.GONE);
    }

    /**
     * Fonction d'activation et initialisation du second slider
     * @param max Valeur maximum du slider
     * @param textColor Couleur de l'affichage de la valeur
     */
    public void enableParamBar2(int max, String textColor){
        paramBarValue2 = 0;
        paramBarControlRow2.setVisibility(View.VISIBLE);
        paramBarControl2.setProgress(paramBarValue2);
        paramBarControl2.setMax(max);
        paramBarControlText2.setTextColor(Color.parseColor(textColor));
        paramBarControlText2.setText(Integer.toString(paramBarValue2));
    }

    /**
     * Fonction de désactivation du second slider
     */
    public void disableParamBar2(){
        paramBarControlRow2.setVisibility(View.GONE);
    }

    /**
     * Fonction d'activation et initialisation du troisième slider
     * @param max Valeur maximum du slider
     * @param textColor Couleur de l'affichage de la valeur
     */
    public void enableParamBar3(int max, String textColor){
        paramBarValue3 = 0;
        paramBarControlRow3.setVisibility(View.VISIBLE);
        paramBarControl3.setProgress(paramBarValue3);
        paramBarControl3.setMax(max);
        paramBarControlText3.setTextColor(Color.parseColor(textColor));
        paramBarControlText3.setText(Integer.toString(paramBarValue3));
    }

    /**
     * Fonction de désactivation du troisième slider
     */
    public void disableParamBar3(){
        paramBarControlRow3.setVisibility(View.GONE);
    }

    /**
     * Fonction d'activation et d'initialisation du choix d'une image de référence
     */
    public void enableParamImg(){
        pixelsReference = null;
        paramImgControlRow.setVisibility(View.VISIBLE);
        paramImgControlText.setTextColor(Color.WHITE);
        paramImgControlText.setText(R.string.imageReferenceEmpty);
        findViewById(R.id.btApply).setVisibility(View.INVISIBLE);
    }

    /**
     * Fonction de désactivation du choix d'une image de référence
     */
    public void disableParamImg(){
        paramImgControlRow.setVisibility(View.GONE);
        findViewById(R.id.btApply).setVisibility(View.VISIBLE);
    }

    /**
     * Fonction d'activation et d'initialisation du champ de texte
     */
    public void enableParamInput(){
        paramInputControl.setText("");
        paramInputControl.setTextColor(Color.LTGRAY);
        paramInputControlRow.setVisibility(View.VISIBLE);
        paramInputControlText.setTextColor(Color.WHITE);
    }

    /**
     * Fonction de désactivation du champ de texte
     */
    public void disableParamInput(){
        paramInputControlRow.setVisibility(View.GONE);
    }

    /**
     * Fonction d'activation et d'initialisation de la matrice de valeurs 5x5
     * @param matrixNameID Référence à la ressource correspondant au nom dans le fichier strings.xml
     */
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

    /**
     * Fonction alternative d'activation et d'initialisation de la matrice de valeurs 5x5
     * Son usage est déconseillé pour faciliter la traduction de l'application, mais la fonction est donnée à titre indicatif.
     * @param matrixNameStr Chaîne de caractères correspondant au nom
     */
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

    /**
     * Fonction de désactivation de la matrice de valeurs 5x5
     */
    public void disableParamMatrix(){
        paramMatrixControlRow.setVisibility(View.GONE);
    }



    /**
     * Fonctions liées à l'interface
     */

    /**
     * Fonction appelée lors de l'interaction tactile avec le menu (hors boutons/paramètres)
     * Cette fonction sert essentiellement à passer entre le menu de sélection de l'image/traitement et le menu des paramètres.
     * @param event Evènement renvoyé par le listener lors de l'interaction
     * @return true (ignoré)
     */
    public boolean menuFlip(MotionEvent event) {
        final int X = (int) event.getRawX();
        switch (event.getAction() & MotionEvent.ACTION_MASK){
            case MotionEvent.ACTION_DOWN:   // Lorsque l'utilisateur effectue un appui sur le menuFlipper
                xDelta = X;
                break;
            case MotionEvent.ACTION_UP:     // Lorsque l'utilisateur relâche son appui sur le menuFlipper
                break;
            case MotionEvent.ACTION_POINTER_DOWN:   // Lorsqu'un appui est effectué alors que le premier est en cours (ignoré)
                break;
            case MotionEvent.ACTION_POINTER_UP:     // Lorsqu'un appui effectué lorsque le premier était en cours est relâché (ignoré)
                break;
            case MotionEvent.ACTION_MOVE:   // Lorsque l'utilisateur effectue un déplacement durant son appui
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

    /**
     * Action de chargement d'une image depuis la gallerie
     * @param v Vue en cours (ignorée)
     */
    public void btLoadClick(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, ""), SELECT_PICTURE);
        // La fonction appelle une application d'exploration des images sur l'appareil
        // et appelle onActivityResult (plus bas) une fois l'image choisie.
    }

    /**
     * Action de prise et chargement de photo
     * @param v Vue en cours (ignorée)
     */
    public void btCamClick(View v) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
        // La fonction appelle une application de prise de photo sur l'appareil
        // et appelle onActivityResult (plus bas) une fois la photo prise.
    }

    /**
     * Action de sauvegarde de l'image active dans la gallerie
      */
    public void btSaveClick() {
        Calendar c = Calendar.getInstance();
        String fileName = "otTER_"+c.get(Calendar.YEAR)+"-"+c.get(Calendar.MONTH)+"-"+c.get(Calendar.DAY_OF_MONTH)
                          +"_"+c.get(Calendar.HOUR_OF_DAY)+"-"+c.get(Calendar.MINUTE)+"-"+c.get(Calendar.SECOND)+".png";
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + fileName;
        // L'image sera enregistrée au format PNG dans le dossier de sauvegarde d'images par défaut de l'appareil Android,
        // avec le nom otTER_YYYY_MM_DD_hh_mm_ss.png adapté au moment de la sauvegarde.

        Bitmap bitmap = ((BitmapDrawable) displayBox.getDrawable()).getBitmap();    // On charge l'image active au format bitmap

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, bytes);      // On compresse l'image bitmap au format PNG

        File file = new File(filePath);

        FileOutputStream fOut;

        try {
            file.createNewFile();       // On crée le fichier otTER_---.png
            fOut = new FileOutputStream(file);
            fOut.write(bytes.toByteArray());    // On charge l'image en format PNG dans le fichier

            ContentResolver cr = getContentResolver();
            String imagePath = file.getAbsolutePath();
            String name = file.getName();
            String desc = getString(R.string.saveDesc);
            MediaStore.Images.Media.insertImage(cr, imagePath, name, desc);  // On insère l'image dans la gallerie

            fOut.flush();
            fOut.close();

            messageBox.setText(R.string.saveSuccess);
        }
        catch (IOException e) {
            messageBox.setText(R.string.saveStoError);
        }
    }

    /**
     * Action de choix du traitement à effectuer
     * Elle fait appel aux dialogues définis plus haut
     * @param v Vue en cours (ignorée)
     */
    public void btChooseClick(View v) {
        ((ViewFlipper) findViewById(R.id.menuFlipper)).setInAnimation(this, R.anim.slide_in_from_right);
        ((ViewFlipper) findViewById(R.id.menuFlipper)).setOutAnimation(this, R.anim.slide_out_to_left);
        processCategoryChooser.show();
    }

    /**
     * Action d'annulation du traitement en cours
     * @param v Vue en cours (ignorée)
     */
    public void btCancelClick(View v) {
        applyTask.cancel(true);
        // Demande à la tâche du traitement de s'arrêter. En pratique, change juste la réponse de applyTask.isCancelled() de false à true.
        // Les algorithmes doivent surveiller cette condition aux moments coûteux !
    }

    /**
     * Action d'exécution du traitement choisi sur l'image active
     * @param v Vue en cours (ignorée)
     */
    public void btApplyClick(View v) {

        // Simple sécurité, le bouton d'exécution de traitement n'est pas censé apparaître en l'absence de traitement sélectionné...
        if (imageProcess == R.string.emptyProcess){
            messageBox.setText(R.string.emptyProcess);

            return;
        }
        else {

            // En revanche, la visibilité du bouton d'exécution ne dépend pas de l'image, cette étape est nécessaire.
            if (displayBox.getDrawable() == null) {
                messageBox.setText(R.string.imageEmpty);

                return;
            }

            BitmapDrawable bitmapDrawable = ((BitmapDrawable) displayBox.getDrawable());
            Bitmap bitmapCurrent = bitmapDrawable.getBitmap();
            bitmapConfig = bitmapCurrent.getConfig();

            // On stocke l'image dans pixelsOld pour pouvoir la rappeler une fois le traitement effectué pour le refaire/défaire
            for (int x=0; x<imageWidth; x++){
                System.arraycopy(pixelsCurrent[x],0,pixelsOld[x],0,imageHeight);
            }

            // On prépare l'affichage de la vue de la barre de progression / bouton d'annulation
            progressBox.setText(String.format(getString(R.string.applyingProcess), getString(imageProcess)));
            progressCount = 0;
            progressBarControl.setProgress(0);
            progressBarControl.setMax(imageHeight * imageWidth);
            ((ViewFlipper) findViewById(R.id.menuFlipper)).setInAnimation(this, R.anim.slide_in_from_bottom);
            ((ViewFlipper) findViewById(R.id.menuFlipper)).setOutAnimation(this, R.anim.slide_out_to_top);
            ((ViewFlipper) findViewById(R.id.menuFlipper)).showNext();      // On affiche la vue.
            ((ViewFlipper) findViewById(R.id.menuFlipper)).setInAnimation(this, R.anim.slide_in_from_top);
            ((ViewFlipper) findViewById(R.id.menuFlipper)).setOutAnimation(this, R.anim.slide_out_to_bottom);

            // On exécute la tâche asynchrone de traitement (plus bas). Elle s'exécute en arrière-plan, laissant l'application réactive.
            // C'est elle qui contient tous les appels aux différents traitements.
            applyTask = new AsyncApplyTask();
            applyTask.execute();
        }

        // On affiche le bouton d'annulation. Si le traitement vient à être annulé, l'affichage sera désactivé à l'annulation.
        findViewById(R.id.btUndo).setVisibility(View.VISIBLE);
        findViewById(R.id.btUndo).setBackgroundResource(R.mipmap.ic_undo);
        cancelled = false;
    }

    /**
     * Action pour défaire/refaire un traitement. Echange l'image active avec l'image en mémoire, si elle existe.
     * @param v Vue en cours (ignorée)
     */
    public void btUndoClick(View v) {

        int pxlSize = (pixelsCurrent.length) * (pixelsCurrent[0].length);
        int[] pixelsTemp = new int[pxlSize];

        // On effectue ici l'échange entre les deux tables.
        for (int x=0; x<pixelsCurrent.length; x++){
            for (int y=0; y<pixelsCurrent[0].length; y++){
                pixelsTemp[pixelsCurrent.length * y + x] = pixelsOld[x][y];
                pixelsOld[x][y] = pixelsCurrent[x][y];
                pixelsCurrent[x][y] = pixelsTemp[pixelsCurrent.length * y + x];
            }
        }

        // On change l'image du bouton suivant s'il s'agit de défaire ou refaire le traitement au prochain appui.
        if (!cancelled){
            findViewById(R.id.btUndo).setBackgroundResource(R.mipmap.ic_redo);
            cancelled = true;
        }
        else {
            findViewById(R.id.btUndo).setBackgroundResource(R.mipmap.ic_undo);
            cancelled = false;
        }

        // On affiche la nouvelle image active.
        Bitmap bitmapNew = Bitmap.createBitmap(pixelsTemp, pixelsCurrent.length, pixelsCurrent[0].length, ((BitmapDrawable) displayBox.getDrawable()).getBitmap().getConfig());
        displayBox.setImageBitmap(bitmapNew);
    }

    /**
     * Action de charger l'image de référence.
     * @param v Vue en cours (ignorée)
     */
    public void btReferenceClick(View v) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, ""), SELECT_REFERENCE_PICTURE);
        // A l'instar de btLoadClick, la fonction appelle une application d'exploration des images
        // sur l'appareil et appelle onActivityResult (plus bas) une fois l'image choisie.
    }

    /**
     * Action d'appeler l'édition de la matrice paramètre.
     * @param v Vue en cours (ignorée)
     */
    public void btMatrixClick(View v) {
        // Ces variables sont stockées en global pour parer aux problèmes causés par la nécessité d'avoir un layout "final"
        paramMatrixTemp = new int[5][5];
        isMatrixValid = true;

        // On construit le builder affichant la matrice
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.matrixConvolutionTitle);

        // Layout vertical, remplissant tout le dialogue
        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        layout.setGravity(Gravity.CENTER);

        // Message avertissant des bornes autorisées de la matrice
        final TextView warning = new TextView(this);
        warning.setText(R.string.valueMatrixWarning);
        warning.setGravity(Gravity.CENTER);
        warning.setVisibility(View.VISIBLE);
        warning.setTextColor(Color.WHITE);

        // Layout horizontal qui permettra de stocker les colonnes
        final LinearLayout layoutMatrixColumns = new LinearLayout(this);
        layoutMatrixColumns.setOrientation(GridLayout.HORIZONTAL);
        layoutMatrixColumns.setGravity(Gravity.CENTER);

        final EditText[][] input = new EditText[5][5];
        for (int i=0;i<5;i++){

            // A chaque colonne, on ajoute un Layout vertical pour faire les 5 cases (une par ligne)
            final LinearLayout layoutMatrixRow = new LinearLayout(this);
            layoutMatrixRow.setOrientation(GridLayout.VERTICAL);
            for (int j=0;j<5;j++){

                // On ajoute enfin un champ de texte dans chaque case.
                input[i][j] = new EditText(this);
                input[i][j].setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);  // Seulement des entiers (possiblement négatifs) autorisés.
                input[i][j].setTextColor(Color.LTGRAY);
                // On adapte la taille des cases en fonction de la largeur de l'affichage de l'appareil.
                input[i][j].setLayoutParams(new LinearLayout.LayoutParams((int)((getResources().getDisplayMetrics().widthPixels)*0.15),LinearLayout.LayoutParams.WRAP_CONTENT));
                if (paramMatrixValue[i][j] != 0) {
                    input[i][j].setText(Integer.toString(paramMatrixValue[i][j]));
                } else {
                    input[i][j].setText("");
                    // Pour simplifier l'affichage, les 0 sont affichés comme des cases vides
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
                            // Pour simplifier l'entrée des données, les cases vides sont interprétées comme des 0.
                        }

                        // Les bornes de la matrice étant définies de -99 à 99, on vérifie qu'il n'y ait pas de dépassement.
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

                    // La somme des coefficients étant automatiquement ramenée à 1, une somme à 0 est considérée invalide.
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

    /**
     * Action pour changer la valeur du premier slider numériquement
     * @param v Vue en cours (ignorée)
     */
    public void changeParamBar1(View v){

        // On construit un dialogue contenant le champ de texte
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.valueChangeTitle);

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Message d'avertissement indiquant les bornes autorisées.
        final TextView warning = new TextView(this);
        warning.setText(String.format(getString(R.string.valueBarWarning), paramBarControl1.getMax()));
        warning.setGravity(Gravity.CENTER);
        warning.setVisibility(View.VISIBLE);
        warning.setTextColor(Color.WHITE);

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);    // Seul les entiers positifs (ou 0) sont autorisés

        layout.addView(warning);
        layout.addView(input);
        builder.setView(layout);

        builder.setPositiveButton(R.string.dialogOK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String m_Text = input.getText().toString();
                int m_int = Integer.parseInt(m_Text);

                // On contrôle que la valeur est bien dans les bornes autorisées.
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

    /**
     * Action pour changer la valeur du second slider numériquement
     * @param v Vue en cours (ignorée)
     */
    public void changeParamBar2(View v){

        // On construit un dialogue contenant le champ de texte
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.valueChangeTitle);

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Message d'avertissement indiquant les bornes autorisées.
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

                // On contrôle que la valeur est bien dans les bornes autorisées.
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

    /**
     * Action pour changer la valeur du troisième slider numériquement
     * @param v Vue en cours (ignorée)
     */
    public void changeParamBar3(View v){

        // On construit un dialogue contenant le champ de texte
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.valueChangeTitle);

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        // Message d'avertissement indiquant les bornes autorisées.
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

                // On contrôle que la valeur est bien dans les bornes autorisées.
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


    /**
     * Fonctions liées au chargement d'image
     */

    /**
     * Fonction appelée à la fin de l'exécution de startActivityForResult
     * Fait appel aux différentes tâches asynchrones plus bas.
     * @param requestCode Identifiant de l'action pour laquelle startActivityForResult a été appelée
     * @param resultCode Identifiant du statut en fin de startActivityForResult (Succès, annulation ou erreur)
     * @param data Résultat de l'appel à startActivityForResult
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            // On affiche la fenêtre de progression de chargement à la place de l'image.
            messageBox.setText(R.string.loading);
            displayBox.setVisibility(View.GONE);
            (findViewById(R.id.imageLoading)).setVisibility(View.VISIBLE);
            switch (requestCode) {
                case SELECT_PICTURE:                        // Chargement de l'image à traiter depuis la gallerie
                    new AsyncImageLoadTask().execute(new loadParams(this, data));
                    break;
                case SELECT_REFERENCE_PICTURE:              // Chargement de l'image de référence depuis la gallerie
                    new AsyncReferenceLoadTask().execute(new loadParams(this, data));
                    break;
                case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:   // Chargement de la photo à traiter depuis l'appareil photo
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

    /**
     * Classe utilisée pour passer plusieurs informations aux tâches asynchrones.
     */
    public class loadParams {
        Context context;
        Intent data;

        loadParams(Context context, Intent data) {
            this.context = context;
            this.data = data;
        }
    }

    /**
     * Tâche asynchrone de chargement de l'image à traiter depuis la gallerie
     */
    private class AsyncImageLoadTask extends AsyncTask<loadParams, Void, Void>
    {
        Bitmap bitmap;
        int tempWidth;
        int tempHeight;

        @Override
        protected Void doInBackground(loadParams... params) {
            try {
                // On charge l'image choisie à l'aide de Picasso afin de gérer les cas d'images sur un cloud
                bitmap = Picasso.with(params[0].context).load(params[0].data.getData()).get();
            } catch (IOException e) {
                return null;
            }

            tempWidth = bitmap.getWidth();
            tempHeight= bitmap.getHeight();

            // Le moteur de rendu empêche l'affichage d'images sortant du cadre 4096x4096, on bloque donc leur chargement.
            if (tempWidth <= 4096 && tempHeight <= 4096) {

                imageWidth = tempWidth;
                imageHeight = tempHeight;

                setProgressLoadMax(imageHeight * imageWidth);
                progressCount = 0;

                pixelsCurrent = new int[imageWidth][imageHeight];
                pixelsOld = new int[imageWidth][imageHeight];
                pixelsTemp = new int[imageWidth * imageHeight];

                for (int x = 0; x < imageWidth; x++) {
                    for (int y = 0; y < imageHeight; y++) {
                        pixelsCurrent[x][y] = bitmap.getPixel(x, y);
                        progressLoadUpdate();   // Ceci nous permet de mettre à jour au fur et à mesure la barre de progression du chargement
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
                    findViewById(R.id.btUndo).setBackgroundResource(R.mipmap.ic_undo);  // On cache le bouton d'annulation si on charge une nouvelle image.
                    findViewById(R.id.btUndo).setVisibility(View.INVISIBLE);
                }
                else {
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
                // On charge l'image choisie à l'aide de Picasso afin de gérer les cas d'images sur un cloud
                bitmap = Picasso.with(params[0].context).load(params[0].data.getData()).get();
            } catch (IOException e) {
                return null;
            }

            // Ici le problème du moteur de rendu ne se pose pas, l'affichage n'étant jamais effectué.

            referenceWidth = bitmap.getWidth();
            referenceHeight = bitmap.getHeight();

            setProgressLoadMax(referenceHeight * referenceWidth);
            progressCount = 0;

            pixelsReference = new int[referenceWidth][referenceHeight];

            for (int x = 0; x < referenceWidth; x++) {
                for (int y = 0; y < referenceHeight; y++) {
                    pixelsReference[x][y] = bitmap.getPixel(x, y);
                    progressLoadUpdate();   // Ceci nous permet de mettre à jour au fur et à mesure la barre de progression du chargement
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
                findViewById(R.id.btApply).setVisibility(View.VISIBLE); // On autorise l'affichage du bouton apply si une image de référence a été chargée.
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
                // On charge la photo à l'aide de Picasso
                bitmap = Picasso.with(params[0].context).load(params[0].data.getData()).get();
            } catch (IOException e) {
                return null;
            }

            tempWidth = bitmap.getWidth();
            tempHeight = bitmap.getHeight();

            // Le moteur de rendu empêche l'affichage d'images sortant du cadre 4096x4096, on bloque donc leur chargement.
            if (tempWidth <= 4096 && tempHeight <= 4096) {

                imageWidth = tempWidth;
                imageHeight = tempHeight;

                setProgressLoadMax(imageHeight * imageWidth);
                progressCount = 0;

                pixelsCurrent = new int[imageWidth][imageHeight];
                pixelsOld = new int[imageWidth][imageHeight];
                pixelsTemp = new int[imageWidth * imageHeight];

                for (int x = 0; x < imageWidth; x++) {
                    for (int y = 0; y < imageHeight; y++) {
                        pixelsCurrent[x][y] = bitmap.getPixel(x, y);
                        progressLoadUpdate();   // Ceci nous permet de mettre à jour au fur et à mesure la barre de progression du chargement
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
                    findViewById(R.id.btUndo).setBackgroundResource(R.mipmap.ic_undo);  // On cache le bouton d'annulation si on charge une nouvelle image.
                    findViewById(R.id.btUndo).setVisibility(View.INVISIBLE);
                }
                else {
                    messageBox.setText(R.string.loadSizeError);
                }
            }
            else
                messageBox.setText(R.string.loadImgError);
            displayBox.setVisibility(View.VISIBLE);
            (findViewById(R.id.imageLoading)).setVisibility(View.GONE);
        }
    }


    /**
     * Fonctions de gestion des barres de progression
     */

    /**
     * Règle le maximum de la barre de progression du traitement de l'image.
     * @param max Maximum de la barre de progression (en général imageWidth * imageHeight)
     */
    public void setProgressMax(int max){
        progressBarControl.setMax(max);
    }

    /**
     * Règle le maximum de la barre de progression du chargement de l'image.
     * @param max Maximum de la barre de progression (en général imageWidth * imageHeight)
     */
    public void setProgressLoadMax(int max){
        progressLoadBarControl.setMax(max);
    }

    /**
     * Met à jour le compteur et, si nécessaire, la barre de progression du traitement de l'image.
     */
    public void progressUpdate(){
        progressCount += 1;
        if ((progressCount % 10000 == 0) | (progressCount == progressBarControl.getMax())) {
            progressBarControl.setProgress(progressCount);
        }
    }

    /**
     * Met à jour le compteur et, si nécessaire, la barre de progression du chargement de l'image.
     */
    public void progressLoadUpdate() {
        progressCount += 1;
        if ((progressCount % 10000 == 0) | (progressCount == progressLoadBarControl.getMax())) {
            progressLoadBarControl.setProgress(progressCount);
        }
    }


    /**
     * Fonctions simplifiées d'accès en lecture/écriture aux valeurs d'un pixel durant un traitement.
     */

    /**
     * Affecte la couleur C (généralement provenant d'un tableau de pixels) à la position (x,y) de l'image en traitement.
     * @param x Position en largeur du pixel à modifier
     * @param y Position en hauteur du pixel à modifier
     * @param C Couleur (se référer à la librairie Color si utilisée en dehors du cadre d'un tableau de pixels)
     */
    public void toPixelCopy(int x, int y, int C){
        pixelsTemp[imageWidth * y + x] = C;
        pixelsCurrent[x][y] = pixelsTemp[imageWidth * y + x];
    }

    /**
     * Affecte la couleur aux composantes (R,G,B) à la position (x,y) de l'image en traitement.
     * Cette fonction met à jour la barre de progression du traitement, est conseillée pour les affectations coûteuses du traitement.
     * @param x Position en largeur du pixel à modifier
     * @param y Position en hauteur du pixel à modifier
     * @param R Composante rouge de la couleur à affecter
     * @param G Composante verte de la couleur à affecter
     * @param B Composante bleue de la couleur à affecter
     */
    public void toPixelRGB(int x, int y, int R, int G, int B){
        pixelsTemp[imageWidth * y + x] = Color.rgb(R,G,B);
        pixelsCurrent[x][y] = pixelsTemp[imageWidth * y + x];
        progressUpdate();
    }

    /**
     * Affecte la couleur aux composantes (R,G,B) à la position (x,y) de l'image en traitement.
     * Cette fonction NE MET PAS à jour la barre de progression du traitement, est plus conseillée
     * pour des affectations dont le coût est négligeable par rapport à la durée totale du traitement.
     * @param x Position en largeur du pixel à modifier
     * @param y Position en hauteur du pixel à modifier
     * @param R Composante rouge de la couleur à affecter
     * @param G Composante verte de la couleur à affecter
     * @param B Composante bleue de la couleur à affecter
     */
    public void toPixelRGB_NoProgress(int x, int y, int R, int G, int B){
        pixelsTemp[imageWidth * y + x] = Color.rgb(R,G,B);
        pixelsCurrent[x][y] = pixelsTemp[imageWidth * y + x];
        // NO PROGRESS UPDATE !
    }

    /**
     * Fonction renvoyant la valeur sur [0..255] du pixel en position (x,y) du
     * tableau pixelsCurrent correspondant à l'image en cours de traitement.
     * @param x Indice en largeur du pixel considéré
     * @param y Indice en hauteur du pixel considéré
     * @param color Indice correspondant à la composante de couleur désirée (0 : Rouge, 1 : Vert, 2 : Bleu)
     * @return Valeur entre 0 et 255
     */
    int getPixelsCurrentColor(int x, int y, int color)
    {
        switch(color)
        {
            case 0 :
                return Color.red(pixelsCurrent[x][y]);
            case 1 :
                return Color.green(pixelsCurrent[x][y]);
            case 2 :
                return Color.blue(pixelsCurrent[x][y]);
            default :
                return 0;
        }
    }

    /**
     * Fonction pour rentrer simplement les valeurs de la matrice paramètre.
     * Le coefficient mXY correspond au coefficient de la colonne (X+1) et ligne (Y+1)
     */
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


    /**
     * Fonctions annexes, utilisées dans le cadre des traitements
     */

    /**
     * Retourne le maximum de deux valeurs
     * @return Le maximum
     */
    int max_2(int a,int b)
    {
        if (a > b)
            return a;
        else
            return b;
    }

    /**
     * Retourne le minimum de deux valeurs
     * @return Le minimum
     */
    int min_2(int a,int b)
    {
        if (a < b)
            return a;
        else
            return b;
    }

    /**
     * Retourne le maximum de 8 valeurs
     * @return Le maximum
     */
    int max_8(int a,int b,int c,int d,int e,int f,int g,int h)
    {
        return max_2(max_2(max_2(a,b),max_2(c,d)),max_2(max_2(e,f),max_2(g,h)));
    }




    /*********************************************************




                        SECTION TRAITEMENTS




     ********************************************************/


    /**
     * Tâche asynchrone d'exécution du traitement.
     * Le booléen running sert à vérifier si la tâche est en cours au moment de quitter l'application.
     */
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


    /**
     * Fonction de seuil
     * Paramètres :
     * - paramBarValue1 : Valeur de seuil pour la composante rouge
     * - paramBarValue2 : Valeur de seuil pour la composante verte
     * - paramBarValue3 : Valeur de seuil pour la composante bleue
     */
    public void applySeuil(){

        int seuilValueR = paramBarValue1;
        int seuilValueG = paramBarValue2;
        int seuilValueB = paramBarValue3;

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


    /**
     * Fonction de spécification
     * Paramètre :
     * - pixelsReference : Tableau des pixels de l'image de référence (referenceWidth et referenceHeight pour les dimensions)
     */
    public void applySpecification(){
        float[][] ddpImg = new float[256][3];
        float[][] ddpRef = new float[256][3];

        int nbPixel = imageWidth * imageHeight;
        int nbPixelRef = referenceWidth * referenceHeight;
        int specR,specG,specB;

        int IndiceMinR, IndiceMaxR, IndiceR, IndiceMinG, IndiceMaxG, IndiceG, IndiceMinB, IndiceMaxB, IndiceB;

        // On lit la densité de probabilité de l'image à traiter
        for(int x = 0 ; x < imageWidth ; x++)
            for(int y = 0 ; y < imageHeight ; y++)
            {
                ddpImg[Color.red(pixelsOld[x][y])][0]++;
                ddpImg[Color.green(pixelsOld[x][y])][1]++;
                ddpImg[Color.blue(pixelsOld[x][y])][2]++;
            }

        // On lit la densité de probabilité de l'image de référence
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

        // On passe les densités en sommes cumulées
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
                toPixelRGB_NoProgress(x, y, specR, specG, specB);
            }
        }

        for (int x = 0; x < imageWidth; x++) {
            for (int y = 0; y < imageHeight; y++) {

                // On cherche par dichotomie l'indice correspondant pour la spécification pour chaque composante
                // Chaque boucle while(true) atteint nécessairement un point de break :
                // - La valeur à encadrer est entre les bornes -> Il existe un indice tel que lui et l'indice précédent encadrent la valeur
                // - La valeur est en-dessous des bornes -> L'indice s'arrête à 1
                // - La valeur est au-dessus des bornes -> L'indice s'arrête à 255

                IndiceMinR = 0;
                IndiceMaxR = 255;

                // Composante rouge
                while (true) {
                    if (!applyTask.isCancelled()) {
                        IndiceR = IndiceMinR + (IndiceMaxR - IndiceMinR + 1) / 2;
                        if ((((255 * ddpRef[IndiceR][0]) >= Color.red(pixelsCurrent[x][y])) && ((255 * ddpRef[IndiceR - 1][0]) <= Color.red(pixelsCurrent[x][y]))) || (IndiceR == 255 || IndiceR == 1)) {
                            specR = IndiceR;
                            break;
                        } else if ((255 * ddpRef[IndiceR][0]) < Color.red(pixelsCurrent[x][y]))
                            IndiceMinR = IndiceR;
                        else IndiceMaxR = IndiceR;
                    }
                    else return;
                }

                IndiceMinG = 0;
                IndiceMaxG = 255;

                // Composante verte
                while (true) {
                    if (!applyTask.isCancelled()) {
                        IndiceG = IndiceMinG + (IndiceMaxG - IndiceMinG + 1) / 2;
                        if ((((255 * ddpRef[IndiceG][1]) >= Color.green(pixelsCurrent[x][y])) && ((255 * ddpRef[IndiceG - 1][1]) <= Color.green(pixelsCurrent[x][y]))) || (IndiceG == 255 || IndiceG == 1)) {
                            specG = IndiceG;
                            break;
                        } else if ((255 * ddpRef[IndiceG][1]) < Color.green(pixelsCurrent[x][y]))
                            IndiceMinG = IndiceG;
                        else IndiceMaxG = IndiceG;
                    }
                    else return;
                }

                IndiceMinB = 0;
                IndiceMaxB = 255;

                // Composante bleue
                while (true){
                    if (!applyTask.isCancelled()) {
                        IndiceB = IndiceMinB + (IndiceMaxB - IndiceMinB + 1) / 2;
                        if ((((255 * ddpRef[IndiceB][2]) >= Color.blue(pixelsCurrent[x][y])) && ((255 * ddpRef[IndiceB -1 ][2]) <= Color.blue(pixelsCurrent[x][y]))) || (IndiceB == 255 || IndiceB == 1)) {
                            specB = IndiceB;
                            break;
                        }
                        else if((255 * ddpRef[IndiceB][2]) < Color.blue(pixelsCurrent[x][y])) IndiceMinB = IndiceB;
                        else IndiceMaxB = IndiceB;
                    }
                    else return;
                }

                toPixelRGB(x, y, specR, specG, specB);

            }
        }

    }

    /**
     * Fonction d'égalisation
     * Pas de paramètre
     */
    public void applyEgalisation(){
        float[][] ddp = new float[256][3];

        int nbPixel = imageWidth * imageHeight;
        int egalR, egalG, egalB;

        // On construit la densité de probabilité de l'image.
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

        // On effectue la transformation d'égalisation
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


    /**
     * Fonction d'expansion
     * Paramètre :
     * - paramBarValue1 : Pourcentage de pixels dont la valeur est coupée pour l'expansion
     */
    public void applyExpansion(){
        float alphaR,alphaG,alphaB,betaR,betaG,betaB;
        int seuil = (int)(imageWidth * imageHeight * paramBarValue1 * 0.01);
        int sum,max_r,min_r,max_g,min_g,max_b,min_b;
        setProgressMax(imageHeight * imageWidth * 2);

        min_r = 0;
        max_r = 0;
        min_g = 0;
        max_g = 0;
        min_b = 0;
        max_b = 0;

        // On construit l'histogramme de l'image
        int[][] hist = new int[256][3];

        for(int x = 0;x < imageWidth;x++)
            for(int y = 0;y < imageHeight;y++)
            {
                hist[Color.red(pixelsOld[x][y])][0]++;
                hist[Color.green(pixelsOld[x][y])][1]++;
                hist[Color.blue(pixelsOld[x][y])][2]++;
            }

        // On effectue le coupage sur chacune des composantes
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

        // On affecte le découpage sur l'image, temporairement.
        for(int x = 0;x < imageWidth;x++)
            for(int y = 0;y < imageHeight;y++)
            {
                if (!applyTask.isCancelled()) {
                    if (Color.red(pixelsCurrent[x][y]) < min_r)
                        toPixelRGB_NoProgress(x, y, min_r, Color.green(pixelsCurrent[x][y]), Color.blue(pixelsCurrent[x][y]));
                    if (Color.red(pixelsCurrent[x][y]) > max_r)
                        toPixelRGB_NoProgress(x, y, max_r, Color.green(pixelsCurrent[x][y]), Color.blue(pixelsCurrent[x][y]));

                    if (Color.green(pixelsCurrent[x][y]) < min_g)
                        toPixelRGB_NoProgress(x, y, Color.red(pixelsCurrent[x][y]), min_g, Color.blue(pixelsCurrent[x][y]));
                    if (Color.green(pixelsCurrent[x][y]) > max_g)
                        toPixelRGB_NoProgress(x, y, Color.red(pixelsCurrent[x][y]), max_g, Color.blue(pixelsCurrent[x][y]));

                    if (Color.blue(pixelsCurrent[x][y]) < min_b)
                        toPixelRGB_NoProgress(x, y, Color.red(pixelsCurrent[x][y]), Color.green(pixelsCurrent[x][y]), min_b);
                    if (Color.blue(pixelsCurrent[x][y]) > max_b)
                        toPixelRGB_NoProgress(x, y, Color.red(pixelsCurrent[x][y]), Color.green(pixelsCurrent[x][y]), max_b);

                    progressUpdate();
                }
                else return;
            }

        // On applique la transformation d'expansion
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

    /**
     * Fonction de dilatation
     * Pas de paramètre
     */
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
                                // On cherche le minimum de la somme des 3 composantes parmi les 8 voisins d'un pixel et lui-même
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

    /**
     * Fonction de d'érosion
     * Pas de paramètre
     */
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
                                // On cherche le maximum de la somme des 3 composantes parmi les 8 voisins d'un pixel et lui-même
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


    /**
     * Fonction de filtre de convolution
     * Paramètre :
     * - paramMatrixValue : grille 5x5 de convolution
     */
    public void applyConvolution() {
        int sum_r,sum_g,sum_b;
        int convR, convG, convB;

        // On copie l'image en sortie pour contrer les effets de bord
        for (int x = 0; x < imageWidth; x++)
            for (int y = 0; y < imageHeight; y++) {
                toPixelCopy(x, y, pixelsOld[x][y]);
            }

        for(int x = 2;x < imageWidth - 2;x++)
            for(int y = 2;y < imageHeight - 2;y++)
            {
                if (!applyTask.isCancelled()) {
                    sum_r = 0;
                    sum_g = 0;
                    sum_b = 0;

                    // On calcule les sommes affectées des coefficients
                    for (int i = x - 2; i < x + 3; i++)
                        for (int j = y - 2; j < y + 3; j++) {
                            sum_r += paramMatrixValue[i - x + 2][j - y + 2] * Color.red(pixelsOld[i][j]);
                            sum_g += paramMatrixValue[i - x + 2][j - y + 2] * Color.green(pixelsOld[i][j]);
                            sum_b += paramMatrixValue[i - x + 2][j - y + 2] * Color.blue(pixelsOld[i][j]);
                        }

                    // On normalise, en s'assurant de ne pas sortir des bornes (possible s'il y a des coefficients négatifs)
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

    /**
     * Fonction de filtre moyenneur
     * Pas de paramètre
     */
    public void applyMoyenneur() {

        // On rentre la grille de valeurs.
        setParamMatrixValue(1, 1, 1, 1, 1,
                            1, 1, 1, 1, 1,
                            1, 1, 1, 1, 1,
                            1, 1, 1, 1, 1,
                            1, 1, 1, 1, 1);
        paramMatrixNorme = 25;

        int sum_r, sum_g, sum_b;

        // On copie l'image en sortie pour contrer les effets de bord
        for (int x = 0; x < imageWidth; x++)
            for (int y = 0; y < imageHeight; y++) {
                toPixelCopy(x, y, pixelsOld[x][y]);
            }

        for(int x = 2;x < imageWidth - 2;x++)
            for(int y = 2;y < imageHeight - 2;y++)
            {
                if (!applyTask.isCancelled()) {
                    sum_r = 0;
                    sum_g = 0;
                    sum_b = 0;

                    // On applique la grille.
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

    /**
     * Fonction de filtre gaussien
     * Pas de paramètre
     */
    public void applyGaussien() {

        // On rentre la grille de valeurs.
        setParamMatrixValue(1, 4,  6,  4,  1,
                            4, 18, 30, 18, 4,
                            6, 30, 48, 30, 6,
                            4, 18, 30, 18, 4,
                            1, 4,  6,  4,  1);
        paramMatrixNorme = 300;

        int sum_r, sum_g, sum_b;

        // On copie l'image en sortie pour contrer les effets de bord
        for (int x = 0; x < imageWidth; x++)
            for (int y = 0; y < imageHeight; y++) {
                toPixelCopy(x, y, pixelsOld[x][y]);
            }

        for(int x = 2;x < imageWidth - 2;x++)
            for(int y = 2;y < imageHeight - 2;y++)
            {
                if (!applyTask.isCancelled()) {
                    sum_r = 0;
                    sum_g = 0;
                    sum_b = 0;

                    // On applique la grille.
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

    /**
     * Fonction de filtre détecteur de contours (Kirsch)
     * Pas de paramètre
     */
    public void applyKirsch() {
        int sum_r,sum_g,sum_b;

        // On copie l'image en sortie pour contrer les effets de bord
        for (int x = 0; x < imageWidth; x++)
            for (int y = 0; y < imageHeight; y++) {
                toPixelCopy(x, y, pixelsOld[x][y]);
            }

        // On rentre la grille
        setParamMatrixValue(2, 4,  5,  4,  2,
                            4, 9,  12, 9,  4,
                            5, 12, 15, 12, 5,
                            4, 9,  12, 9,  4,
                            2, 4,  5,  4,  2);
        paramMatrixNorme = 159;

        int[][][] grad_temp = new int[imageWidth][imageHeight][3];

        setProgressMax(imageHeight * imageWidth * 2);

        for(int x = 2;x < imageWidth - 2;x++)
            for(int y = 2;y < imageHeight - 2;y++)
            {
                if (!applyTask.isCancelled()) {
                    sum_r = 0;
                    sum_g = 0;
                    sum_b = 0;

                    // On applique la grille.
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

        // On applique la deuxième partie du filtre de Kirsch.
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
                toPixelRGB_NoProgress(x, y, grad_temp[x][y][0], grad_temp[x][y][1], grad_temp[x][y][2]);
    }

    /**
     * Fonction intermédiaire pour l'application du filtre de Kirsch
     * @param x Indice en largeur du pixel considéré
     * @param y Indice en hauteur du pixel considéré
     * @param color Indice correspondant à la composante de couleur désirée (0 : Rouge, 1 : Vert, 2 : Bleu)
     * @return Valeur entre 0 et 255
     */
    int kirsch(int x,int y,int color)
    {
        int k = 5 * (getPixelsCurrentColor(x - 1, y - 1, color) + getPixelsCurrentColor(x, y - 1, color) + getPixelsCurrentColor(x + 1, y - 1, color));
        k += -3 * (getPixelsCurrentColor(x - 1, y, color) + getPixelsCurrentColor(x + 1, y, color) + getPixelsCurrentColor(x - 1, y + 1, color) + getPixelsCurrentColor(x, y + 1, color) + getPixelsCurrentColor(x + 1, y + 1, color));

        int g1,g2,g3,g4,g5,g6,g7,g8;

        g1 = k;

        g2 = k + -8 * getPixelsCurrentColor(x + 1, y - 1, color) + 8 * getPixelsCurrentColor(x - 1, y, color);

        g3 = k + -8 * getPixelsCurrentColor(x + 1, y - 1, color) + 8 * getPixelsCurrentColor(x - 1, y, color) + -8 * getPixelsCurrentColor(x, y - 1, color) + 8 * getPixelsCurrentColor(x - 1, y + 1, color);

        g4 = k +  -8*(getPixelsCurrentColor(x + 1, y - 1, color) + getPixelsCurrentColor(x, y - 1, color) + getPixelsCurrentColor(x - 1, y - 1, color)) + 8*(getPixelsCurrentColor(x - 1, y, color) + getPixelsCurrentColor(x - 1, y + 1, color) + getPixelsCurrentColor(x, y + 1, color));

        g5 = k +  -8*(getPixelsCurrentColor(x + 1, y - 1, color) + getPixelsCurrentColor(x, y - 1, color) + getPixelsCurrentColor(x - 1, y - 1, color)) + 8*(getPixelsCurrentColor(x - 1, y + 1, color) + getPixelsCurrentColor(x, y + 1, color)+ getPixelsCurrentColor(x + 1, y + 1, color));

        g6 = k +  -8*(getPixelsCurrentColor(x + 1, y - 1, color) + getPixelsCurrentColor(x, y - 1, color) + getPixelsCurrentColor(x - 1, y - 1, color)) + 8*(getPixelsCurrentColor(x + 1, y, color) + getPixelsCurrentColor(x, y + 1, color)+ getPixelsCurrentColor(x + 1, y + 1, color));

        g7 = k +  -8*(getPixelsCurrentColor(x, y - 1, color) + getPixelsCurrentColor(x - 1, y - 1, color)) + 8*(getPixelsCurrentColor(x + 1, y, color) + getPixelsCurrentColor(x + 1, y + 1, color));

        g8 = k +  -8*(getPixelsCurrentColor(x - 1, y - 1, color)) + 8*(getPixelsCurrentColor(x + 1, y, color) );

        return max_8(g1,g2,g3,g4,g5,g6,g7,g8);
    }


    /**
     * Fonction pour la transformation en niveaux de gris
     */
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

    /**
     * Fonction pour la transformation en sépia
     */
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

    /**
     * Fonction pour la transformation en négatif
     */
    public void applyNegative() {
        for (int x = 0; x < imageWidth; x++)
            for (int y = 0; y < imageHeight; y++)
                if (!applyTask.isCancelled()) {
                    toPixelRGB(x, y, 255 - Color.red(pixelsOld[x][y]), 255 - Color.green(pixelsOld[x][y]), 255 - Color.blue(pixelsOld[x][y]));
                }
                else return;
    }

    /**
     * Fonction pour la transformation miroir selon l'axe horizontal placé au milieu de l'image.
     */
    public void applyMirror() {
        for (int x = 0; x < imageWidth; x++)
            for (int y = 0; y < imageHeight; y++)
                if (!applyTask.isCancelled()) {
                    toPixelRGB(x, y, Color.red(pixelsOld[imageWidth - 1 - x][y]), Color.green(pixelsOld[imageWidth - 1 - x][y]), Color.blue(pixelsOld[imageWidth - 1 - x][y]));
                }
                else return;
    }

}


