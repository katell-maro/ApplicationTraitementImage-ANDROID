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
     * @param bitmap Bitmap dont on veut la tableau de pixels
     * @return tableau de int de taille width*height
     */
    static int[] getBitmapRGB(Bitmap bitmap) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        int[] pixels = new int[height*width];
        bitmap.getPixels(pixels,0,width,0,0,width,height);
        return pixels;
    }



    /**
     * Renvoie le tableau composé de toutes les valeurs de hsv[2] d'un pixel
     * @param bitmap Bitmap dont on veut la luminosité à chaque pixel
     * @return tableau de float de taille width*height
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
     * Retourne une bitmap après lui avoir changé les pixels
     * @param bitmap Bitmap que l'on veut modifier
     * @param pixels tableau de int qui va remplacer les pixels de bitmap
     * @return la bitmap de retour
     */
    static Bitmap setBitmapRGB(Bitmap bitmap, int[] pixels) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        bitmap.setPixels(pixels,0,width,0,0,width,height);
        return bitmap;
    }



    /**
     * Permet de convertir un pixel hsv en pixel rgb
     * @param hsv le tableau HSV pour un pixel
     * @return le pixel en rgb
     *
     * Algorithme trouvé sur Wikipedia
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
     *
     * Algorithme trouvé sur Wikipédia
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
     * @param bitmap l'image que l'on veut griser
     * @return l'image une fois grisée
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
     * Applique une teinte à l'image
     * @param bitmap l'image que l'on veut voir avec une teinte
     * @param hue teinte appliquée
     * @return l'image une fois la teinte appliquée
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
     * Applique une teinte sepia à l'image
     * @param bitmap l'image que l'on veut voir en sepia
     * @return l'image une fois en sepia
     *
     * Paramètres pour HSV trouvé sur divers tutos Photoshop
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
     * Permet de passer toute une image en nuances de gris sauf une certaine couleur
     * @param bitmap que l'on veut voir modifié
     * @param hue la teinte que l'on souhaite garder
     * @param interval la tolérance de l'algorithme (ie plus la tolérance est élevée, plus de couleurs seront gardées)
     * @return l'image une fois modifiée
     */
    static Bitmap isolateColor(Bitmap bitmap, int hue, int interval, int[] pixelsInit) {
        int[] pixels = pixelsInit.clone();
        boolean intervalWithZero = false;
        int min = (hue-interval)%360; //-30
        int max = (hue+interval)%360; //30
        if (min<0) {
            intervalWithZero = true;
            min = min+360; //330
        }
        if (max<0) {
            intervalWithZero = true;
            max = max+360;
        }
        if (min>max) {
            intervalWithZero = true;
            int tmp = min;
            min = max; //30
            max = tmp; //330
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
     * @param bitmap l'image dont on veut changer la luminosité
     * @param value la valeur de changement de luminosité (au dessus de 50, elle augmente, en dessous, elle diminue)
     * @param pixelsInit le tableau de pixels en rgb de l'image initiale
     * @param pixelsHSV le tableau de l'image initiale avec la luminosité de chaque pixels (ie hsv[2])
     * @return l'image une fois modifiée
     */
    static Bitmap brightness(Bitmap bitmap, int value, int[] pixelsInit, float[] pixelsHSV) {
        float valueF = value-50;
        Log.i("value",String.valueOf(valueF));
        int[] pixels = pixelsInit.clone();
        float[] hsv = new float[3];

        for (int i = 0; i < pixels.length; i++) {
            colorToHSV(pixels[i], hsv);
            hsv[2] = pixelsHSV[i] + valueF / 100;
            if (hsv[2] < 0) {
                hsv[2] = 0;
            }
            if (hsv[2] > 1) {
                hsv[2] = 1;
            }
            pixels[i] = HSVToColor(hsv);
        }

        return  setBitmapRGB(bitmap,pixels);
    }



    /**
     * ********** CONTRASTE ***********
     */

    /**
     * L'algo est lancé quand le contraste diminue
     * Permet de calculer la LUT
     * @param min la valeur minimum que l'on veut avoir pour une composante d'un pixel
     * @param max la valeur maximum que l'on veut avoir pour une composante d'un pixel
     * @param bitmap l'image que l'on souhaite modifier
     * @param pixelsContrast le tableau de pixels de l'image initiale
     * @return l'image une fois modifiée
     */
    static Bitmap contrastLower(int min, int max, Bitmap bitmap, int[] pixelsContrast) {
        int[] LUT = new int[256];
        for(int i=0 ; i<256 ; i++) {
            LUT[i] = (i * (max-min) / 255) + min;
        }
        return contrast(bitmap, pixelsContrast, LUT);
    }


    /**
     * L'algo est lancé quand le contraste augmente
     * Permet de calculer la LUT
     * @param min la valeur minimum que l'on veut avoir pour une composante d'un pixel
     * @param max la valeur maximum que l'on veut avoir pour une composante d'un pixel
     * @param bitmap l'image que l'on souhaite modifier
     * @param pixelsContrast le tableau de pixels de l'image initiale
     * @return l'image une fois modifiée
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
     * Permet de modifier le contraste d'une image à l'aide des LUT calculées précédement
     * @param bitmap l'image dont on veut changer le contraste
     * @param pixelsContrast le tableau de pixels de l'image initiale
     * @param LUT le tableau qu'il faut appliquer aux pixels de l'image initiale
     * @return l'image une fois modifiée
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
     * Permet de changer les pixels à l'aide de la LUT
     * @param color la couleur à modifier
     * @param LUT le tableau qu'il faut appliquer aux pixels de l'image initiale
     * @return la couleur correspondante
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
     * @param bitmap l'image que l'on veut voir avec cet effet
     * @param value la force de l'algorithme (ie plus la value est grande, plus la surexposition sera importante
     * @param pixelsOver le tableau de pixels de l'image initiale
     * @return l'image une fois modifiée
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
     * Egalisation d'histogramme pour une image en couleur
     * @param bitmap l'imae que l'on souhaite égaliser
     * @return l'imge une fois égalisée
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
     * Les pixels de l'image seront soit blanc, soit noir
     * @param bitmap la bitmap qui l'on veut modifier
     * @param threshold le seuil (plus le seuil est élévé, plus l'image est blanche)
     * @return l'image une fois modifiée
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
     * @param bitmap1 une image
     * @param bitmap2 une image plus petite que la bitmap1
     * @param posX la position de la bitmap2 en x par rapport à la bitmap1
     * @param posY la position de la bitmap2 par rapport à la bitmap1 en y
     * @param factor permet de savoir l'importance de la fusion (plus factor est important, moins bitmap2 sera visible)
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
     * Permet de fusionner deux images, une image avec du grain (noise) et l'autre
     * L'algo modifie la taille de noise pour la caler à celle de bitmap
     * Permet d'obtenir un grain à l'image
     * @param bitmap l'image que l'on veut avoir avec du grain
     * @param noise l'image avec du grain
     * @return l'image modifiée
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

            for (int i=0 ; i<height ; i++) {
                for (int j=0 ; j<width ; j++) {

                    if (i>=height2) {
                        int diff = (i-height2)%height2;
                        pixelsFinal[i*width+j] = pixelsFinal[(i-2*diff-1)*width+j];
                    } else if (j>=width2) {
                        int diff = (j-width2)%width2;
                        pixelsFinal[i*width+j] = pixels[(i*width2+j - 2*diff -1)];
                    } else {
                        pixelsFinal[i * width + j] = pixels[i * width2 + j];
                    }
                }
            }

            bm = Bitmap.createBitmap(pixelsFinal,width,height, Bitmap.Config.ARGB_8888);
        } else {
            bm = Bitmap.createBitmap(noise, 0, 0, width, height);
        }

        return fusion(bitmap,bm,0,0,3);
    }



    /**
     * Permet de "croquer" les bords pour donner un effet abimé à l'image
     * @param bitmap l'image initiale
     * @return bitmap l'image modifié
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



    /**
     * Permet de générer la matrice pour la convolution moyenneur
     * @param size la taille de la matrice
     * @return la matrice de taille size*size
     */
    static int[][] moyenneur(int size) {
        int[][] matrix = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                matrix[i][j] = 1;
            }
        }
        return matrix;
    }



    /**
     * Permet d'appliquer une convolution avec une matrice de n'importe quelle dimension. Ne gère que les valeurs positives. Bords calculés avec le miroir de l'image.
     *
     * @param kernel
     */
    static Bitmap convolute(Bitmap bitmap, int[][] kernel, int matrix_size) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = getBitmapRGB(bitmap);
        int[] pixelsN = pixels.clone();
        int max = 255;
        int mat_compt = matrix_size / 2;
        int red, green, blue;
        int factor = 0;

        for (int k = 0; k < matrix_size; k++) {
            for (int l = 0; l < matrix_size; l++) {
                factor += kernel[k][l];
            }
        }

        for (int i = 0; i < height; i++) {
            for (int j = mat_compt; j < width-mat_compt; j++) {

                int red_sum = 0;
                int green_sum = 0;
                int blue_sum = 0;

                for (int k = 0; k < matrix_size; k++) {
                    for (int l = 0; l < matrix_size; l++) {
                        if (i<mat_compt){
                            red_sum += kernel[k][l] * Color.red(pixels[(i + Math.abs(mat_compt - k)) * width + (j - mat_compt + l)]);
                            green_sum += kernel[k][l] * Color.green(pixels[(i + Math.abs(mat_compt - k)) * width + (j - mat_compt + l)]);
                            blue_sum += kernel[k][l] * Color.blue(pixels[(i + Math.abs(mat_compt - k)) * width + (j - mat_compt + l)]);
                        }else if (i>=height-mat_compt){
                            red_sum += kernel[k][l] * Color.red(pixels[(i - Math.abs(mat_compt - k)) * width + (j - mat_compt + l)]);
                            green_sum += kernel[k][l] * Color.green(pixels[(i - Math.abs(mat_compt - k)) * width + (j - mat_compt + l)]);
                            blue_sum += kernel[k][l] * Color.blue(pixels[(i - Math.abs(mat_compt - k)) * width + (j - mat_compt + l)]);
                        }/*else if (j<mat_compt &&(i>=mat_compt && i<height-mat_compt)){
                            red_sum += kernel[k][l] * Color.red(pixels[(i - mat_compt + k) * width + (j + Math.abs(mat_compt - l))]);
                            green_sum += kernel[k][l] * Color.green(pixels[(i - mat_compt + k) * width + (j + Math.abs(mat_compt - l))]);
                            blue_sum += kernel[k][l] * Color.blue(pixels[(i - mat_compt + k) * width + (j + Math.abs(mat_compt - l))]);
                        }else if (j>=width-mat_compt && (i>=mat_compt && i<height-mat_compt)){
                            red_sum += kernel[k][l] * Color.red(pixels[(i - mat_compt + k) * width + (j - Math.abs(mat_compt - l))]);
                            green_sum += kernel[k][l] * Color.green(pixels[(i - mat_compt + k) * width + (j - Math.abs(mat_compt - l))]);
                            blue_sum += kernel[k][l] * Color.blue(pixels[(i - mat_compt + k) * width + (j - Math.abs(mat_compt - l))]);
                        }*/
                        else{
                            red_sum += kernel[k][l] * Color.red(pixels[(i - mat_compt + k) * width + (j - mat_compt + l)]);
                            green_sum += kernel[k][l] * Color.green(pixels[(i - mat_compt + k) * width + (j - mat_compt + l)]);
                            blue_sum += kernel[k][l] * Color.blue(pixels[(i - mat_compt + k) * width + (j - mat_compt + l)]);
                        }
                    }
                }

                if (factor != 0) {
                    red = red_sum / factor;
                    green = green_sum / factor;
                    blue = blue_sum / factor;
                } else {
                    red = red_sum;
                    green = green_sum;
                    blue = blue_sum;
                }
                if (red > max) {
                    red = max;
                }
                if (red < 0) {
                    red = 0;
                }
                if (green < 0) {
                    green = 0;
                }
                if (blue < 0) {
                    blue = 0;
                }
                if (green > max) {
                    green = max;
                }
                if (blue > max) {
                    blue = max;
                }
                pixelsN[i*width + j] = Color.rgb(red, green, blue);
            }
        }

        return setBitmapRGB(bitmap,pixelsN);
    }



    /**
     *
     * Permet d'appliquer un filtre qui a pour facteur 0, gère les valeurs négatives.
     * @param kernel
     * @param matrix_size
     * @return
     */
    static float[] convolute2(Bitmap bitmap, int[][] kernel, int matrix_size) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = getBitmapRGB(bitmap);
        int mat_compt = matrix_size / 2;
        float[] result = new float[width*height - 2*mat_compt];

        for (int i = mat_compt; i < height-mat_compt; i++) {
            for (int j = mat_compt; j < width-mat_compt; j++) {

                float gray_sum = 0;

                for (int k = 0; k < matrix_size; k++) {
                    for (int l = 0; l < matrix_size; l++) {
                        gray_sum += kernel[k][l] * Color.red(pixels[(i - mat_compt + k) * width + (j - mat_compt + l)]);
                    }
                }

                result[i*width+j] = gray_sum;
            }
        }

        float min = result[0];
        float max = result[0];

        for (int i=1; i<result.length ; i++){
            if (result[i]<min){
                min = result[i];
            }
            if (result[i]>max){
                max = result[i];
            }
        }

        for (int i = 0; i<result.length;i++){
            result[i] = ((result[i]-min)/(max-min))*255;
        }

        return result;
    }



    /**
     * Cette fonction permet d'appliquer le résultat des deux convolutions de Sobel à l'image.
     * @param sobel1
     * @param sobel2
     */
    static Bitmap sobel(Bitmap bitmap, float[] sobel1, float[] sobel2){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] val = new int[sobel1.length];
        int[] pixelsN = new int[width*height];
        bitmap.getPixels(pixelsN, 0, width, 0, 0, width, height);
        for (int i=0; i<sobel1.length; i++){
            val[i] = (int)Math.sqrt(sobel1[i]*sobel1[i]+sobel2[i]*sobel2[i]);
            if (val[i]>255){
                val[i] = 255;
            }
            pixelsN[i+1] = Color.rgb(val[i],val[i],val[i]);
        }
        return setBitmapRGB(bitmap,pixelsN);
    }



    /**
     * Permet d'appliquer le résultat du filtre Laplacien à l'image.
     * @param lap
     */
    static Bitmap laplacien(Bitmap bitmap, float [] lap){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixelsN = getBitmapRGB(bitmap);
        bitmap.getPixels(pixelsN, 0, width, 0, 0, width, height);
        for (int i=0; i<lap.length; i++){
            pixelsN[i+1] = Color.rgb((int)lap[i],(int)lap[i],(int)lap[i]);
        }
        return setBitmapRGB(bitmap, pixelsN);
    }

}
