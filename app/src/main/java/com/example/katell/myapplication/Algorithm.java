package com.example.katell.myapplication;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Created by Katell on 13/03/2017.
 * Classe composé de tous les algos utiles à l'application
 */
class Algorithm {


    /**
     * Récupère le tableau des pixels en RGB
     * @param bitmap
     * @return int[height*width]
     */
    static int[] getBitmapRGB(Bitmap bitmap) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int[] pixels = new int[height*width];
        bitmap.getPixels(pixels,0,width,0,0,width,height);
        return pixels;
    }


    /**
     * renvoie le tableau composé de toutes les valeurs de hsv[2] d'un pixel
     * @param bitmap
     * @return
     */
    static float[] getBitmapBrightness(Bitmap bitmap) {
        int[] pixels = getBitmapRGB(bitmap);
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        float[] pixelsHSV = new float[height*width];
        float[] hsv = new float[3];

        for (int i = 0; i < pixels.length; i++) {
            Color.colorToHSV(pixels[i], hsv);
            pixelsHSV[i] = hsv[2];
        }

        return pixelsHSV;
    }



    /**
     * Modifie le tableau des pixels en RGB
     * @param bitmap
     * @param pixels
     * @return
     */
    static Bitmap setBitmapRGB(Bitmap bitmap, int[] pixels) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        bitmap.setPixels(pixels,0,width,0,0,width,height);
        return bitmap;
    }



    /**
     * Permet de griser l'image
     * @param bitmap
     * @return Bitmap
     */
    static Bitmap toGray(Bitmap bitmap) {
        int[] pixels = getBitmapRGB(bitmap);

        for (int i=0 ; i<pixels.length ; i++) {
            int color = pixels[i];
            int red = Color.red(color);
            int green = Color.green(color);
            int blue = Color.blue(color);
            int gray = (30*red + 59*green + 11*blue)/100;
            color = Color.rgb(gray,gray,gray);
            pixels[i] = color;
        }

        return setBitmapRGB(bitmap,pixels);
    }



    /**
     * Colorie l'image selon une teinte donnée en paramètre
     * @param bitmap
     * @param hue teinte passé en paramètres
     * @return Bitmap
     */
    static Bitmap toColorize(Bitmap bitmap,int hue) {

        int[] pixels = getBitmapRGB(bitmap);

        int color;
        float[] hsv = new float[3];

        for (int i=0 ; i<pixels.length ; i++) {
            color = pixels[i];
            Color.colorToHSV(color,hsv);
            hsv[0] = hue;
            color = Color.HSVToColor(hsv);
            pixels[i] = color;
        }

        return setBitmapRGB(bitmap,pixels);
    }


    /**
     * Permet de passer un filtre sepia à l'image
     * @param bitmap
     * @return Bitmap
     */
    static Bitmap sepia(Bitmap bitmap) {
        int[] pixels = getBitmapRGB(bitmap);

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

        return setBitmapRGB(bitmap,pixels);
    }


    /**
     * Permet de passer toute une image en gris sauf la teinte hue
     * @param bitmap
     * @param hue teinte
     * @param interval tolérance
     * @return Bitmap
     */
    static Bitmap isolateColor(Bitmap bitmap, int hue, int interval) {
        int[] pixels = getBitmapRGB(bitmap);
        boolean intervalWithZero = false;
        int min = (hue-interval)%360;
        int max = (hue+interval)%360;
        if (min<0) {
            intervalWithZero = true;
            min = min+360;
        }
        if (max<0) {
            intervalWithZero = true;
            max = max+360;
        }
        if (min>max) {
            int tmp = min;
            min = max;
            max = tmp;
        }

        for (int i=0 ; i<pixels.length ; i++) {
            int color = pixels[i];
            float[] hsv = new float[3];
            Color.colorToHSV(color,hsv);

            if((!intervalWithZero && (hsv[0]>max || hsv[0]<min)) || (intervalWithZero && (hsv[0]<max && hsv[0]>min))) {
                int red = Color.red(color);
                int green = Color.green(color);
                int blue = Color.blue(color);
                int gray = (30*red + 59*green + 11*blue)/100;
                color = Color.rgb(gray,gray,gray);
            }

            pixels[i] = color;
        }

        return setBitmapRGB(bitmap,pixels);
    }



    /**
     * Permet de changer la luminosité d'une image
     * @param bitmap
     * @param value
     * @param pixelsHSV
     * @return
     */
    static Bitmap brightness(Bitmap bitmap, int value, float[] pixelsHSV) {
        float valueF = value-50;
        int[] pixels = getBitmapRGB(bitmap);
        float[] hsv = new float[3];

        for (int i = 0; i < pixels.length; i++) {
            Color.colorToHSV(pixels[i], hsv);
            hsv[2] = pixelsHSV[i] + valueF / 100;
            pixels[i] = Color.HSVToColor(hsv);
        }

        return  setBitmapRGB(bitmap,pixels);
    }



    /**
     * ********** CONTRASTE ***********
     */

    /**
     * Est lancé quand le contraste diminue
     * @param min
     * @param max
     * @param bitmap
     * @param pixelsContrast
     */
    static Bitmap contrastLower(int min, int max, Bitmap bitmap, int[] pixelsContrast) {
        int[] LUT = new int[256];
        for(int i=0 ; i<256 ; i++) {
            LUT[i] = (i * (max-min) / 255) + min;
        }
        return contrast(bitmap, pixelsContrast, LUT);
    }


    /**
     * Est lancé quand le contraste augmente
     * @param min
     * @param max
     * @param bitmap
     * @param pixelsContrast
     */
    static Bitmap contrastIncrease(int min, int max, Bitmap bitmap, int[] pixelsContrast) {
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
        return contrast(bitmap, pixelsContrast, LUT);
    }


    /**
     * Algo qui permet de transformer les pixels
     * @param bitmap
     * @param pixelsContrast
     * @param LUT
     * @return
     */
    private static Bitmap contrast(Bitmap bitmap, int[] pixelsContrast, int[] LUT) {
        int[] pixels = getBitmapRGB(bitmap);

        for (int i=0 ; i<pixelsContrast.length ; i++) {
            int red = Color.red(pixelsContrast[i]);
            red = changeColor(red,LUT);

            int green = Color.green(pixelsContrast[i]);
            green = changeColor(green,LUT);

            int blue = Color.blue(pixelsContrast[i]);
            blue = changeColor(blue,LUT);

            pixels[i] = Color.rgb(red,green,blue);
        }

        return setBitmapRGB(bitmap,pixels);
    }


    /**
     * Permet de changer les pixels à l'aide de LUT
     * @param color
     * @param LUT
     * @return
     */
    private static int changeColor (int color, int[] LUT) {
        color = LUT[color];
        if (color<0) {
            color = 0;
        } else if (color>255) {
            color = 255;
        }
        return color;
    }



    /**
     * Permet de provoquer un effet de surexposition sur une image
     * @param bitmap
     * @param value
     * @param pixelsOver
     * @return
     */
    static Bitmap overexposure(Bitmap bitmap, int value, int[] pixelsOver) {
        float valueF = (float) value/10;
        if (valueF<1) { valueF = 1; }
        int[] pixels = getBitmapRGB(bitmap);

        for (int i=0 ; i<pixels.length ; i++) {
            int red = Color.red(pixelsOver[i]);
            red = Math.round(valueF * red);
            if (red>255) { red = 255; }
            int green = Color.green(pixelsOver[i]);
            green =  Math.round(valueF * green);
            if (green>255) { green = 255; }
            int blue = Color.blue(pixelsOver[i]);
            blue = Math.round(valueF * blue);
            if (blue>255) { blue = 255; }
            pixels[i] = Color.rgb(red,green,blue);
        }

        return setBitmapRGB(bitmap,pixels);
    }



    /**
     * Zoom par interpolation au plus proche voisin
     * @param bitmap
     * @param zoom : facteur de zoom fixe
     * @return
     */
    static Bitmap zoom(Bitmap bitmap, int zoom) {
        //S'occuper de la taille
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int heightN = zoom*height;
        int widthN = zoom*width;

        //recuperer pixels de bitmap
        int[] pixels = getBitmapRGB(bitmap);

        Bitmap bmp = Bitmap.createBitmap(widthN,heightN,Bitmap.Config.ARGB_8888);
        int[] pixelsN = new int[heightN*widthN];
        for(int i=0 ; i<heightN*widthN ; i++) {
            int x = (i%widthN)/zoom;
            int y = (i/widthN)/zoom;
            pixelsN[i] = pixels[x+y*width];
        }

        return setBitmapRGB(bmp,pixelsN);
    }
}
