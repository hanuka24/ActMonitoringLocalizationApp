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

    public float mScaleY;
    public float mScaleX;
    public int mWidth;
    public int mHeight;

    public MapView(Context context) {
        super(context);

    }

    public MapView(Context context, AttributeSet attrst) {
        super(context, attrst);
    }

    public MapView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
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
            canvas.drawCircle(p.getX(), p.getY(), p.getWeight() * mParticleSet.NUM_PARTICLES, mPaint);
        }

    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;

        Log.wtf(tag, "Width = " + w + "; Height = " + h);

        initMap();
    }

    public void initMap()
    {
        Drawable background_image = ResourcesCompat.getDrawable(getResources(), R.drawable.iti_floorplan2, null);

        mScaleX = mWidth *  getResources().getDisplayMetrics().density / (float)((BitmapDrawable)background_image).getBitmap().getWidth() ;
        mScaleY = mHeight *  getResources().getDisplayMetrics().density / (float)((BitmapDrawable)background_image).getBitmap().getHeight();

        Bitmap background = Bitmap.createScaledBitmap(((BitmapDrawable)background_image).getBitmap(), mWidth, mHeight, true);
        mBackground = background;
        this.setBackground(new BitmapDrawable(getResources(), background));

        for(int i = 0; i < mBackground.getWidth(); i++) {

            for (int j = 0; j < mBackground.getHeight(); j++) {
                if (mBackground.getPixel(i, j) == 0xFFFFFFFF)
                    mParticleSet.mFloor.add(new Point(i, j));
            }
        }

        mParticleSet.mMaxY = mWidth;
        mParticleSet.mMaxX = mHeight;

        mParticleSet.mScaleMeter = mHeight / 18 * mScaleY;

        Log.wtf(tag, "ScaleMeter: " + mParticleSet.mScaleMeter);

        Walls walls = new Walls();
        walls.scaleWalls(mScaleX, mScaleY);

        mParticleSet.mWalls.addAll(walls.getScaledWalls());

        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(ResourcesCompat.getColor(getResources(),
                R.color.colorPrimaryDark, null));

    }


    void update()
    {
        invalidate();
    }

    int getMapWidth()
    {
        return mWidth;
    }

    int getMapHeight() { return mHeight; }

    public void drawWalls()
    {
        mParticleSet.mParticles.clear();
        for(Line p : mParticleSet.mWalls)
        {
            if(p.startPoint.x == p.endPoint.x)
            {
                int y_start = (p.startPoint.y > p.endPoint.y) ? p.endPoint.y : p.startPoint.y;
                int y_end = (p.startPoint.y > p.endPoint.y) ? p.startPoint.y : p.endPoint.y;
                for(int i = 0; i < y_end - y_start; i++)
                    mParticleSet.mParticles.add(new Particle((int)((float)p.startPoint.x), (int)((float)(y_start + i)), 3.0f / mParticleSet.NUM_PARTICLES));
            }
            else
            {
                int x_start = (p.startPoint.x > p.endPoint.x) ? p.endPoint.x : p.startPoint.x;
                int x_end = (p.startPoint.x > p.endPoint.x) ? p.startPoint.x : p.endPoint.x;
                for(int i = 0; i < x_end - x_start; i++)
                    mParticleSet.mParticles.add(new Particle((int)((float)(x_start + i)), (int)((float)p.startPoint.y), 3.0f / mParticleSet.NUM_PARTICLES));
            }
        }
    }


}
