package com.example.toper.myapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static int RESULT_LOAD_IMG = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Afficher une image
        ImageView imageView = (ImageView) findViewById(R.id.imageView1);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.lenna, options);
        imageView.setImageBitmap(bitmap);
    }

    protected void onClick(View view) { //methode de gestion des boutons

        ImageView imageView = (ImageView) findViewById(R.id.imageView1);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        options.inScaled = false;
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        //Matrix mat = new Matrix();
        int[][] gaussien = new int[][] {{1,2,1},{2,4,2},{1,2,1}}; // 16
        int[][] moyenneur = new int[][]{{1,1,1},{1,1,1},{1,1,1}}; // 9
        int[][] sobel = new int[][]{{-1,0,1},{-2,0,2},{-1,0,1}}; // 1
        int[][]laplacien = new int[][]{{0,1,0},{1,-4,1},{0,1,0}}; // 1
        //Matrix mat = new Matrix(test);
        //float [] mat = new float[MATRIX_SIZE*MATRIX_SIZE];

        /*for (int i=0; i < MATRIX_SIZE; i++) {
            for (int j=0; j<MATRIX_SIZE;j++) {
                mat[i+j] = Gaussien[i][j];
            }
        }*/

        switch (view.getId()) {
            case R.id.teinte:
                colorize(bitmap);
                break;

            case R.id.gris:
                gray(bitmap);
                break;

            case R.id.reset:

                reset();
                break;

            case R.id.contraste:
                contrast(bitmap);
                break;

            case R.id.egalisation:
                egalisation(bitmap);
                break;

            case R.id.ZoomF2:
                ZoomF2(bitmap, 5);
                break;

            case R.id.ZoomB:
                ZoomB(bitmap, 2);
                break;

            case R.id.surexposition:
                surexposition(3, bitmap);
                break;

            case R.id.couleur:
                teinte(90, 150, bitmap);
                break;

            case R.id.resetI:
                resetI();
                break;

            case R.id.gallery:
                LoadGallery(imageView);
                break;

            case R.id.save:
                SaveImage(bitmap);
                break;

            case R.id.convolute:
                convolute(bitmap,laplacien,1);
                break;
        }
    }

    public boolean onTouchEvent(MotionEvent e) { //zoom en appuyant sur l'ecran
        ImageView imageView = (ImageView) findViewById(R.id.imageView1);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        options.inScaled = false;
        BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
        Bitmap bitmap = drawable.getBitmap();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (bitmap != null) {
                    ZoomF2(bitmap, 2);
                }
                break;
        }
        return true;
    }


    public void egalisation(Bitmap bmp) {
        ImageView imageView = (ImageView) findViewById(R.id.imageView1);
        int height = bmp.getHeight();
        int width = bmp.getWidth();
        int[] hist = new int[256];
        int[] pixels = new int[height * width];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
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
        bmp.setPixels(pixelsN, 0, width, 0, 0, width, height);
        imageView.setImageBitmap(bmp);
    }

    public void ZoomF2(Bitmap bmp, int zoom) { // zoom par la methode d interpolarisation des plus proches voisins
        ImageView imageView = (ImageView) findViewById(R.id.imageView1);
        int height = bmp.getHeight();
        int width = bmp.getWidth();
        int heightN = zoom * height;
        int widthN = zoom * width;
        int[] pixels = new int[height * width];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap bitmap = Bitmap.createBitmap(widthN, heightN, Bitmap.Config.ARGB_8888);
        int[] pixelsN = new int[heightN * widthN];
        for (int i = 0; i < heightN * widthN; i++) {
            int x = (i % widthN) / zoom;
            int y = (i / widthN) / zoom;
            pixelsN[i] = pixels[x + y * width];
        }
        bitmap.setPixels(pixelsN, 0, widthN, 0, 0, widthN, heightN);
        bmp = bitmap;
        imageView.setImageBitmap(bmp);
    }

    public void ZoomB(Bitmap bmp, int zoom) { //zoom par la methode d interpolarisation bilineaire (fonctionne mal)
        ImageView imageView = (ImageView) findViewById(R.id.imageView1);
        int height = bmp.getHeight();
        int width = bmp.getWidth();
        int heightN = zoom * height;
        int widthN = zoom * width;
        float x_r = ((float) (width - 1)) / widthN;
        float y_r = ((float) (height - 1)) / heightN;
        int[] pixels = new int[height * width];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap bitmap = Bitmap.createBitmap(widthN, heightN, Bitmap.Config.ARGB_8888);
        int[] pixelsN = new int[heightN * widthN];
        int compteur = 0;
        for (int i = 0; i < heightN; i++) {
            for (int k = 0; k < widthN; k++) {
                int x = (int) (x_r * k);
                int y = (int) (y_r * i);
                float x_d = (x_r * k) - x;
                float y_d = (y_r * i) - x;
                int index = y * width + x;
                //float blue = Color.blue(pixels[index])*(1-width)*(1-height) + Color.blue(pixels[index+1])*(1-height) + Color.blue(pixels[index+width])*(1-width)+ Color.blue(pixels[index+1+width])*(1-height*width);
                //float blue = Color.blue(pixels[index])*(1-x_d)*(1-y_d) + Color.blue(pixels[index+1])*(x_d)*(1-y_d) + Color.blue(pixels[index+width])*(1-x_d)*(y_d) + Color.blue(pixels[index+width+1])*(x_d)*(y_d);
                //float green = Color.green(pixels[index])*(1-width)*(1-height) + Color.green(pixels[index+1])*(1-height) + Color.green(pixels[index+width])*(1-width)+ Color.green(pixels[index+1+width])*(1-height*width);
                //float green = Color.green(pixels[index])*(1-x_d)*(1-y_d) + Color.green(pixels[index+1])*(x_d)*(1-y_d) + Color.green(pixels[index+width])*(1-x_d)*(y_d) + Color.green(pixels[index+width+1])*(x_d)*(y_d);
                //float red = Color.red(pixels[index])*(1-width)*(1-height) + Color.red(pixels[index+1])*(1-height) + Color.red(pixels[index+width])*(1-width)+ Color.red(pixels[index+1+width])*(1-height*width);
                //float red = Color.red(pixels[index])*(1-x_d)*(1-y_d) + Color.red(pixels[index+1])*(x_d)*(1-y_d) + Color.red(pixels[index+width])*(1-x_d)*(y_d) + Color.red(pixels[index+width+1])*(x_d)*(y_d);

                //pixelsN[compteur] = Color.rgb((int)red,(int)green,(int)blue);
                //pixelsN[compteur] = Color.rgb((int)blue,(int)blue,(int)blue);
                pixelsN[compteur] = (int) (pixels[index] * (int) (1 - x_d) * (1 - y_d) + pixels[index + 1] * (x_d) * (1 - y_d) + pixels[index + width] * (1 - x_d) * (y_d) + pixels[index + width + 1] * (x_d) * (y_d));
                //int IP1 = bmp.getPixel(k,i) + (int)y_d*(bmp.getPixel(k,i+1)-bmp.getPixel(k,i));
                //int IP2 = bmp.getPixel(k+1,i)+(int)y_d*(bmp.getPixel(k+1,i+1)-bmp.getPixel(k+1,i));
                //pixelsN[compteur] = IP1 + (int)x_d*(IP2 - IP1);
                //bmp.setPixel(k, i, IP1 + (int)x_d*(IP2 - IP1));
                compteur++;
            }
        }
        bitmap.setPixels(pixelsN, 0, widthN, 0, 0, widthN, heightN);
        bmp = bitmap;
        imageView.setImageBitmap(bmp);
    }

    public void reset() {
        ImageView imageView = (ImageView) findViewById(R.id.imageView1);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inMutable = true;
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image, options);
        imageView.setImageBitmap(bitmap);
    }

    public void colorize(Bitmap bmp) {
        ImageView imageView = (ImageView) findViewById(R.id.imageView1);
        Random rand = new Random();
        int hue = rand.nextInt(360);
        int height = bmp.getHeight();
        int width = bmp.getWidth();
        for (int i = 0; i < height; i++) {
            for (int k = 0; k < width; k++) {
                int pixel = bmp.getPixel(k, i);
                int alpha = Color.alpha(pixel);
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);
                float[] hsv = new float[3];
                Color.RGBToHSV(red, green, blue, hsv);
                hsv[0] = hue;
                bmp.setPixel(k, i, Color.HSVToColor(alpha, hsv));
            }
        }
        imageView.setImageBitmap(bmp);
    }

    public void gray(Bitmap bitmap) {
        ImageView imageView = (ImageView) findViewById(R.id.imageView1);
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int[] pixels = new int[height * width];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < pixels.length; i++) {
            int color = pixels[i];
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            int gray;
            gray = (30 * red + 59 * green + 11 * blue) / 100;
            color = Color.rgb(gray, gray, gray);
            pixels[i] = color;
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        imageView.setImageBitmap(bitmap);
    }

    public void contrast(Bitmap bitmap) {
        ImageView imageView = (ImageView) findViewById(R.id.imageView1);
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int[] pixels = new int[height * width];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        int max = 0;
        int min = 255;
        int[] contrast = new int[height * width];
        for (int i = 0; i < pixels.length; i++) {
            int color = (30 * Color.red(pixels[i]) + 59 * Color.green(pixels[i]) + 11 * Color.blue(pixels[i])) / 100;
            contrast[i] = color;

            if (color > max) {
                max = color;
            }

            if (color < min) {
                min = color;
            }
        }
        int[] LUT = new int[256];
        for (int i = 0; i < 256; i++) {
            LUT[i] = (255 * (i - min) / (max - min));
        }
        for (int i = 0; i < pixels.length; i++) {
            int color = LUT[contrast[i]];
            pixels[i] = Color.rgb(color, color, color);
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        imageView.setImageBitmap(bitmap);

    }

    public void surexposition(int coeff, Bitmap bitmap) {
        ImageView imageView = (ImageView) findViewById(R.id.imageView1);
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int[] pixels = new int[height * width];
        int max = 255;
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < pixels.length; i++) {
            int red = Color.red(pixels[i]) * coeff;
            if (red > max) {
                red = max;
            }
            int green = Color.green(pixels[i]) * coeff;
            if (green > max) {
                green = max;
            }
            int blue = Color.blue(pixels[i]) * coeff;
            if (blue > max) {
                blue = max;
            }
            pixels[i] = Color.rgb(red, green, blue);
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        imageView.setImageBitmap(bitmap);

    }

    public void teinte(int min, int max, Bitmap bmp) {
        ImageView imageView = (ImageView) findViewById(R.id.imageView1);
        int height = bmp.getHeight();
        int width = bmp.getWidth();
        int[] pixels = new int[height * width];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < pixels.length; i++) {
            int red = Color.red(pixels[i]);
            int green = Color.green(pixels[i]);
            int blue = Color.blue(pixels[i]);
            float[] hsv = new float[3];
            Color.RGBToHSV(red, green, blue, hsv);
            if (hsv[0] < min && hsv[0] > max) {
                int gray = (30 * red + 59 * green + 11 * blue) / 100;
                pixels[i] = Color.rgb(gray, gray, gray);
            }
        }
        bmp.setPixels(pixels, 0, width, 0, 0, width, height);
        imageView.setImageBitmap(bmp);
    }

    public void LoadGallery(View view) {
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

                    ImageView imageView = (ImageView) findViewById(R.id.imageView1);
                    imageView.setImageBitmap(bmp);
                } catch (FileNotFoundException e){
                    e.printStackTrace();
                    Toast.makeText(this, "Unable to open image",Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void SaveImage(Bitmap bmp){

        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/saved_images");
        myDir.mkdirs();
        Random generator = new Random();
        int n = 10000;
        n = generator.nextInt(n);
        String fname = "Image-"+n+".jpg";
        File file = new File (myDir, fname);
        if (file.exists()) file.delete();
        try {
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG,90,out);
            out.flush();
            out.close();
        } catch (Exception e){
            e.printStackTrace();
        }
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED,Uri.parse("file://" + Environment.getExternalStorageDirectory())));
    }
      /*  File pictureFile = getOutputMediaFile();
        if (pictureFile == null) {
            Log.d(TAG, "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            bmp.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
    }

    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + getApplicationContext().getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    } */

    public void resetI() {
        ImageView imageView = (ImageView) findViewById(R.id.imageView1);
        imageView.setImageBitmap(null);
    }

    private static final int MATRIX_SIZE = 3;

    private static int cap(int color) {
        if (color > 255)
            return 255;
        else if (color < 0)
            return 0;
        else
            return color;
    }

    public void convolute(Bitmap bmp, int[][] kernel,int factor) {

        //float [] mxv = new float[MATRIX_SIZE * MATRIX_SIZE];
        //mat.getValues(mxv);
        ImageView imageView = (ImageView) findViewById(R.id.imageView1);
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixels = new int[width * height];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int[] pixelsN = pixels.clone();
        int max =255;
        for (int i = 1; i < width-1; i++) {
            for (int j = 1; j < height-1; j++) {
                //int red = Color.red(pixelsN[i + j * width]);
                //int green = Color.green(pixelsN[i + j * width]);
                //int blue = Color.blue(pixelsN[i + j * width]);
                //int gray = (30 * Color.red(pixels[i]) + 59 * Color.green(pixels[i]) + 11 * Color.blue(pixels[i])) / 100;

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
        bmp.setPixels(pixelsN, 0, width, 0, 0, width, height);
        imageView.setImageBitmap(bmp);

    }



        /*int [] rtPxs = pixels.clone();
        int r, g, b;
        int rSum, gSum, bSum;
        int idx;
        int pix;
        float mv;
        int w = width - MATRIX_SIZE + 1;
        int h = height - MATRIX_SIZE + 1;
        for(int x = 0; x < w ; x++) {
            for(int y = 0; y < h; y++) {
                idx = (x + 1) + (y + 1) * width;
                rSum = gSum = bSum = 0;
                for(int mx = 0; mx < MATRIX_SIZE; mx++) {
                    for(int my = 0; my < MATRIX_SIZE; my++) {
                        pix = pixels[(x + mx) + (y + my) * width];
                        mv = mxv[mx + my * MATRIX_SIZE];
                        rSum += (Color.red(pix) * mv);
                        gSum += (Color.green(pix) * mv);
                        bSum += (Color.blue(pix) * mv);
                    }
                }
                r = cap((int)(rSum / factor + offset));
                g = cap((int)(gSum / factor + offset));
                b = cap((int)(bSum / factor + offset));

                rtPxs[idx] = Color.argb(Color.alpha(pixels[idx]), r, g, b);
            }
        }
        bmp.setPixels(rtPxs, 0, width, 0, 0, width, height);
        imageView.setImageBitmap(bmp);*/


}