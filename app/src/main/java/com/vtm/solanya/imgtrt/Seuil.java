package com.vtm.solanya.imgtrt;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;

public class Seuil extends Traitement {
    public Seuil(Context context, Activity activity, Image image){
        this.context = context;
        this.activity = activity;
        this.currentImage = image;
    }

    public void applyProcess(){
        int seuilValueR = paramBarValue1;
        int seuilValueG = paramBarValue2;
        int seuilValueB = paramBarValue3;

        // pixelsTemp est la table de l'image à retourner

        int red, green, blue, RSeuil, GSeuil, BSeuil;

        for (int x = 0; x < currentImage.imageWidth; x++) {
            for (int y = 0; y < currentImage.imageHeight; y++) {

                // On lit les 3 couleurs en [0..255]

                red = Color.red(currentImage.pixelsOld[x][y]);
                green = Color.green(currentImage.pixelsOld[x][y]);
                blue = Color.blue(currentImage.pixelsOld[x][y]);

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
}
