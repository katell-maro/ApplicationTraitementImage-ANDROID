package com.example.katell.myapplication.SeekBar;

import android.widget.SeekBar;

/**
 * Created by Katell on 31/03/2017.
 */
public class OnSeekBarChangeListenerWithArray implements SeekBar.OnSeekBarChangeListener {

    /**
     * Tableau d'entiers comprenant tous les pixels d'une image
     */
    private int[] pixels;

    /**
     * Tableau de float qui réprésente le v de hsv dans le tableau
     */
    private float[] brightness;


    /**
     * Constructeur
     * @param pixels tableau des pixels de l'image
     */
    protected OnSeekBarChangeListenerWithArray(int[] pixels) {
        this.pixels = pixels;
    }


    /**
     * Constructeur
     * @param brightness tableau qui représente la luminosité de chaque pixels
     */
    protected OnSeekBarChangeListenerWithArray(float[] brightness) {
        this.brightness = brightness;
    }


    /**
     * retourne le champs pixels
     * @return pixels
     */
    protected int[] getPixels() {
        return this.pixels;
    }


    /**
     * Retourne le champs brightness
     * @return brightness
     */
    protected float[] getBrightness() {
        return this.brightness;
    }

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
