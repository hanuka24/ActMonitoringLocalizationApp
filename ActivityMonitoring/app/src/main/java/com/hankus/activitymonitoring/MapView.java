package com.hankus.activitymonitoring;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class MapView extends View {

  //Bitmap background;
    Canvas mCanvas;
    Paint mPaint;

    int num_circles;

    public MapView(Context context) {
        super(context);
        initMap();
    }

    public MapView(Context context, AttributeSet attrst) {
        super(context, attrst);
        Log.wtf("MapView","Create MapView");
        initMap();
    }

    public MapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.wtf("MapView","Create MapView");
        initMap();
    }


    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);
        Log.wtf("MapView","OnDraw (Num = " + num_circles + ")");

        for(int i = 0; i < num_circles; i++)
            canvas.drawCircle(20, i * 50, 20, mPaint);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        //super.onSizeChanged(w, h, oldw, oldh);

        // your Canvas will draw onto the defined Bitmap
       // background = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        //mCanvas = new Canvas(background);
    }

    void initMap()
    {
        num_circles = 0;

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(ResourcesCompat.getColor(getResources(),
                R.color.colorPrimaryDark, null));


    }

    void addCircle()
    {
        Log.wtf("MapView", "Add circles");
        num_circles++;
        invalidate();
    }

    void clear()
    {
        num_circles = 0;
        invalidate();
    }

    void update()
    {
        invalidate();
    }
}
