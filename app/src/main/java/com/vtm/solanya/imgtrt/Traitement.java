package com.vtm.solanya.imgtrt;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class Traitement {
    Context context;
    Activity activity;
    public Image currentImage;

    public Traitement(){
        this.context = null;
        this.activity = null;
    }

    public Traitement(Context context, Activity activity, Image image){
        this.context = context;
        this.activity = activity;
        this.currentImage = image;
    }

    public int[] pixelsTemp;
    public int progressCount = 0;

    TextView messageBox;
    ImageView displayBox;
    ProgressBar progressBarControl;
    ViewFlipper menuFlipper;

    public int paramBarValue1 = 0;
    public int paramBarValue2 = 0;
    public int paramBarValue3 = 0;

    public void initializeProcess(){

        messageBox = (TextView) activity.findViewById(R.id.textBox);
        displayBox = (ImageView) activity.findViewById(R.id.imageDisplay);
        progressBarControl = (ProgressBar) activity.findViewById(R.id.progressBar);
        menuFlipper = (ViewFlipper) activity.findViewById(R.id.menuFlipper);

        pixelsTemp = new int[currentImage.imageHeight * currentImage.imageWidth];

        if (displayBox.getDrawable() == null) {
            messageBox.setText("No image.");

            return;
        }

        BitmapDrawable bitmapDrawable = ((BitmapDrawable) displayBox.getDrawable());
        Bitmap bitmapCurrent = bitmapDrawable.getBitmap();
        currentImage.bitmapConfig = bitmapCurrent.getConfig();

        for (int x=0; x<currentImage.imageWidth; x++){
            for (int y=0; y<currentImage.imageHeight; y++){
                currentImage.pixelsOld[x][y] = currentImage.pixelsCurrent[x][y];
            }
        }

        progressCount = 0;
        progressBarControl.setProgress(0);
        progressBarControl.setMax(currentImage.imageHeight * currentImage.imageWidth);
        menuFlipper.setInAnimation(context, R.anim.slide_in_from_bottom);
        menuFlipper.setOutAnimation(context, R.anim.slide_out_to_top);
        menuFlipper.showNext();
        menuFlipper.setInAnimation(context, R.anim.slide_in_from_top);
        menuFlipper.setOutAnimation(context, R.anim.slide_out_to_bottom);

        new AsyncApplyTask().execute();

        activity.findViewById(R.id.btCancel).setVisibility(View.VISIBLE);
        ((Button) activity.findViewById(R.id.btCancel)).setText("Annuler");
        currentImage.cancelled = false;
    }

    private class AsyncApplyTask extends AsyncTask<Void, Void, Void>
    {
        ImageView displayBox = (ImageView) activity.findViewById(R.id.imageDisplay);
        ViewFlipper menuFlipper = (ViewFlipper) activity.findViewById(R.id.menuFlipper);

        @Override
        protected Void doInBackground(Void... params) {
            applyProcess();
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            Bitmap bitmapNew = Bitmap.createBitmap(pixelsTemp, currentImage.imageWidth, currentImage.imageHeight, currentImage.bitmapConfig);
            displayBox.setImageBitmap(bitmapNew);
            menuFlipper.showPrevious();
        }
    }

    public void applyProcess(){
        messageBox = (TextView) activity.findViewById(R.id.textBox);
        messageBox.setText("Something has gone wrong.");
    }

    public void toPixelCopy(int x, int y, int C){
        pixelsTemp[currentImage.imageWidth * y + x] = C;
        currentImage.pixelsCurrent[x][y] = pixelsTemp[currentImage.imageWidth * y + x];
        //progressBarUpdate();
    }

    public void toPixelRGB(int x, int y, int R, int G, int B){
        pixelsTemp[currentImage.imageWidth * y + x] = Color.rgb(R, G, B);
        currentImage.pixelsCurrent[x][y] = pixelsTemp[currentImage.imageWidth * y + x];
        progressBarUpdate();
    }

    public void progressBarUpdate(){
        progressBarControl = (ProgressBar) activity.findViewById(R.id.progressBar);
        progressCount += 1;
        if ((progressCount%1000 == 0) | (progressCount == currentImage.imageHeight*currentImage.imageWidth)) {
            progressBarControl.setProgress(progressCount);
        }
    }
}