package com.example.katell.myapplication;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ClipDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

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

    /**
     * Tableau de pixels qui correspond à la precedente bitmap
     * permet d'enlever le filtre precedent
     */
    private int[] bitmapInit;

    /**
     * la seekBar qui est utilisé dans différents cas :
     * pour la luminosité ou le contraste
     */
    private SeekBar seekBar;

    /**
     * Le textView qui est utilisé dans le cas :
     * où on veut pouvoir afficher une information qui permet de remplir l'EditText ensuite
     */
    private TextView textView;

    /**
     * Utilisé pour entrer une donnée qui est utilisé ensuite dans les algos :
     * la tolérance dans l'algo d'isolation d'une couleur
     */
    private EditText editText;

    /**
     * Bouton qui est utilisé pour valider un editText suite à une saisi
     */
    private Button button;

    /**
     * passe à true quand on veut teinter une image
     * permet d'utiliser le bouton 2 fois
     */
    boolean colorize = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Gerer le seekBar
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setVisibility(View.INVISIBLE);

        //Gerer le textView
        textView = (TextView) findViewById(R.id.textView);
        textView.setVisibility(View.INVISIBLE);

        //Gerer le editText
        editText = (EditText) findViewById(R.id.editText);
        editText.setVisibility(View.INVISIBLE);

        //Gerer le bouton
        button = (Button) findViewById(R.id.button);
        button.setVisibility(View.INVISIBLE);

        //imageView : initialisation
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        BitmapFactory.Options option = new BitmapFactory.Options();
        bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.hamster,option); //hamster : nom de l'image affiché au lancement de l'application
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        imageView.setImageBitmap(bitmap);

        recover();
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
     * Redirection de l'action quand un item du menu a été pressé
     * @param item
     */
    public void onOptionsItem(MenuItem item) {
        //Permet de rendre touts les éléments invisibles suite à une utilisation
        seekBar.setVisibility(View.INVISIBLE);
        seekBar.setProgress(0);
        textView.setVisibility(View.INVISIBLE);
        editText.setVisibility(View.INVISIBLE);
        editText.setText("");
        button.setVisibility(View.INVISIBLE);
        colorize = false;

        switch (item.getItemId()) {
            //pour griser
            case R.id.gray:
                toGray();
                break;

            //pour appliquer une teinte aléatoire
            case R.id.random:
                int hue = (int) (Math.random() * 360);
                toColorize(hue);
                break;

            //pour appliquer une teinte rouge
            case R.id.colorize:
                colorize = true;
                colorize();
                break;

            //pour appliquer une teinte sepia
            case R.id.sepia:
                sepia();
                break;

            //permet de ne garder qu'une seule couleur
            case R.id.isolateColor:
                isolateColor();
                break;

            //permet de regler la luminosite
            case R.id.brightness:
                brightness();
                break;

            //pour contraster
            case R.id.contrast:
                contrast();
                break;

            //pour l'égalisation d'histogramme
            case R.id.histogram:
                histogram();
                break;

            //pour la surexposition
            case R.id.surexposition:
                overexposure();
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
                recover();
                break;

            //permet de mettre une image (smarties)
            case R.id.smarties:
                imageView = (ImageView) findViewById(R.id.imageView);
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.smarty);
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
     * Permet de rediriger l'action du bouton
     */
    public void onOptionsButton(View view) {
        if (colorize) {
            colorizeButton();
        } else {
            isolateColorButton();
        }
    }



    /**
     * Permet de griser l'image
     */
    private void toGray() {
        bitmap = Algorithm.toGray(bitmap);
    }



    /**
     ********* Teinte **********
     * Ici la méthode permet d'afficher les différents éléments utiles à l'utilisateur
     */
    private void colorize() {
        seekBar.setVisibility(View.VISIBLE);
        seekBar.setMax(360);
        seekBar.setThumb(getResources().getDrawable(R.drawable.ic_color_lens_black_24dp));
        button.setVisibility(View.VISIBLE);

        //gerer les actions de la seekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float[] hsv = new float[3];
                hsv[0] = progress;
                hsv[1] = 1;
                hsv[2] = 1;
                int color = Color.HSVToColor(hsv);
                seekBar.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }


    /**
     * Méthode appelé quand le bouton est pressé
     * Elle permet de récuperer la valeur de la seekBar
     */
    public void colorizeButton() {
        int color = seekBar.getProgress();
        toColorize(color);
    }

    /**
     * colorie l'image selon une teinte donnée en paramètre
     * @param hue la teinte à appliquer à l'image
     */
    private void toColorize(int hue) {
        bitmap = Algorithm.toColorize(bitmap,hue);
    }



    /**
     * l'image passe avec une teinte sepia
     * l'algo est basé sur celui de la teinte
     */
    private void sepia() {
        bitmap = Algorithm.sepia(bitmap);
    }



    /**
     * ****** ISOLATION DE COULEUR********
     * Ici la méthode permet d'afficher les différents éléments utiles à l'utilisateur
     */
    private void isolateColor() {
        seekBar.setVisibility(View.VISIBLE);
        seekBar.setMax(360);
        seekBar.setThumb(getResources().getDrawable(R.drawable.ic_color_lens_black_24dp));
        editText.setVisibility(View.VISIBLE);
        editText.setText("30");
        textView.setVisibility(View.VISIBLE);
        textView.setText(R.string.message_Tolerance);
        button.setVisibility(View.VISIBLE);


        //gerer les actions de la seekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float[] hsv = new float[3];
                hsv[0] = progress;
                hsv[1] = 1;
                hsv[2] = 1;
                int color = Color.HSVToColor(hsv);
                seekBar.getProgressDrawable().setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }


    /**
     * Méthode appelé quand le bouton est pressé
     * Elle permet de récupérer la tolérance qui l'utilisateur a noté dans l'EditText
     * et de changer la bitmap
     */
    public void isolateColorButton() {
        try {
            int interval = Integer.parseInt(editText.getText().toString());
            int color = seekBar.getProgress();
            bitmap = Algorithm.isolateColor(bitmap,color,interval);
        } catch (Exception e) {
            Toast.makeText(MainActivity.this, "Veuillez rentrer un nombre", Toast.LENGTH_LONG).show();
            editText.setText("");
        }
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



        //gerer les actions de la seekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            float[] pixelsHSV = Algorithm.getBitmapBrightness(bitmap);

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    bitmap = Algorithm.brightness(bitmap, progress, pixelsHSV);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
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



        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            //recuperer le tableau de pixels de l'image initial
            int[] pixelsContrast = Algorithm.getBitmapRGB(bitmap);

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int min = 122 - progress;
                    int max = 122 + progress;
                    if (progress < 61) {
                        bitmap = Algorithm.contrastLower(min, max, bitmap, pixelsContrast);
                    } else if (progress == 61) {
                        bitmap = Algorithm.setBitmapRGB(bitmap, pixelsContrast);
                    } else {
                        bitmap = Algorithm.contrastIncrease(min, max, bitmap, pixelsContrast);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }



    /**
     * Egalisation d'histogramme pour une image en couleur
     * Utilisatin d'une SeekBar
     */
    private void histogram() {

        Toast.makeText(MainActivity.this, "En construction", Toast.LENGTH_LONG).show();

        /*seekBar.setVisibility(View.VISIBLE);
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
        });*/
    }


    private void histogram(int value) {

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

        //gerer les actions de la seekBar
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            //avoir le tableau de pixels de l'image originale
            int [] pixelsOver = Algorithm.getBitmapRGB(bitmap);

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                bitmap = Algorithm.overexposure(bitmap,progress,pixelsOver);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }



    /**
     * Zoom par interpolation au plus proche voisin
     * @param zoom : facteur de zoom fixe
     */
    private void zoom(int zoom) {
        bitmap = Algorithm.zoom(bitmap,zoom);
    }



    /**
     * Début de l'accès à une image à partir de la caméra
     */
    public static final int CAMERA_PERMISSION = 0;
    String mCurrentPhotoPath;
    static final int REQUEST_TAKE_PHOTO = 1;

    /**
     * Permet de gerer le permissions car l'API 23 ne le fait pas automatiquement
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 5) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Now user should be able to use camera
            } else {
                // Your app will not have this permission. Turn off all functions
                // that require this permission or it will force close like your
                // original question
            }
        }
    }


    /**
     * Permet de gerer les permissions et aussi l'existance ou non de la caméra.
     */
    private void takePhoto() {
        PackageManager pm = this.getPackageManager();
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION);
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
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }


    /**
     * Permet de prendre une photo
     * qui sera mis dans mCurrentPhotoPath
     */
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

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW/targetW, photoH/targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        imageView.setImageBitmap(bitmap);

        recover();
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
    }







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
}