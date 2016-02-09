package com.vtm.solanya.test;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * Sélectionner une photo dans la gallerie
 * http://www.fobec.com/java/1161/selectionner-puis-afficher-une-image.html
 * @author Axel fevrier 2015
 */
public class Main extends Activity {
    //constante pour définir l'id du type image

    final static int SELECT_PICTURE = 1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ((Button) findViewById(R.id.btGallery)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btGalleryClick(v);
            }
        });

        ((Button) findViewById(R.id.btSeuil)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btSeuilClick(v);
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

    public void btSeuilClick(View v) {

        ImageView iv = (ImageView) findViewById(R.id.imageView1);

        BitmapDrawable bitmapDrawable = ((BitmapDrawable) iv.getDrawable());
        Bitmap bitmap = bitmapDrawable.getBitmap();

        int[][] pixels = new int[bitmap.getHeight()][bitmap.getWidth()];
        int[] pixels2 = new int[bitmap.getHeight()*bitmap.getWidth()];

        int red,green,blue,RSeuil,GSeuil,BSeuil;

        for (int x=0;x<bitmap.getHeight();x++) {
            for (int y=0;y<bitmap.getWidth();y++) {
                pixels[x][y] = bitmap.getPixel(x,y);
                red = Color.red(pixels[x][y]);
                green = Color.green(pixels[x][y]);
                blue = Color.blue(pixels[x][y]);
                if (red < 128) {
                    RSeuil = 0;
                }
                else {
                    RSeuil = 255;
                }
                if (green < 128) {
                    GSeuil = 0;
                }
                else {
                    GSeuil = 255;
                }
                if (blue < 128) {
                    BSeuil = 0;
                }
                else {
                    BSeuil = 255;
                }
                pixels2[bitmap.getHeight()*y+x] = Color.rgb(RSeuil,GSeuil,BSeuil);
            }
        }

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
        TextView textView = (TextView) findViewById(R.id.tvStatus);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SELECT_PICTURE:
                    String path = getRealPathFromURI(data.getData());
                    Log.d("Choose Picture", path);
                    //Transformer la photo en Bitmap
                    Bitmap bitmap = BitmapFactory.decodeFile(path);
                    //Afficher le Bitmap
                    mImageView.setImageBitmap(bitmap);
                    //Renseigner les informations status
                    textView.setText("");
                    textView.append("Fichier: " + path);
                    textView.append(System.getProperty("line.separator"));
                    textView.append("Taille: " + bitmap.getWidth() + "px X " + bitmap.getHeight() + " px");
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
