package com.example.katell.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.*;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.graphics.Bitmap.createBitmap;
import static com.example.katell.myapplication.R.id.imageView;

public class MainActivity extends AppCompatActivity {

    /**
     * Attribut correspondant au bitmap contenue dans l'ImageView
     */
    protected Bitmap bitmap;

    /**
     * Sert dans la méthode qui permet de charger une image de la galerie
     */
    private static int RESULT_LOAD_IMG = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //imageView : initialisation
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        BitmapFactory.Options option = new BitmapFactory.Options();
        bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.hamster,option); //hamster : nom de l'image affiché au lancement de l'application
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        imageView.setImageBitmap(bitmap);
    }



    /**
     * Permet de relier le fichier menu.xml pour en donner un menu sur l'application
     * On transforme le xml vers un format plus intéressant (ici une activité)
     * @param menu
     * @return true
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }



    /**
     * Redirection de l'action quand un item a été pressé
     * @param item
     */
    public void onOptionsItem(MenuItem item) {
        switch (item.getItemId()) {

            //pour griser
            case R.id.gray:
                toGray();
                break;

            //pour appliquer une teinte aléatoire
            case R.id.random:
                int hue = (int) (Math.random() * 360);
                colorize(hue);
                break;

            //pour appliquer une teinte rouge
            case R.id.red:
                colorize(0);
                break;

            //pour appliquer une teinte verte
            case R.id.green:
                colorize(100);
                break;

            //pour appliquer une teinte bleu
            case R.id.blue:
                colorize(240);
                break;

            //pour appliquer une teinte sepia
            case R.id.sepia:
                sepia();
                break;

            //pour contraster
            case R.id.egalisation:
                egalisation();
                break;

            //pour zoomer (2x)
            case R.id.zoom:
                zoom(2);
                break;

            //permet de mettre une image (hamster)
            case R.id.hamster:
                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hamster);
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                imageView.setImageBitmap(bitmap);
                break;

            //permet de mettre une image (smarties)
            case R.id.smarties:
                imageView = (ImageView) findViewById(R.id.imageView);
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.smarty);
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                imageView.setImageBitmap(bitmap);
                break;

            //convolution gaussienne
            case R.id.gaussien:
                int[][] gaussien = new int[][] {{1,2,1},{2,4,2},{1,2,1}}; // 16
                convolute(gaussien, 16);
                break;

            //convolution moyenneur
            case R.id.moyenneur:
                int[][] moyenneur = new int[][]{{1,1,1},{1,1,1},{1,1,1}}; // 9
                convolute(moyenneur, 9);
                break;

            //convolution sobel
            case R.id.sobel:
                int[][] sobel = new int[][]{{-1,0,1},{-2,0,2},{-1,0,1}}; // 1
                convolute(sobel, 1);
                break;

            //convolution laplacien
            case R.id.laplacien:
                int[][]laplacien = new int[][]{{0,1,0},{1,-4,1},{0,1,0}}; // 1
                convolute(laplacien, 1);
                break;

            //charger une image de la galerie
            case R.id.gallery:
                LoadGallery();
                break;
        }
    }


    /**
     * Permet quand on touche l'écran de générer un évènement
     * ici un zoom x2
     * @param e
     * @return
     */
    public boolean onTouchEvent(MotionEvent e) { //zoom en appuyant sur l'ecran
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        bitmap = drawable.getBitmap();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (bitmap != null) {
                    zoom(2);
                }
                break;
        }
        return true;
    }



    /**
     * Permet de griser l'image
     */
    public void toGray() {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int[] pixels = new int[height*width];
        bitmap.getPixels(pixels,0,width,0,0,width,height);

        for (int i=0 ; i<pixels.length ; i++) {
            int color = pixels[i];
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            int gray = (30*red + 59*green + 11*blue)/100;
            color = Color.rgb(gray,gray,gray);
            pixels[i] = color;
        }

        bitmap.setPixels(pixels,0,width,0,0,width,height);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);
    }



    /**
     * colorie l'image selon une teinte donnée en paramètre
     * @param hue la teinte à appliquer à l'image
     */
    public void colorize(int hue) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        int[] pixels = new int[height*width];
        bitmap.getPixels(pixels,0,width,0,0,width,height);

        int color;
        float[] hsv = new float[3];

        for (int i=0 ; i<pixels.length ; i++) {
            color = pixels[i];
            Color.colorToHSV(color,hsv);
            hsv[0] = hue;
            color = Color.HSVToColor(hsv);
            pixels[i] = color;
        }

        bitmap.setPixels(pixels,0,width,0,0,width,height);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);
    }



    /**
     * l'image passe avec une teinte sepia
     * l'algo est basé sur celui de la teinte
     */
    public void sepia() {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int[] pixels = new int[height*width];
        bitmap.getPixels(pixels,0,width,0,0,width,height);

        for (int i=0 ; i<pixels.length ; i++) {
            int color = pixels[i];
            float[] hsv = new float[3];
            Color.colorToHSV(color,hsv);
            hsv[0] = 30;
            hsv[1] = hsv[1]*55/100;
            hsv[2] = hsv[2]*92/100;
            color = Color.HSVToColor(hsv);
            pixels[i] = color;
        }

        bitmap.setPixels(pixels,0,width,0,0,width,height);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);
    }





    /**
     * Egalisation d'histogramme d'une image en nuances de gris
     */
    public void egalisation() {
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int[] hist = new int[256];
        int[] pixels = new int[height * width];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < pixels.length; i++) {
            int gray = (30 * Color.red(pixels[i]) + 59 * Color.green(pixels[i]) + 11 * Color.blue(pixels[i])) / 100;
            hist[gray]++;
        }
        int[] c = new int[256];
        for (int k = 0; k < 256; k++) {
            for (int j = 0; j < k; j++) {
                c[k] = c[k] + hist[j];
            }
        }
        int[] pixelsN = new int[height * width];
        for (int i = 0; i < pixels.length; i++) {
            int gray = (30 * Color.red(pixels[i]) + 59 * Color.green(pixels[i]) + 11 * Color.blue(pixels[i])) / 100;
            pixelsN[i] = Color.rgb((c[gray] * 255) / (width * height), (c[gray] * 255) / (width * height), (c[gray] * 255) / (width * height));
        }
        bitmap.setPixels(pixelsN, 0, width, 0, 0, width, height);
        imageView.setImageBitmap(bitmap);
    }







    /**
     * Zoom par interpolation au plus proche voisin
     * @param zoom : facteur de zoom fixe
     */
    public void zoom(int zoom) {
        //S'occuper de la taille
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int heightN = zoom*height;
        int widthN = zoom*width;

        //recuperer pixels de bitmap
        int[] pixels = new int[height*width];
        bitmap.getPixels(pixels,0,width,0,0,width,height);

        Bitmap bmp = createBitmap(widthN,heightN,Bitmap.Config.ARGB_8888);
        int[] pixelsN = new int[heightN*widthN];
        for(int i=0 ; i<heightN*widthN ; i++) {
            int x = (i%widthN)/zoom;
            int y = (i/widthN)/zoom;
            pixelsN[i] = pixels[x+y*width];
        }

        bmp.setPixels(pixelsN,0,widthN,0,0,widthN,heightN);
        bitmap = bmp;
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);
    }



    /**
     * Permet d'appliquer une convolution avec une matrice 3*3
     * @param kernel
     * @param factor
     */
    public void convolute(int[][] kernel,int factor) {

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] pixelsN = pixels.clone();
        int max =255;
        for (int i = 1; i < width-1; i++) {
            for (int j = 1; j < height-1; j++) {

                int red = kernel[0][0]*Color.red(pixels[i-1 + (j-1)*width])+kernel[0][1]*Color.red(pixels[i + (j-1)*width])+kernel[0][2]*Color.red(pixels[i+1 + (j-1)*width])+kernel[1][0]*Color.red(pixels[i-1 + j*width])
                        +kernel[1][1]*Color.red(pixels[i + j*width])+kernel[1][2]*Color.red(pixels[i+1 + j*width])+kernel[2][0]*Color.red(pixels[i-1 + (j+1)*width])+kernel[2][1]*Color.red(pixels[i + (j+1)*width])+kernel[2][2]*Color.red(pixels[i+1 + (j+1)*width]);

                int green = kernel[0][0]*Color.green(pixels[i-1 + (j-1)*width])+kernel[0][1]*Color.green(pixels[i + (j-1)*width])+kernel[0][2]*Color.green(pixels[i+1 + (j-1)*width])+kernel[1][0]*Color.green(pixels[i-1 + j*width])
                        +kernel[1][1]*Color.green(pixels[i + j*width])+kernel[1][2]*Color.green(pixels[i+1 + j*width])+kernel[2][0]*Color.green(pixels[i-1 + (j+1)*width])+kernel[2][1]*Color.green(pixels[i + (j+1)*width])+kernel[2][2]*Color.green(pixels[i+1 + (j+1)*width]);

                int blue = kernel[0][0]*Color.blue(pixels[i-1 + (j-1)*width])+kernel[0][1]*Color.blue(pixels[i + (j-1)*width])+kernel[0][2]*Color.blue(pixels[i+1 + (j-1)*width])+kernel[1][0]*Color.blue(pixels[i-1 + j*width])
                        +kernel[1][1]*Color.blue(pixels[i + j*width])+kernel[1][2]*Color.blue(pixels[i+1 + j*width])+kernel[2][0]*Color.blue(pixels[i-1 + (j+1)*width])+kernel[2][1]*Color.blue(pixels[i + (j+1)*width])+kernel[2][2]*Color.blue(pixels[i+1 + (j+1)*width]);

                red = red/factor;
                green = green/factor;
                blue = blue/factor;

                if (red > max) {
                    red = max;
                }
                if (green > max) {
                    green = max;
                }
                if (blue > max) {
                    blue = max;
                }
                //int gray = (30 * red + 59 * green + 11 * blue) / 100;

                pixelsN[i+j*width] = Color.rgb(red, green, blue);
            }
        }
        bitmap.setPixels(pixelsN, 0, width, 0, 0, width, height);
        imageView.setImageBitmap(bitmap);
    }


    /**
     * Méthode qui permet de charger une image depuis la galerie
     */
    public void LoadGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK); //MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();

        Uri data = Uri.parse(pictureDirectoryPath);
        galleryIntent.setDataAndType(data, "image/*");
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK){
            if (requestCode == RESULT_LOAD_IMG){
                Uri imageUri = data.getData();
                InputStream inputStream;

                try{
                    inputStream = getContentResolver().openInputStream(imageUri);

                    Bitmap bmp = BitmapFactory.decodeStream(inputStream);

                    ImageView imageView = (ImageView) findViewById(R.id.imageView);
                    bitmap = bmp;
                    bitmap = bitmap.copy(Bitmap.Config.ARGB_8888,true);
                    imageView.setImageBitmap(bmp);

                } catch (FileNotFoundException e){
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to open image",Toast.LENGTH_LONG).show();
                }
            }
        }
    }



    /**
     * Provoque un effet de surexposition
     */
 /*   public void surexposition(int coef) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int[] pixels = new int[height*width];
        bitmap.getPixels(pixels,0,width,0,0,width,height);

        for (int i=0 ; i<pixels.length ; i++) {
            int red = Color.red(pixels[i]);
            red = coef * red;
            if (red>255) { red = 255; }
            int green = Color.green(pixels[i]);
            green = coef * green;
            if (green>255) { green = 255; }
            int blue = Color.blue(pixels[i]);
            blue = coef * blue;
            if (blue>255) { blue = 255; }
            pixels[i] = Color.rgb(red,green,blue);
        }

        bitmap.setPixels(pixels,0,width,0,0,width,height);
    }*/



    /**
     * Contraste d'une image en nuance de gris
     * Par egalisation d'histogramme
     */
   /* public void contrast() {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int[] pixels = new int[height*width];
        bitmap.getPixels(pixels,0,width,0,0,width,height);

        int[] h = new int[256];
        for(int i=0 ; i<256 ; i++) {
            h[i] = 0;
        }

        for (int i=0 ; i<pixels.length ; i++) {
            int color = Color.red(pixels[i]);
            h[color]++;
        }

        int[] c = new int[256];
        int sum = 0;
        for(int i=0 ; i<256 ; i++) {
            sum = sum + h[i];
            c[i] = sum;
        }

        for (int i=0 ; i<pixels.length ; i++) {
            int color = Color.red(pixels[i]);
            color = (c[color] * 255) / pixels.length;
            pixels[i] = Color.rgb(color,color,color);
        }

        bitmap.setPixels(pixels,0,width,0,0,width,height);
    }*/



    /**
     Contraste une image en nuance de gris
     par extension de dynamique
     */
 /*   public void contrast() {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int[] pixels = new int[height*width];
        bitmap.getPixels(pixels,0,width,0,0,width,height);
        int min = 255;
        int max = 0;
        int[] contrast = new int[height*width];

        for (int i=0 ; i<pixels.length ; i++) {
            int color = Color.red(pixels[i]);
            contrast[i] = color;

            if (color<min) {
                min = color;
            }
            if (color>max) {
                max = color;
            }
        }

        int[] LUT = new int[256];
        for(int i=0 ; i<256 ; i++) {
            LUT[i] = (255 * (i - min)) / (max - min);
            //LUT[i] = (i * (max -min) / 255) + min; //Permet de diminuer le contraste
        }

        for (int i=0 ; i<pixels.length ; i++) {
            int color = LUT[contrast[i]];
            pixels[i] = Color.rgb(color,color,color);
        }

        bitmap.setPixels(pixels,0,width,0,0,width,height);
    }*/

}
