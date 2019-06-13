package com.hankus.activitymonitoring;

import android.graphics.Point;

import java.util.Random;

public class Particle {
    private Point mPosition;
    private float mWeight;

    Particle(Particle p)
    {
        mPosition = new Point(p.getPosition());
        mWeight = p.mWeight;
    }

    Particle(Point pos, float weight)
    {
        mPosition = new Point(pos);
        mWeight = weight;
    }

    Particle(int x, int y, float weight)
    {
        mPosition = new Point(x,y);
        mWeight = weight;
    }

    public void setX(int x)
    {
        mPosition.x = x;
    }

    public void setY(int y)
    {
        mPosition.y = y;
    }

    public void setPosition(Point pos)
    {
        mPosition = new Point(pos);
    }

    public void setPosition(int x, int y)
    {
        mPosition = new Point(x, y);
    }

    public void setWeight(float weight)
    {
        mWeight = weight;
    }

    public int getX()
    {
       return mPosition.x;
    }

    public int getY()
    {
        return mPosition.y;
    }

    public Point getPosition()
    {
        return mPosition;
    }

    public float getWeight()
    {
        return mWeight;
    }

}
