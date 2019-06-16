package com.hankus.activitymonitoring;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class MapView extends View {

    private Paint mPaint;
    private Paint mPaintPos;
    private String mTag = "MapView";
    private ParticleSet mParticleSet;

    public float mScaleY;
    public float mScaleX;
    public int mWidth;
    public int mHeight;

    public MapView(Context context) { super(context); }

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
            canvas.drawCircle(p.getX(), p.getY(), 1, mPaint);
        }
        canvas.drawCircle(mParticleSet.mPosX, mParticleSet.mPosY, 3, mPaintPos);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
        mHeight = h;
        //init Map, once the size is known
        Log.wtf(mTag, "Width = " + w + "; Height = " + h);
        initMap();
    }

    public void initMap()
    {
        Drawable background_image = ResourcesCompat.getDrawable(getResources(), R.drawable.iti_floorplan2, null);

        mScaleX = mWidth *  getResources().getDisplayMetrics().density / (float)((BitmapDrawable)background_image).getBitmap().getWidth();
        mScaleY = mHeight *  getResources().getDisplayMetrics().density / (float)((BitmapDrawable)background_image).getBitmap().getHeight();

        mParticleSet.mScaleMeterY = mHeight * mScaleY / ( 2f * 18.0f); //appr 18 m height
        mParticleSet.mScaleMeterX = mParticleSet.mScaleMeterY * mScaleX / mScaleY / 2f;

        Log.wtf(mTag, "scaleX: " + mScaleX);
        Log.wtf(mTag, "scaleX: " + mScaleY);
        Log.wtf(mTag, "ScaleMeter: " + mParticleSet.mScaleMeterX);
        Log.wtf(mTag, "ScaleMeter: " + mParticleSet.mScaleMeterY);

        Bitmap background = Bitmap.createScaledBitmap(((BitmapDrawable)background_image).getBitmap(), mWidth, mHeight, true);
        this.setBackground(new BitmapDrawable(getResources(), background));

        //retrieve floor pixels
        for(int i = 0; i < background.getWidth(); i++) {

            for (int j = 0; j < background.getHeight(); j++) {
                if (background.getPixel(i, j) == 0xFFFFFFFF)
                    mParticleSet.mFloor.add(new Point(i, j));
            }
        }

        //Scale walls according to screen density / picture size
        Walls walls = new Walls();
        walls.scaleWalls(mScaleX, mScaleY);
        mParticleSet.mWalls.addAll(walls.getScaledWalls());

        //Set paints for particles and position
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(ResourcesCompat.getColor(getResources(),
                R.color.colorPrimaryDark, null));

        mPaintPos = new Paint();
        mPaintPos.setStyle(Paint.Style.FILL);
        mPaintPos.setColor(0xFFFF00FF);

    }

    void update()
    {
        invalidate();
    }

    public void drawWalls()
    {
        mParticleSet.mParticles.clear();
        for(Line p : mParticleSet.mWalls)
        {
            if(p.mStartPoint.x == p.mEndPoint.x)
            {
                int y_start = (p.mStartPoint.y > p.mEndPoint.y) ? p.mEndPoint.y : p.mStartPoint.y;
                int y_end = (p.mStartPoint.y > p.mEndPoint.y) ? p.mStartPoint.y : p.mEndPoint.y;
                for(int i = 0; i < y_end - y_start; i++)
                    mParticleSet.mParticles.add(new Particle((int)((float)p.mStartPoint.x), (int)((float)(y_start + i)), 1.0f / mParticleSet.mNumParticles));
            }
            else if(p.mStartPoint.y == p.mEndPoint.y)
            {
                int x_start = (p.mStartPoint.x > p.mEndPoint.x) ? p.mEndPoint.x : p.mStartPoint.x;
                int x_end = (p.mStartPoint.x > p.mEndPoint.x) ? p.mStartPoint.x : p.mEndPoint.x;
                for(int i = 0; i < x_end - x_start; i++)
                    mParticleSet.mParticles.add(new Particle((int)((float)(x_start + i)), (int)((float)p.mStartPoint.y), 1.0f / mParticleSet.mNumParticles));
            }
            else
                Log.wtf(mTag, "Invalid wall");
        }
    }


}
