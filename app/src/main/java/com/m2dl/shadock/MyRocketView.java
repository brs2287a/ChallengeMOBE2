package com.m2dl.shadock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.View;

public class MyRocketView extends View {

    private final Bitmap rocketBitmap;
    float x = 0.0f;
    float y = 0.0f;

    public MyRocketView(Context context) {
        super(context);
        rocketBitmap = BitmapFactory.decodeResource(context.getResources(),
                R.drawable.fusee_shadocks_resized);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Rect imageBounds = canvas.getClipBounds();
        canvas.drawBitmap(rocketBitmap,  x, y, null);
    }
}
