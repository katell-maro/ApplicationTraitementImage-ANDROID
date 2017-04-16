package com.example.katell.myapplication;

import android.graphics.*;
import android.util.Log;

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
            colorToHSV(pixels[i], hsv);
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
     * Permet de convertir un pixel hsv en pixel rgb
     * @param hsv le tableau HSV
     * @return le pixel en rgb
     */
    static int HSVToColor(float[] hsv) {
        float h = hsv[0];
        float s = hsv[1];
        float v = hsv[2] ;

        int h2 = (int) (h/60 % 6);
        float f = h/60 - h2;
        v = 255*v; //pour pouvoir l'utiliser dans rgb
        int l = (int) (v*(1-s));
        int m = (int) (v*(1-f*s));
        int n = (int) (v*(1-(1-f)*s));

        switch (h2) {
            case 0:
                if (h==360) {
                    return Color.rgb((int) v,0,0);
                } else {
                    return Color.rgb((int) v,n, l);
                }

            case 1:
                return Color.rgb(m,(int) v,l);

            case 2:
                return Color.rgb(l,(int) v,n);

            case 3:
                return Color.rgb(l, m, (int) v);

            case 4:
                return Color.rgb(n, l , (int) v);

            case 5:
            default:
                return Color.rgb((int) v, l, m);
        }
    }



    /**
     * Permet de convertir un pixel rgb en pixel hsv
     * @param color le pixel en rgb
     * @param hsv le tableau à remplir pour avoir le pixel en hsv
     */
    static void colorToHSV(int color, float[] hsv) {
        float r = Color.red(color);
        float g = Color.green(color);
        float b = Color.blue(color);

        float max = Math.max(Math.max(r,g),b); //pour avoir le max entre r, g et b
        float min = Math.min(Math.min(r,g),b); //pour avoir le min entre r, g et b

        //hue
        float hue;
        float diff = max-min;
        if (max==min) {
            hue = 0;
        } else if (max==r) {
            hue = (60*(((g-b)/diff) + 360)) % 360;
        } else if (max==g) {
            hue = (60*((b-r)/diff) + 120) % 360;
        } else {
            hue = (60*((r-g)/diff) + 240) % 360;
        }

        //saturation
        float saturation;
        if (max != 0) {
            saturation = 1 - min/max;
        } else {
            saturation = 0;
        }

        //value
        float value = max/255;

        hsv[0] = hue;
        hsv[1] = saturation;
        hsv[2] = value;

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
            //int gray = (red+green+blue)/3;
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
    static Bitmap toColorize(Bitmap bitmap,float hue) {

        int[] pixels = getBitmapRGB(bitmap);

        int color;
        float[] hsv = new float[3];

        for (int i=0 ; i<pixels.length ; i++) {
            color = pixels[i];
            colorToHSV(color,hsv);
            hsv[0] = hue;
            color = HSVToColor(hsv);
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
            colorToHSV(color,hsv);
            hsv[0] = 35;
            hsv[1] = hsv[1]*55/100;
            color = HSVToColor(hsv);
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
    static Bitmap isolateColor(Bitmap bitmap, int hue, int interval, int[] pixelsInit) {
        int[] pixels = pixelsInit.clone();
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
            colorToHSV(color,hsv);

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
            colorToHSV(pixels[i], hsv);
            hsv[2] = pixelsHSV[i] + valueF/100;
            if (hsv[2]<0) { hsv[2] = 0; }
            if (hsv[2]>1) { hsv[2] = 1; }
            pixels[i] = HSVToColor(hsv);
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


    /**
     * Egalisation d'histogramme pour une image en couleur
     * @param bitmap
     * @return
     */
    static Bitmap histogram(Bitmap bitmap) {
        //Calcul de l'histogramme sur l'image en nuances de gris
        Bitmap bitmapGray = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        bitmapGray = toGray(bitmapGray);
        int[] pixelsGray = getBitmapRGB(bitmapGray);
        int[] h = new int[256];

        for (int i=0 ; i<pixelsGray.length ; i++) {
            int color = Color.red(pixelsGray[i]);  //car tous les canaux sont égaux
            h[color]++;
        }


        //Calcul de l'histogramme cumulé
        int[] sum = new int[256];
        sum[0] = h[0];
        for (int i=1 ; i<256 ; i++) {
            sum[i] = sum[i-1] + h[i];
        }


        //Transformation des canaux
        int[] pixels = getBitmapRGB(bitmap);
        int size = pixels.length;

        for (int i=0 ; i<size ; i++) {

            int color = pixels[i];
            float value = (float) sum[Color.red(pixelsGray[i])]/ (float) size;
            float[] hsv = new float[3];
            colorToHSV(color, hsv);
            hsv[2] = value;
            color = HSVToColor(hsv);
            pixels[i] = color;
        }

        return setBitmapRGB(bitmap, pixels);
    }


    /**
     * Seuillage d'une image
     * Permet de passer une image en noir et blanc à l'aide d'un seuil
     * D'avoir une image monochrome
     * @param bitmap
     * @param threshold
     * @return bitmap
     */
    static Bitmap thresholding(Bitmap bitmap, int threshold, int[] pixelsInit) {
        int[] pixels = pixelsInit.clone();
        for (int i=0 ; i<pixels.length ; i++) {
            if (Color.red(pixels[i])>=threshold) {
                pixels[i] = Color.rgb(255,255,255);
            } else {
                pixels[i] = Color.rgb(0,0,0);
            }
        }

        return setBitmapRGB(bitmap,pixels);
    }


    /**
     * Permet de fusionner deux images
     * @param bitmap1
     * @param bitmap2 plus petite que la bitmap1
     * @param posX la position de la bitmap2 en x
     * @param posY la position de la bitmap2 par rapport à la bitmap1 en y
     * @param factor pour savoir l'importance de la fusion
     * @return la bitmap de sortie
     */
    static Bitmap fusion(Bitmap bitmap1, Bitmap bitmap2, int posX, int posY, int factor) {
        //si bitmap2 est plus grande que bitmap1
        int w1 = bitmap1.getWidth();
        int h1 = bitmap1.getHeight();
        int w2 = bitmap2.getWidth();
        int h2 = bitmap2.getHeight();
        if (w2>w1 || h2>h1) {
            return bitmap1;
        }

        int[] pixels1 = getBitmapRGB(bitmap1);
        int[] pixels2 = getBitmapRGB(bitmap2);
        int start = posY*bitmap1.getWidth()+posX;
        int k = start;
        int cpt = 0;

        for (int i=0; i<pixels2.length ; i++) {
            if (cpt>=bitmap2.getWidth()) {
                k += bitmap1.getWidth() - bitmap2.getWidth();
                cpt = 0;
            }

            int a1 = Color.alpha(pixels1[k]);
            int a2 = Color.alpha(pixels2[i]);

            //red
            int r1 = Color.red(pixels1[k]);
            int r2 = Color.red(pixels2[i]);
            int r = r1;

            //green
            int g1 = Color.green(pixels1[k]);
            int g2 = Color.green(pixels2[i]);
            int g = g1;

            //blue
            int b1 = Color.blue(pixels1[k]);
            int b2 = Color.blue(pixels2[i]);
            int b = b1;

            if (a2!=0 && a1!=0) {
                r = (factor*r1+r2)/(factor+1);
                g = (factor*g1+g2)/(factor+1);
                b = (factor*b1+b2)/(factor+1);
            }

            pixels1[k] = Color.rgb(r,g,b);

            k++;
            cpt++;
        }

        return setBitmapRGB(bitmap1, pixels1);
    }


    /**
     * permet de gerer la taille de l'image noise pour la caller avec celle de bitmap et ensuite de les fusionner
     * @param bitmap
     * @param noise
     * @return
     */
    static Bitmap fusionNoise(Bitmap bitmap, Bitmap noise) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int width2 = noise.getWidth();
        int height2 = noise.getHeight();

        Bitmap bm;

        if (width2<width || height2<height) {
            int[] pixels = getBitmapRGB(noise);
            int[] pixelsFinal = new int[width*height];
            for (int i=0 ; i<pixelsFinal.length-1 ; i++) {
                pixelsFinal[i] = pixels[i%pixels.length];
            }
            bm = Bitmap.createBitmap(pixelsFinal,width,height, Bitmap.Config.ARGB_8888);
        } else {
            bm = Bitmap.createBitmap(noise, 0, 0, width, height);
        }

        return fusion(bitmap,bm,0,0,3);
    }


    /**
     * Permet de rogner les bords de l'imge selon un effet un peu vieilli
     * @param bitmap
     * @return bitmap
     */
    static Bitmap crop(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = getBitmapRGB(bitmap);
        int cropW = width/100;
        int cropH = height/100;
        int bound = pixels.length-width+1;

        //pour le haut
        for (int i=0 ; i<width ; i++) {
            int random = (int) (Math.random()*cropH);
            for (int k=0 ; k<random ; k++) {
                pixels[i+k*width] = Color.rgb(255,255,255);
            }
        }

        //pour le bas
        for (int i=bound ; i<pixels.length ; i++) {
            int random = (int) (Math.random()*cropH);
            for (int k=0 ; k<random ; k++) {
                pixels[i-k*width] = Color.rgb(255,255,255);
            }
        }

        //pour la gauche
        for (int i=0 ; i<bound ; i+=width) {
            int random = (int) (Math.random()*cropW);
            for (int k=0 ; k<random ; k++) {
                pixels[i+k] = Color.rgb(255,255,255);
            }
        }

        //pour la droite
        for (int i=width-1 ; i<bound ; i+=width) {
            int random = (int) (Math.random()*cropW);
            for (int k=0 ; k<random ; k++) {
                pixels[i-k] = Color.rgb(255,255,255);
            }
        }

        return setBitmapRGB(bitmap,pixels);
    }

}
