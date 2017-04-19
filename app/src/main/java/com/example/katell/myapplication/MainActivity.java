package com.example.katell.myapplication;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
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

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

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

    /**
     * Sert dans la méthode qui permet de charger une image de la galerie
     */
    private static int RESULT_LOAD_IMG = 1;

    /**
     * Permet de différencier si on veut charger une image de la galerie ou de l'appareil photo
     * Si vrai, c'est de la galerie
     * Si faux, c'est la l'appareil photo
     */
    boolean galery;




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

        //Permet de définir un taille pour l'imageView
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;
        imageView.getLayoutParams().height = height;
        imageView.getLayoutParams().width = width;

        //Permet de mettre une bitmap dans l'imageView
        BitmapFactory.Options option = new BitmapFactory.Options();
        bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.hamster,option);
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
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
        int color = Color.parseColor("#ffcc5c"); //la couleur initiale
        seekBar.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
        seekBar2.setVisibility(View.INVISIBLE);
        seekBar2.setProgress(0);
        ZoomInZoomOut zoom = new ZoomInZoomOut();
        imageView.setOnTouchListener(zoom);


        switch (item.getItemId()) {
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

            //permet de mettre une image (image peu contrastée)
            case R.id.notContrast:
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pas_contraste);
                bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                imageView.setImageBitmap(bitmap);
                recover();
                break;

            //permet de prendre une photo
            case R.id.photo:
                takePhoto();
                break;

            //charger une image de la galerie
            case R.id.gallery:
                LoadGallery();
                break;

            //Sauvegarder une image
            case R.id.save:
                SaveImage(bitmap);
                break;

            //Permet d'afficher l'image initiale
            case R.id.init:
                init();
                break;

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

            //pour la surexposition
            case R.id.overexposure:
                overexposure();
                break;

            //pour le seuillage
            case R.id.thresholding:
                thresholding();
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

            //convolution gaussienne
            case R.id.gaussien:
                gaussien();
                break;

            //convolution moyenneur
            case R.id.moyenneur:
                moyenneur();
                break;

            //convolution moyenneur2
            case R.id.moyenneur2:
                int[][] matrix = Algorithm.moyenneur(5);
                bitmap = Algorithm.convolute(bitmap,matrix,5);
                break;

            //convolution sobel
            case R.id.sobel:
                bitmap = Algorithm.toGray(bitmap);
                int[][] sobel_1 = new int[][]{{-1, 0, 1}, {-2, 0, 2}, {-1, 0, 1}}; // 0
                int[][] sobel_2 = new int[][]{{-1, -2, -1}, {0, 0, 0}, {1, 2, 1}}; // 0
                float[] sobel1 = Algorithm.convolute2(bitmap, sobel_1, 3);
                float[] sobel2 = Algorithm.convolute2(bitmap, sobel_2, 3);
                bitmap = Algorithm.sobel(bitmap, sobel1,sobel2);
                bitmap = Algorithm.histogram(bitmap);
                break;

            //convolution laplacien
            case R.id.laplacien:
                bitmap = Algorithm.toGray(bitmap);
                int[][] laplacien = new int[][]{{0, 1, 0}, {1, -4, 1}, {0, 1, 0}}; // 1
                float [] laplacien_res = Algorithm.convolute2(bitmap, laplacien,3);
                bitmap = Algorithm.laplacien(bitmap, laplacien_res);
                bitmap = Algorithm.histogram(bitmap);
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
        seekBar2.setThumb(getResources().getDrawable(R.drawable.ic_compare_arrows_black_24dp));

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
        final int[] pixels = Algorithm.getBitmapRGB(bitmap);

        imageView.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event){
                int x = (int)event.getX();
                int y = (int)event.getY();
                int pixel = bitmap.getPixel(x,y);
                float[] hsv = new float[3];
                Algorithm.colorToHSV(pixel,hsv);
                bitmap = Algorithm.isolateColor(bitmap,(int) hsv[0],30,pixels);
                imageView.setImageBitmap(bitmap);
                return false;
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
        int[] pixels = Algorithm.getBitmapRGB(bitmap);

        //gerer les actions de la seekBar
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListenerWithArray(pixels, pixelsHSV){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bitmap = Algorithm.brightness(bitmap, progress, getPixels(), getBrightness()); //getBrightness est dans la classe OnSeekBarChangeListenerWithArray
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
     * Convolution moyenneur
     * Permet de gérer les seekBars
     * seekBar : sert à choisir la taille de la matrice à appliquer
     * seekbar2 : sert à choisir le nombre de fois que l'algorithme est appliqué
     */
    private void moyenneur() {
        //configurer les seekBars
        seekBar.setVisibility(View.VISIBLE);
        seekBar.setMax(3);
        seekBar.setProgress(1);
        seekBar.setThumb(getResources().getDrawable(R.drawable.ic_filter_list_black_24dp));
        seekBar2.setVisibility(View.VISIBLE);
        seekBar2.setMax(5);
        seekBar2.setThumb(getResources().getDrawable(R.drawable.ic_repeat_black_24dp));

        //gerer les actions de la seekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int size = 2*(progress+1)+1;
                int[][] matrix = Algorithm.moyenneur(size);
                for (int i=0 ; i<seekBar2.getProgress() ; i++) {
                    bitmap = Algorithm.convolute(bitmap, matrix, size);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        //gerer les actions de la seekBar2
        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int size = 2*(seekBar.getProgress()+1)+1;
                int[][] matrix = Algorithm.moyenneur(size);
                for (int i=0 ; i<progress ; i++) {
                   bitmap = Algorithm.convolute(bitmap, matrix, size);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }


    /**
     * Convolution : gaussien
     * permet de gérer la seekBar
     */
    private void gaussien() {
        //configurer les seekBars
        seekBar.setVisibility(View.VISIBLE);
        seekBar.setMax(5);
        seekBar.setThumb(getResources().getDrawable(R.drawable.ic_repeat_black_24dp));

        //gerer les actions de la seekBar
        seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListenerWithArray(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                for (int i=0 ; i<progress ; i++) {
                    bitmap = Algorithm.convolute(bitmap, getGaussien(), 5);
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
        galery = false;
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
        if (!galery) {
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
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decoder le fichier pour que la bitmap remplisse l'ImageView
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
            bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
            imageView.setImageBitmap(bitmap);

            recover();
        } else {
            if (resultCode == RESULT_OK) {
                if (requestCode == RESULT_LOAD_IMG) {
                    Uri imageUri = data.getData();
                    InputStream inputStream;

                    try {
                        inputStream = getContentResolver().openInputStream(imageUri);

                        Bitmap bmp = BitmapFactory.decodeStream(inputStream);

                        ImageView imageView = (ImageView) findViewById(R.id.imageView);
                        bitmap = bmp;
                        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                        imageView.setImageBitmap(bmp);

                        recover();

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Unable to open image", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }
    }



    /**
     * Méthode qui permet de charger une image depuis la galerie
     */
    public void LoadGallery() {
        galery = true;
        Intent galleryIntent = new Intent(Intent.ACTION_PICK); //MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        File pictureDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        String pictureDirectoryPath = pictureDirectory.getPath();

        Uri data = Uri.parse(pictureDirectoryPath);
        galleryIntent.setDataAndType(data, "image/*");
        startActivityForResult(galleryIntent, RESULT_LOAD_IMG);
    }



    /**
     * Permet de sauvegarder la bitmap modifiée dans le téléphone.
     * Dans un dossier Image_PhotArt qui l'on trouve dans le gestionnaire de fichiers
     * @param bmp
     * Inspiré de StackOverFlow
     */
    public void SaveImage(Bitmap bmp) {
        verifyStoragePermissions(this);
        OutputStream fOut = null;
        Uri outputFileUri;
        try {
            File root = new File(Environment.getExternalStorageDirectory() + File.separator + "Images_PhotArt" + File.separator);
            root.mkdirs();
            Random generator = new Random();
            int n = 10000;
            n = generator.nextInt(n);
            String fname = "Image-" + n + ".jpg";
            File sdImageMainDirectory = new File(root, fname);
            outputFileUri = Uri.fromFile(sdImageMainDirectory);
            fOut = new FileOutputStream(sdImageMainDirectory);

            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri contentUri = Uri.fromFile(root);
            mediaScanIntent.setData(contentUri);
            this.sendBroadcast(mediaScanIntent);

        } catch (Exception e) {
            Toast.makeText(this, "Error occured", Toast.LENGTH_SHORT).show();
        }
        try {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
        }
        Toast.makeText(this, "Image saved in Images_PhotArt", Toast.LENGTH_SHORT).show();
    }



    /**
     * Ce qui permet de gérer les permissions pour la sauvagrde d'une image
     * Car à partir de l'API23, les permissions ne sont pu géré
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        final int REQUEST_EXTERNAL_STORAGE = 1;

        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        }
    }

}