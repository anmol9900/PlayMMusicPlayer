package com.example.anmolpc.playmmusicplayer.adapter;


import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;


/**
 * Created by Anmol Pc on 5/7/2017.
 */

public class BitmapBrightness {

    public Bitmap setBrightness(Bitmap src, int value)
    {
        Bitmap alteredBitmap = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
        Canvas canvas = new Canvas(alteredBitmap);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        float b = (float)value;  // I know seems redundant, but used for possible scaling if needed
        cm.set(new float[] {    1, 0, 0, 0, b,
                0, 1, 0, 0, b,
                0, 0, 1, 0, b,
                0, 0, 0, 1, 0});

        paint.setColorFilter(new ColorMatrixColorFilter(cm));
        Matrix matrix = new Matrix();
        canvas.drawBitmap(src, matrix, paint);

        return alteredBitmap;
    }
}
