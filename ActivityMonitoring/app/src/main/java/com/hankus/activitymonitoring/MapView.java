package com.hankus.activitymonitoring;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.provider.Telephony;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

public class MapView extends View {

  //Bitmap background;
    private Canvas mCanvas;
    private Paint mPaint;
    private ArrayList<Particle> mParticles;
    private String tag = "MapView";

    private int width;
    private int height;

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
        Log.wtf("MapView","OnDraw (Num = " + mParticles.size() + ")");

        for (Particle p: mParticles
             ) {
            canvas.drawCircle(p.x, p.y, p.weight * 10, mPaint);
        }

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;

        Log.wtf(tag, "Width = " + w + "; Height = " + h);
    }

    void initMap()
    {
        num_circles = 0;

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(ResourcesCompat.getColor(getResources(),
                R.color.colorPrimaryDark, null));

        mParticles = new ArrayList<Particle>();
    }

    void addParticle(Particle particle)
    {
        Log.wtf(tag, "Add Particle");
        Log.wtf(tag, "x = " + particle.x);
        Log.wtf(tag, "y = " + particle.y);
        mParticles.add(particle);
    }

    void removeParticle()
    {
        Log.wtf(tag, "Add Particle");
        mParticles.remove(0);
    }

    void clear()
    {
        Log.wtf(tag, "Clear all particles");
        mParticles.clear();
        invalidate();
    }

    void update()
    {
        invalidate();
    }

    int getMapWidth()
    {
        return width;
    }

    int getMapHeight()
    {
        return height;
    }
}
