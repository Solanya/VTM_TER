package com.vtm.solanya.imgtrt;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;

public class Flou extends Traitement {

    public Flou(Context context, Activity activity, Image image){
        this.context = context;
        this.activity = activity;
        this.currentImage = image;
    }

    public void applyProcess(){

        int flouWindow = paramBarValue1;

        int rFlou, gFlou, bFlou;

        for (int x = 0; x < currentImage.imageWidth; x++) {
            for (int y = 0; y < currentImage.imageHeight; y++) {

                // Si l'image a déjà été rentrée dans la table pixelsCurrent, on ne la relit pas.

                toPixelCopy(x,y,currentImage.pixelsOld[x][y]);
            }
        }

        for (int x = flouWindow; x < currentImage.imageWidth-flouWindow; x++) {
            for (int y = flouWindow; y < currentImage.imageHeight-flouWindow; y++) {

                rFlou = 0;
                gFlou = 0;
                bFlou = 0;

                // On applique le flou en lisant les couleurs en [0..255]
                for (int i = -flouWindow; i < flouWindow + 1; i++){
                    for (int j = -flouWindow; j < flouWindow + 1; j++){
                        rFlou += Color.red(currentImage.pixelsOld[x + i][y + j]);
                        gFlou += Color.green(currentImage.pixelsOld[x + i][y + j]);
                        bFlou += Color.blue(currentImage.pixelsOld[x + i][y + j]);
                    }
                }

                rFlou /= java.lang.Math.pow(2 * flouWindow + 1, 2);
                gFlou /= java.lang.Math.pow(2 * flouWindow + 1, 2);
                bFlou /= java.lang.Math.pow(2 * flouWindow + 1, 2);

                toPixelRGB(x, y, rFlou, gFlou, bFlou);
            }
        }
    }
}
