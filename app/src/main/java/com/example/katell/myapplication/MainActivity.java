package com.example.katell.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.graphics.Bitmap.createBitmap;

public class MainActivity extends AppCompatActivity {

    /**
     * Attribut correspondant au bitmap contenue dans l'ImageView
     */
    private Bitmap bitmap;
    SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Cacher le seekBar
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setVisibility(View.INVISIBLE);

        //imageView : initialisation
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        BitmapFactory.Options option = new BitmapFactory.Options();
        bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.pas_contraste,option); //hamster : nom de l'image affiché au lancement de l'application
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
        seekBar.setVisibility(View.INVISIBLE);

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

            //permet de regler la luminosite
            case R.id.brightness:
                brightness();
                break;

            //pour contraster
            case R.id.contrast:
                contrast();
                break;

            case R.id.histogram:
                histogram();
                break;

            //pour zoomer (2x)
            case R.id.zoom:
                zoom(2);
                break;

            //permet de mettre une image (hamster)
            case R.id.hamster:
                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pas_contraste);
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

            //permet de prendre une photo
            case R.id.photo:
                takePhoto();
                break;
        }
    }



    /**
     * Permet de griser l'image
     */
    private void toGray() {
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
    private void colorize(int hue) {
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
    private void sepia() {
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
     * Contraste d'une image en couleur
     * Par extension de dynamique
     * utilise avec une seekBar
     */
    int[] pixelsContrast; //problème d'initialisation de taille
    private void contrast() {
        //gerer la seekBar
        seekBar.setVisibility(View.VISIBLE);
        seekBar.setProgress(61);
        seekBar.setMax(122);
        seekBar.setThumb(getResources().getDrawable(R.drawable.ic_tonality_black_24dp));

        //recuperer le tableau de pixels de l'image initial
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        pixelsContrast = new int[height*width];
        bitmap.getPixels(pixelsContrast, 0, width, 0, 0, width, height);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int min = 122 - progress;
                int max = 122 + progress;
                if (progress<61) {
                    contrastLower(min,max);
                } else {
                    contrastIncrease(min,max);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }


    /**
     * Est lancé quand le contraste va diminuer
     * @param min
     * @param max
     */
    private void contrastLower(int min, int max) {
        int[] LUT = new int[256];
        for(int i=0 ; i<256 ; i++) {
            LUT[i] = (i * (max-min) / 255) + min;
        }
        contrast(LUT,min,max);
    }


    /**
     * Est lancé quand le contraste augmente
     * @param min
     * @param max
     */
    private void contrastIncrease(int min, int max) {
        int[] LUT = new int[256];
        for(int i=0 ; i<min ; i++) {
            LUT[i] = 0;
        }
        for(int i=min ; i<max ; i++) {
            LUT[i] = (255 * (i-min)) / (max - min) ;
        }
        for(int i=max ; i<256 ; i++) {
            LUT[i] = 255;
        }
        contrast(LUT,min,max);
    }


    private void contrast(int[] LUT, int min, int max) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int[] pixels = new int[height*width];
        bitmap.getPixels(pixels,0,width,0,0,width,height);

        for (int i=0 ; i<pixelsContrast.length ; i++) {
            int red = Color.red(pixelsContrast[i]);
            red = changeColor(red,LUT,min,max);

            int green = Color.green(pixelsContrast[i]);
            green = changeColor(green,LUT,min,max);

            int blue = Color.blue(pixelsContrast[i]);
            blue = changeColor(blue,LUT,min,max);

            pixels[i] = Color.rgb(red,green,blue);
        }

        bitmap.setPixels(pixels,0,width,0,0,width,height);
    }


    private int changeColor (int color, int[] LUT, int min, int max) {
        color = LUT[color];
        if (color<min) {
            color = min;
        } else if (color>max) {
            color = max;
        }
        return color;
    }



    /**
     * Egalisation d'histogramme pour une image en couleur
     * Utilisatin d'une SeekBar
     */
    private void histogram() {
        seekBar.setVisibility(View.VISIBLE);
        seekBar.setProgress(50);
        seekBar.setMax(100);
        seekBar.setThumb(getResources().getDrawable(R.drawable.ic_equalizer_black_24dp));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                histogram(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }


    private void histogram(int value) {

    }



    /**
     * Zoom par interpolation au plus proche voisin
     * @param zoom : facteur de zoom fixe
     */
    private void zoom(int zoom) {
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
     * Permet d'accéder à l'appareil photo et de prendre une photo
     * de la mettre ensuite dans le bitmap
     */
    /*static final int REQUEST_IMAGE_CAPTURE = 1;

    private void takeImageFromCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            bitmap = imageBitmap;
            ImageView imageView = (ImageView) findViewById(R.id.imageView);
            imageView.setImageBitmap(bitmap);
        }
    }
    */


    private void takePhoto() {
        PackageManager pm = this.getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            dispatchTakePictureIntent();
        } else {
            Toast.makeText(MainActivity.this, "Pas d acces à la camera", Toast.LENGTH_LONG).show();
        }
    }


    String mCurrentPhotoPath;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    static final int REQUEST_TAKE_PHOTO = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(MainActivity.this, "Erreur au moment de creer le fichier", Toast.LENGTH_LONG).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.example.android.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }



    /**
     * Permet de changer la luminosite
     * Utilisation d'une seekBar
     */
    float pixelsHSV[];
    private void brightness() {
        //configurer la seekBar
        seekBar.setVisibility(View.VISIBLE);
        seekBar.setProgress(50);
        seekBar.setMax(100);
        seekBar.setThumb(getResources().getDrawable(R.drawable.ic_brightness_low_black_24dp));

        //avoir le tableau de pixels de l'image originale
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int[] pixels = new int[height * width];
        pixelsHSV = new float[height * width];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        float[] hsv = new float[3];

        for (int i = 0; i < pixels.length; i++) {
            Color.colorToHSV(pixels[i], hsv);
            pixelsHSV[i] = hsv[2];
        }


        //gerer les actions de la seekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                brightness(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    /**
     * Algo pour changer la luminosite
     * @param value
     */
    private void brightness(int value) {
        float valueF = value-50;
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int[] pixels = new int[height * width];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        float[] hsv = new float[3];

        for (int i = 0; i < pixels.length; i++) {
            Color.colorToHSV(pixels[i], hsv);
            hsv[2] = pixelsHSV[i] + valueF / 100;
            pixels[i] = Color.HSVToColor(hsv);
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);

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


    /*private void contrast(int value) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int[] pixels = new int[height*width];
        bitmap.getPixels(pixels,0,width,0,0,width,height);

        int[] hr = new int[256];
        int[] hg = new int[256];
        int[] hb = new int[256];
        for(int i=0 ; i<256 ; i++) {
            hr[i] = 0;
            hg[i] = 0;
            hb[i] = 0;
        }

        for (int i=0 ; i<pixels.length ; i++) {
            int red = Color.red(pixels[i]);
            hr[red]++;

            int green = Color.green(pixels[i]);
            hg[green]++;

            int blue = Color.blue((pixels[i]));
            hb[blue]++;
        }

        int[] cr = new int[256];
        int sumr = 0;
        int[] cg = new int[256];
        int sumg = 0;
        int[] cb = new int[256];
        int sumb = 0;
        for(int i=0 ; i<256 ; i++) {
            sumr = sumr + hr[i];
            cr[i] = sumr;
            sumg = sumg + hg[i];
            cg[i] = sumg;
            sumb = sumb + hb[i];
            cb[i] = sumb;
        }

        for (int i=0 ; i<pixels.length ; i++) {
            int red = Color.red(pixels[i]);
            red = (cr[red] * 255) / pixels.length;
            int green = Color.green(pixels[i]);
            green = (cr[green] * 255) / pixels.length;
            int blue = Color.blue(pixels[i]);
            blue = (cr[blue] * 255) / pixels.length;
            pixels[i] = Color.rgb(red,green,blue);
        }

        bitmap.setPixels(pixels,0,width,0,0,width,height);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);
    }*/


    /**
     * Ne garde qu'une seule couleur de l'image, ici le vert
     * A essayer sur une image avec des couleurs très tranchés (par ex : smarties)
     */
    /*public void dominate() {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int[] pixels = new int[height*width];
        bitmap.getPixels(pixels,0,width,0,0,width,height);

        for (int i=0 ; i<pixels.length ; i++) {
            int color = pixels[i];
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);

            if (green<blue+red) {
                red = (3 * red + 59 * green + 11 * blue) / 100;
                color = Color.rgb(red, red, red);
                pixels[i] = color;
            }
        }

        bitmap.setPixels(pixels,0,width,0,0,width,height);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(bitmap);
    }*/

}
