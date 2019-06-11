package com.hankus.activitymonitoring;

import android.app.ActionBar;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.provider.CalendarContract;
import android.provider.Telephony;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class MapView extends View {

    private Canvas mCanvas;
    private Paint mPaint;
    private String tag = "MapView";
    private Bitmap mBackground;
    private ParticleSet mParticleSet;

    public int width;
    public int height;
    boolean mInitialized;

    public MapView(Context context) {
        super(context);
        mInitialized = false;

    }

    public MapView(Context context, AttributeSet attrst) {
        super(context, attrst);

        mInitialized = false;
    }

    public MapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mInitialized = false;
    }

    public void setParticleSet(ParticleSet particleSet)
    {
        mParticleSet = particleSet;
    }


    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        Log.wtf("MapView","OnDraw (Num = " + mParticleSet.mParticles.size() + ")");

        for (Particle p: mParticleSet.mParticles
             ) {
            canvas.drawCircle(p.x, p.y, p.weight, mPaint);
        }

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        width = w;
        height = h;

        Log.wtf(tag, "Width = " + w + "; Height = " + h);

        initMap();
    }

    public void initMap()
    {
        Drawable background_image = ResourcesCompat.getDrawable(getResources(), R.drawable.iti_floorplan2, null);
        Bitmap background = Bitmap.createScaledBitmap(((BitmapDrawable)background_image).getBitmap(), width, height, true);
        mBackground = background;
        this.setBackground(new BitmapDrawable(getResources(), background));


        for(int i = 0; i < mBackground.getWidth(); i++) {

            for (int j = 0; j < mBackground.getHeight(); j++) {
                if (mBackground.getPixel(i, j) == 0xFFFFFFFF)
                    mParticleSet.mFloor.add(new Point(i, j));
                else if (mBackground.getPixel(i, j) != 0xFFFF0000)
                    mParticleSet.mWalls.add(new Point(i, j));
            }
        }

        mParticleSet.mMaxY = height;
        mParticleSet.mMaxX = width;

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(ResourcesCompat.getColor(getResources(),
                R.color.colorPrimaryDark, null));

        mInitialized = true;
    }


    boolean initialized()
    {
        return mInitialized;
    }

    void update()
    {
        invalidate();
    }

    int getMapWidth()
    {
        return width;
    }

    int getMapHeight() { return height; }

    public void drawWalls()
    {
        mParticleSet.mParticles.clear();
        for (Point p: mParticleSet.mWalls
        ) {
            mParticleSet.mParticles.add(new Particle(p.x, p.y, 0, 1));
        }
    }


}
