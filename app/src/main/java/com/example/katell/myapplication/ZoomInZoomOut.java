package com.example.katell.myapplication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

/**
 * Created by Katell on 07/04/2017.
 * Inspiré grandement Stack Overflow
 */
public class ZoomInZoomOut extends MainActivity implements OnTouchListener {

    private static final String TAG = "Touch";

    //Utilisées pour évaluer les points de l'image
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    //Les 3 états possibles pour l'utilisateur
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    //Ces points servent à connaitre la position touchée par l'utilisateur
    private PointF start = new PointF();
    private PointF mid = new PointF();
    private float oldDist = 1f;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImageView view = (ImageView) findViewById(R.id.imageView);
        view.setOnTouchListener(this);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;
        view.setScaleType(ImageView.ScaleType.MATRIX);
        float scale;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            //pour drag : premier doigt vers le bas
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                Log.d(TAG, "mode=DRAG");    //écrit dans LogCat
                mode = DRAG;
                break;

            //premier doigt levé
            case MotionEvent.ACTION_UP:

            //second doigt levé
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                Log.d(TAG, "mode=NONE");
                break;

            //pour zoom : premier et deuxième vers le bas
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                Log.d(TAG, "oldDist=" + oldDist);
                if (oldDist > 5f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                    Log.d(TAG, "mode=ZOOM");
                }
                break;

            //pour zoom ou pour drag
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y); //créer la transformation dans la matrice de points
                } else if (mode == ZOOM) {
                    //pincer pour zoomer
                    float newDist = spacing(event);
                    Log.d(TAG, "newDist=" + newDist);
                    if (newDist > 5f) {
                        matrix.set(savedMatrix);
                        scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }

        view.setImageMatrix(matrix); //affiche le changement

        return true;
    }



    /**
     * Permet de connaitre l'espacement entre les deux doigt
     * @param event MotionEvent
     * @return float
     */
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }



    /**
     * Calcule le point médian enre les deux doigts
     * @param point pointMedian
     * @param event MotionEvent
     */
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }



    /**
     * Permet de savor si l'image sort du cadre
     */
    private boolean inScreen(final ImageView image) {
        //Bitmap bitmap = ((BitmapDrawable) image.getDrawable()).getBitmap();
        int top = image.getTop();
        int bottom = image.getBottom();
        int left = image.getLeft();
        int right = image.getRight();

        Log.i("top", String.valueOf(top));
        Log.i("bottom", String.valueOf(bottom));
        Log.i("left", String.valueOf(left));
        Log.i("right", String.valueOf(right));


        return true;
    }

}
