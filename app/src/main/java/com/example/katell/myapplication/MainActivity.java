package com.example.katell.myapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.util.Pools;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.example.katell.myapplication.SeekBar.OnSeekBarChangeListenerWithArray;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    /**
     * Attribut correspondant au bitmap contenue dans l'ImageView
     */
    private Bitmap bitmap;

    /**
     * L'imageView contenant la bitmap
     */
    private ImageView imageView;

    /**
     * Tableau de pixels qui correspond à la bitmap initiale
     * permet d'enlever tous les filtres
     */
    public int[] bitmapInit;

    /**
     * La seekBar qui est utilisé dans différents cas :
     * pour la luminosité ou le contraste par exemple
     */
    private SeekBar seekBar;

    /**
     * une deuxième seekBar qui est utilisé :
     * pour isolation de couleur
     */
    private SeekBar seekBar2;



    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Gerer les seekBar
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setVisibility(View.INVISIBLE);
        seekBar2 = (SeekBar) findViewById(R.id.seekBar2);
        seekBar2.setVisibility(View.INVISIBLE);

        //imageView : initialisation
        imageView = (ImageView) findViewById(R.id.imageView);

        /*DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        imageView.getLayoutParams().height = height;
        imageView.getLayoutParams().width = width;*/


        BitmapFactory.Options option = new BitmapFactory.Options();
        bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.icon,option);
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        //float scaleW = ((float) width)/bitmap.getWidth();
        //float scaleH = ((float) width * bitmap.getHeight()) /bitmap.getWidth();
        //Matrix matrix = new Matrix();
        //matrix.postScale(scaleW, scaleH);
        //bitmap = Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix, true);


        imageView.setImageBitmap(bitmap);


        //Permet de gérer le zoom
        ZoomInZoomOut zoom = new ZoomInZoomOut();
        imageView.setOnTouchListener(zoom);

        //Permet de récupérer les pixels de l'image pour ensuite pouvoir réinitialiser si besoin
        recover();
    }



    /**
     * Permet de relier le fichier menu.xml pour avoir un menu sur l'application
     * On transforme le xml vers un format plus intéressant (ici une activité)
     * @param menu la menu que l'on veut avoir
     * @return true (car tout ce passe bien)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }



    /**
     * Redirection de l'action quand un item du menu a été pressé
     * @param item l'item qui a été pressé
     */
    public void onOptionsItem(MenuItem item) {
        //Permet de réinitialiser tous les éléments suite à une utilisation
        seekBar.setVisibility(View.INVISIBLE);
        seekBar.setProgress(0);
        seekBar2.setVisibility(View.INVISIBLE);
        seekBar2.setProgress(0);
        ZoomInZoomOut zoom = new ZoomInZoomOut();
        imageView.setOnTouchListener(zoom);


        switch (item.getItemId()) {
            //pour griser
            case R.id.gray:
                bitmap = Algorithm.toGray(bitmap);
                break;

            //pour appliquer une teinte aléatoire
            case R.id.random:
                int hue = (int) (Math.random() * 360);
                bitmap = Algorithm.toColorize(bitmap, hue);
                break;

            //pour appliquer une teinte rouge
            case R.id.colorize:
                colorize();
                break;

            //pour appliquer une teinte sepia
            case R.id.sepia:
                bitmap = Algorithm.sepia(bitmap);
                break;

            //pour vieillir l'image
            case R.id.old:
                old();
                break;

            //permet de ne garder qu'une seule couleur avec une SeekBar
            case R.id.isolateColorBar:
                isolateColorBar();
                break;

            //permet de ne garder qu'une seule couleur avec le toucher
            case R.id.isolateColorTouch:
                isolateColorTouch();
                break;

            //permet de regler la luminosite
            case R.id.brightness:
                brightness();
                break;

            //pour contraster
            case R.id.contrast:
                contrast();
                break;

            //pour l'égalisation d'histogramme lent
            case R.id.histogram:
                bitmap = Algorithm.histogram(bitmap);
                break;

            //pour la surexposition
            case R.id.overexposure:
                overexposure();
                break;

            //pour le seuillage
            case R.id.thresholding:
                thresholding();
                break;

            //permet de mettre une image (hamster)
            case R.id.hamster:
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hamster);
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                imageView.setImageBitmap(bitmap);
                recover();
                break;

            //permet de mettre une image (smarties)
            case R.id.smarties:
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.smarty);
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                imageView.setImageBitmap(bitmap);
                recover();
                break;

            //permet de mettre une image (Lenna)
            case R.id.lenna:
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pas_contraste);
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                imageView.setImageBitmap(bitmap);
                recover();
                break;

            //permet de prendre une photo
            case R.id.photo:
                takePhoto();
                break;

            //Permet d'afficher l'image initiale
            case R.id.init:
                init();
                break;
        }
    }




    /**
     * Applique une teinte à l'image
     * Ici la méthode permet d'afficher les différents éléments utiles à l'utilisateur
     * Et de lancer les algorithmes utiles
     */
    private void colorize() {
        //configurer la seekBar
        seekBar.setVisibility(View.VISIBLE);
        seekBar.setMax(359);
        seekBar.setThumb(getResources().getDrawable(R.drawable.ic_color_lens_black_24dp));

        //gerer les actions de la seekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float[] hsv = new float[3];
                    hsv[0] = progress;
                    hsv[1] = 1;
                    hsv[2] = 1;
                    int color = Algorithm.HSVToColor(hsv);
                    //permet d'avoir la seekBar qui change couleur en fonction de progress
                    seekBar.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
                    bitmap = Algorithm.toColorize(bitmap,progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }



    /**
     * Pour vieillir une image
     * Teinte l'image en sepia
     * Lui ajoute du bruit
     * Rajoute une tâche de café en haut à gauche de l'image
     * Abime les bords de l'image
     */
    private void old() {
        //images pour les algos
        Bitmap bmStain = BitmapFactory.decodeResource(getResources(), R.drawable.tache_cafe);
        Bitmap bmNoise = BitmapFactory.decodeResource(getResources(), R.drawable.bruit);

        bitmap = Algorithm.sepia(bitmap);
        bitmap = Algorithm.fusionNoise(bitmap,bmNoise);
        bitmap = Algorithm.fusion(bitmap,bmStain,0,0,2);
        bitmap = Algorithm.crop(bitmap);
    }



    /**
     * Isolation de couleur avec des SeekBars
     * Ici la méthode permet d'afficher les différents éléments utiles à l'utilisateur
     * seekBar : gère la teinte à garder
     * seekBar2 : gère la tolérance
     */
    private void isolateColorBar() {
        seekBar.setVisibility(View.VISIBLE);
        seekBar.setMax(360);
        seekBar.setThumb(getResources().getDrawable(R.drawable.ic_color_lens_black_24dp));
        seekBar2.setVisibility(View.VISIBLE);
        seekBar2.setMax(100);
        seekBar2.setProgress(30);
        seekBar.setThumb(getResources().getDrawable(R.drawable.ic_compare_arrows_black_24dp));

        int[] pixels = Algorithm.getBitmapRGB(bitmap);

        //gerer les actions de la seekBar
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListenerWithArray(pixels){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    float[] hsv = new float[3];
                    hsv[0] = progress;
                    hsv[1] = 1;
                    hsv[2] = 1;
                    int color = Color.HSVToColor(hsv);
                    seekBar.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
                    bitmap = Algorithm.isolateColor(bitmap,progress,seekBar2.getProgress(),getPixels());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        //gerer les actions de la seekBar2
        seekBar2.setOnSeekBarChangeListener(new OnSeekBarChangeListenerWithArray(pixels){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    bitmap = Algorithm.isolateColor(bitmap,seekBar.getProgress(),progress,getPixels());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }



    /**
     * Permet de ne garder que la couleur touchée
     */
    private void isolateColorTouch() {
        seekBar.setVisibility(View.VISIBLE);
        seekBar.setMax(100);
        seekBar.setProgress(30);
        seekBar.setThumb(getResources().getDrawable(R.drawable.ic_compare_arrows_black_24dp));

        int[] pixels = Algorithm.getBitmapRGB(bitmap);

        //gerer les actions de la seekBar
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListenerWithArray(pixels){
            @Override
            public void onProgressChanged(final SeekBar seekBar, int progress, boolean fromUser) {
                imageView.setOnTouchListener(new View.OnTouchListener(){
                    @Override
                    public boolean onTouch(View v, MotionEvent event){
                        int x = (int)event.getX();
                        int y = (int)event.getY();
                        int pixel = bitmap.getPixel(x,y);
                        float[] hsv = new float[3];
                        Algorithm.colorToHSV(pixel,hsv);
                        bitmap = Algorithm.isolateColor(bitmap,(int) hsv[0],seekBar.getProgress(),getPixels());
                        imageView.setImageBitmap(bitmap);
                        return false;
                    }
                });
            }
        });
    }



    /**
     * Gère la luminosité de l'image
     * Permet de gerer la seekBar et les différents algos à faire
     */
    private void brightness() {
        //configurer la seekBar
        seekBar.setVisibility(View.VISIBLE);
        seekBar.setProgress(50);
        seekBar.setMax(100);
        seekBar.setThumb(getResources().getDrawable(R.drawable.ic_brightness_low_black_24dp));

        //avoir le tableau de pixels de l'image originale
        float[] pixelsHSV = Algorithm.getBitmapBrightness(bitmap);

        //gerer les actions de la seekBar
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListenerWithArray(pixelsHSV){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bitmap = Algorithm.brightness(bitmap, progress, getBrightness()); //getBrightness est dans la classe OnSeekBarChangeListenerWithArray
            }
        });
    }



    /**
     * Contraste d'une image en couleur
     * Par extension de dynamique
     * utilise avec une seekBar
     */
    private void contrast() {
        //gerer la seekBar
        seekBar.setVisibility(View.VISIBLE);
        seekBar.setProgress(61);
        seekBar.setMax(122);
        seekBar.setThumb(getResources().getDrawable(R.drawable.ic_tonality_black_24dp));

        //recuperer le tableau de pixels de l'image initial
        int[] pixelsContrast = Algorithm.getBitmapRGB(bitmap);

        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListenerWithArray(pixelsContrast){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int min = 122 - progress;
                    int max = 122 + progress;
                    if (progress < 61) {
                        bitmap = Algorithm.contrastLower(min, max, bitmap, getPixels());
                    } else if (progress == 61) {
                        bitmap = Algorithm.setBitmapRGB(bitmap, getPixels());
                    } else {
                        bitmap = Algorithm.contrastIncrease(min, max, bitmap, getPixels());
                    }
                }
            }
        });
    }



    /**
     * Surexposition
     * Permet de gerer la seekBar de lancer les algos nécessaire
     */
    private void overexposure() {
        //configurer la seekBar
        seekBar.setVisibility(View.VISIBLE);
        seekBar.setMax(50);
        seekBar.setThumb(getResources().getDrawable(R.drawable.ic_flare_black_24dp));

        //avoir le tableau de pixels de l'image originale
        int[] pixelsOver = Algorithm.getBitmapRGB(bitmap);

        //gerer les actions de la seekBar
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListenerWithArray(pixelsOver){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    bitmap = Algorithm.overexposure(bitmap, progress, getPixels());
                }
            }
        });
    }



    /**
     * Seuillage
     * Permet de gerer la seekBar de lancer l'algo nécessaire
     */
    private void thresholding() {
        //configurer la seekBar
        seekBar.setVisibility(View.VISIBLE);
        seekBar.setMax(255);
        seekBar.setProgress(127);
        seekBar.setThumb(getResources().getDrawable(R.drawable.ic_filter_b_and_w_black_24dp));
        bitmap = Algorithm.toGray(bitmap);

        //avoir le tableau de pixels de l'image original en nuances de gris
        int pixels[] = Algorithm.getBitmapRGB(bitmap);

        //pour la première fois
        bitmap = Algorithm.thresholding(bitmap,127,pixels);

        //gerer les actions de la seekBar
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListenerWithArray(pixels){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    bitmap = Algorithm.thresholding(bitmap, progress, getPixels());
                    imageView.setImageBitmap(bitmap);
                }
            }
        });
    }



    /**
     * Permet de récupérer l'image initiale
     */
    private void recover() {
        bitmapInit = Algorithm.getBitmapRGB(bitmap);
    }



    /**
     * Permet de réinitialiser l'image
     */
    private void init() {
        bitmap = Algorithm.setBitmapRGB(bitmap,bitmapInit);
        imageView.setImageBitmap(bitmap);
    }



    /**
     ********* ACCEDER A LA CAMERA *********
     * Inspiré grandement de Android Developers
     */
    public static final int CAMERA_PERMISSION = 0;
    String mCurrentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;


    /**
     * Permet de gerer les permissions car l'API 23 ne le fait pas automatiquement
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    /**
     * Permet de gerer les permissions et l'existance ou non de la caméra.
     */
    private void takePhoto() {
        PackageManager pm = this.getPackageManager();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        }

        if (pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            dispatchTakePictureIntent();
        } else {
            Toast.makeText(MainActivity.this, "Cet appareil n'a pas de camera.", Toast.LENGTH_LONG).show();
        }

    }


    /**
     * Permet de creer un fichier pour pouvoir stocker l'image ensuite
     * @return file
     * @throws IOException
     */
    private File createImageFile() throws IOException {
        //Creer un nom de fichier
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Sauvegarder le fichier : chemin à utiliser
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    /**
     * Permet de prendre une photo
     * qui sera mis dans mCurrentPhotoPath
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;

            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(MainActivity.this, "Erreur au moment de creer le fichier", Toast.LENGTH_LONG).show();
            }

            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getApplicationContext(), "com.example.katell.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                try {
                    startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "les droits ne sont pas accordés pour accéder à la camera.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    /**
     * Permet de prendre la photo contenue dans mCurrentPhotoPath
     * pour ensuite l'afficher
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        //Avoir les dimensions de bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        //Determine de combien réduire l'image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decoder le fichier pour que la bitmap remplisse l'ImageView
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        imageView.setImageBitmap(bitmap);

        recover();
    }
}