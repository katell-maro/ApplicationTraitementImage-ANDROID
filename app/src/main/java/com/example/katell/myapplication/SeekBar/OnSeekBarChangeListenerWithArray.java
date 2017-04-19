package com.example.katell.myapplication.SeekBar;

import android.widget.SeekBar;

/**
 * Created by Katell on 31/03/2017.
 * Cette classe permet d'utiliser un tableau dans les méthodes de OnSeekBarChangeListener
 * On peut donc utiliser avoir un meilleur rendu des algorithmes car on travaille toujours sur l'image initiale et non sur l'image précédente
 */
public class OnSeekBarChangeListenerWithArray implements SeekBar.OnSeekBarChangeListener {

    /**
     * Tableau d'entiers comprenant tous les pixels en RGB d'une image
     */
    final private int[] pixels;

    /**
     * Tableau de float qui réprésente le v de hsv
     */
    final private float[] brightness;

    /**
     * Tableau qui est utilisé pour la convolution gaussienne
     */
    final private int[][] gaussien = new int[][]{{1,2,3,2,1}, {2,6,8,6,2}, {3,8,10,8,3}, {2,6,8,6,2}, {1,2,3,2,1}};


    /**
     * Contructeur
     */
    protected OnSeekBarChangeListenerWithArray() {
        this.pixels = null;
        this.brightness = null;
    }


    /**
     * Constructeur
     * @param pixels tableau des pixels RGB de l'image
     */
    protected OnSeekBarChangeListenerWithArray(int[] pixels) {
        this.pixels = pixels;
        this.brightness = null;
    }


    /**
     * Constructeur
     * @param pixels tableau des pixels RGB de l'image
     */
    protected OnSeekBarChangeListenerWithArray(int[] pixels, float[] brightness) {
        this.pixels = pixels;
        this.brightness = brightness;
    }



    /**
     * GETTER
     * retourne le champs pixels
     * @return pixels
     */
    protected int[] getPixels() {
        return this.pixels;
    }



    /**
     * GETTER
     * Retourne le champs brightness
     * @return brightness
     */
    protected float[] getBrightness() {
        return this.brightness;
    }



    /**
     * GETTER
     * Retourne la matrice de gauss
     * @return gauss
     */
    protected  int[][] getGaussien() { return this.gaussien; }



    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
