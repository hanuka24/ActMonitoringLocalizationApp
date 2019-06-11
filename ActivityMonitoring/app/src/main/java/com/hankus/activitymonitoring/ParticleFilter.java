package com.hankus.activitymonitoring;

import android.graphics.Canvas;
import android.graphics.Point;
import android.util.Log;

import java.util.ArrayList;
import java.util.Random;

public class ParticleFilter {
    private String tag = "ParticleFilter";
    private ParticleSet mParticleSet;
    private boolean is_initialized;

    public ParticleFilter(ParticleSet particleSet)
    {
        this.mParticleSet = particleSet;
        this.is_initialized = false;
    }

    public void initParticles()
    {
        if(!is_initialized)
        {
            Random r = new Random();
            mParticleSet.clear();
            for (int i = 0; i < mParticleSet.NUM_PARTICLES; i++) {
                Point p =  mParticleSet.mFloor.get(r.nextInt(mParticleSet.mFloor.size() - 1));
                mParticleSet.addParticle(new Particle(p.x, p.y, 0, 2));
            }
            is_initialized = true;
        }
    }

    public void sense()
    {
        for (int i = mParticleSet.mParticles.size() - 1; i >= 0; i--)
        {
            Particle particle = mParticleSet.mParticles.get(i);
            if(mParticleSet.mParticles.get(i).weight == 0)
            {
                removeParticle(i);
            }
        }
    }

    public void resampling()
    {
        int num_new_particle = mParticleSet.NUM_PARTICLES - mParticleSet.mParticles.size();
        int num_old_particles = mParticleSet.mParticles.size();

        for(int i = 0; i < num_new_particle; i++) {
            Random r = new Random();
            Particle p = mParticleSet.mParticles.get(r.nextInt(num_old_particles - 1));
            p.x += r.nextInt(3);
            p.y += r.nextInt(3);
            mParticleSet.addParticle(new Particle(p.x, p.y, p.orientation, p.weight));
        }
    }

    public void moveParticles(int stepwidth, float direction) //move particles and remove if they move on wall
    {
        for (int i = mParticleSet.mParticles.size() - 1; i >= 0; i--) {

            double orientation_randomness = (new Random().nextInt(25) * (new Random().nextBoolean() ? 1 : -1) * Math.PI / 180f);

            int x = mParticleSet.mParticles.get(i).x + (int)(stepwidth * Math.sin((double) direction + orientation_randomness));
            int y = mParticleSet.mParticles.get(i).y + (int)(stepwidth * Math.cos((double) direction + orientation_randomness));

            if(!wallCollision(i, x, y))
            {
                mParticleSet.mParticles.set(i, new Particle(x,y, mParticleSet.mParticles.get(i).orientation, mParticleSet.mParticles.get(i).weight));
            }
            else
            {
                mParticleSet.mParticles.get(i).weight = 0;
            }
        }
    }

    public boolean wallCollision(int i, int x, int y)
    {
        Point p = new Point(mParticleSet.mParticles.get(i).x, mParticleSet.mParticles.get(i).y);
        if(!crossedWall(new Point(x, y), mParticleSet.mParticles.get(i))
                && mParticleSet.mFloor.contains(p) && !outOfBound(p.x, p.y))
            return false;
        else
            return true;
    }

    private boolean crossedWall(Point new_point, Particle old_particle)
    {
        int x_high = 0;
        int x_low = 0;
        int y_high = 0;
        int y_low = 0;

        if(new_point.x < old_particle.x)
        {
            x_high = old_particle.x;
            x_low = new_point.x;
        }
        else
        {
            x_high = new_point.x;
            x_low = old_particle.x;
        }

        if(new_point.y < old_particle.y)
        {
            y_high = old_particle.y;
            y_low = new_point.y;
        }
        else
        {
            y_high = new_point.y;
            y_low = old_particle.y;
        }

        for (Point point : mParticleSet.mWalls)
        {
          if(point.x < x_high && point.x > x_low
            && point.y < y_high && point.y > y_low)
          {
              return true;
          }
        }

        return false;
    }

    private boolean outOfBound(int x, int y)
    {
        if(x < 0 || x >= mParticleSet.mMaxX || y < 0 || y >= mParticleSet.mMaxY)
            return true;

        return false;
    }

    public void removeParticle(int i)
    {
        Log.wtf(tag, "Remove Particle");
        mParticleSet.mParticles.remove(i);
    }
}
