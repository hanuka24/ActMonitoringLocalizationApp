package com.hankus.activitymonitoring;

import android.graphics.Canvas;
import android.graphics.Point;
import android.provider.Telephony;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class ParticleFilter {
    private String tag = "ParticleFilter";
    private ParticleSet mParticleSet;

    private int ORIENTATION_VARIANCE = 50; //degrees
    private double STEPWIDTH_VARIANCE = 0.5; //m
    private double STEPWIDTH = 0.6; //m


    public ParticleFilter(ParticleSet particleSet)
    {
        this.mParticleSet = particleSet;
    }

    public void sense()
    {
        for (int i = mParticleSet.mParticles.size() - 1; i >= 0; i--)
        {
            if( mParticleSet.mParticles.get(i).getWeight() == 0)
            {
                removeParticle(i);
            }
        }
    }

    public void resampling()
    {
       /* if(! mParticleSet.mParticles.isEmpty())
        {
            int num_new_particle = mParticleSet.NUM_PARTICLES - mParticleSet.mParticles.size();
            int num_old_particles = mParticleSet.mParticles.size();

            for(int i = 0; i < num_new_particle; i++) {
                Random r = new Random();
                if(r.nextFloat() < 0.1f)
                    mParticleSet.addParticle(mParticleSet.createRandomParticle());
                else
                    mParticleSet.addParticle(mParticleSet.createRandomValidParticle());
            }
        }*/

     //https://www.codeproject.com/Articles/865934/Object-Tracking-Particle-Filter-with-Ease
        Log.wtf(tag, "Number of Particles: " + mParticleSet.mParticles.size());
        if(! mParticleSet.mParticles.isEmpty()) {


            ArrayList<Particle> new_particles = new ArrayList<Particle>(mParticleSet.NUM_PARTICLES);

            // compute cumulative weights
            float cum_weights[] = new float[mParticleSet.NUM_PARTICLES];
            float cum_sum = 0.0f;
            cum_weights[0] = 0.0f;


            for (int i = 1; i < mParticleSet.NUM_PARTICLES; i++) {
                cum_weights[i] = cum_weights[i - 1] + mParticleSet.mParticles.get(i).getWeight();
            }

            Random rng = new Random();
            // double p_step = 1.0 / Ns; // probability step size for resampling (new sample weight)
            float init_weight = 1.0f / mParticleSet.NUM_PARTICLES;
            float p_resample = (rng.nextFloat() - 1) * init_weight;
            int cdf_idx = 0;

            for (int i = 0; i < mParticleSet.NUM_PARTICLES; i++) {

                p_resample += init_weight;

                if(rng.nextFloat() < 0.05f)
                    new_particles.add(mParticleSet.createRandomParticle());
                else
                {
                    while (cdf_idx < (mParticleSet.NUM_PARTICLES - 1) && (p_resample > cum_weights[cdf_idx] ||
                            mParticleSet.mParticles.get(cdf_idx).getWeight() == 0.0f)) {
                        cdf_idx++;
                    }

                    if (mParticleSet.mParticles.get(cdf_idx).getWeight() == 0.0f) {

                        new_particles.add(mParticleSet.createRandomParticle());
                        Log.wtf(tag, "Last element weight is 0");
                    } else
                        new_particles.add(new Particle(mParticleSet.mParticles.get(cdf_idx).getPosition(), init_weight));
                }
            }
            mParticleSet.mParticles = new_particles;
        }


          /*  float min_cum_weight = cum_weights[mParticleSet.mParticles.size() - 1];
            float max_cum_weight = cum_weights[0];
            float init_weight = 1.0f / mParticleSet.NUM_PARTICLES;

            Random rand = new Random();
            float rand_weight = (rand.nextFloat() - 1) * init_weight;


            for(int i = 0; i < mParticleSet.NUM_PARTICLES; i++)
            {
                int particle_idx = 0;
                while (particle_idx < mParticleSet.NUM_PARTICLES - 1 && (cum_weights[particle_idx] < rand_weight || mParticleSet.mParticles.get(particle_idx).getWeight() == 0.0f)) //find particle's index
                {
                    particle_idx++;
                }
                if(mParticleSet.mParticles.get(particle_idx).getWeight() == 0.0f)
                    new_particles.set(i, mParticleSet.createRandomParticle());
                else
                    new_particles.set(i, new Particle(mParticleSet.mParticles.get(particle_idx).getPosition(), 1.0f/mParticleSet.NUM_PARTICLES));

                rand_weight += 1.0/init_weight;
            }

            Log.wtf(tag, "Number of new Particles: " + new_particles.size());
           mParticleSet.mParticles = new_particles;
        }*/
    }

    public void updateWeight()
    {
        float sum_weight = 0.0f;
        for(Particle p : mParticleSet.mParticles)
        {
            sum_weight += p.getWeight();
        }

        for(Particle p : mParticleSet.mParticles)
        {
            p.setWeight(p.getWeight()/sum_weight);
        }
    }

    public void moveParticles(int steps, float direction) //move particles and remove if they move on wall
    {
        for(Particle p : mParticleSet.mParticles)
        {
            double orientation_var = (new Random().nextInt(ORIENTATION_VARIANCE) * (new Random().nextBoolean() ? 1 : -1) * Math.PI / 180f);
            double stepwidth_var = (new Random().nextDouble() * STEPWIDTH_VARIANCE * (new Random().nextBoolean() ? 1 : -1));

            int x = p.getX() + (int)(steps * (STEPWIDTH + stepwidth_var)*mParticleSet.mScaleMeterX / 2 * Math.sin((double) direction + orientation_var));
            int y = p.getY() + (int)(steps * (STEPWIDTH + stepwidth_var)*mParticleSet.mScaleMeterY  / 2* Math.cos((double) direction + orientation_var));

            //set Weight to zero, if physical constraints are violated
//            if(!mParticleSet.mFloor.contains(p.getPosition()))

            if(crossedWall(new Point(x,y), p.getPosition()))
                p.setWeight(0.0f);
            else
                p.setPosition(x , y);


        }
        Log.wtf(tag, "Number of Particles: " + mParticleSet.mParticles.size());
    }


    public void positioning()
    {
        if(mParticleSet.mParticles.isEmpty())
            return;

        ArrayList<Integer> x = new ArrayList<>(mParticleSet.NUM_PARTICLES);
        ArrayList<Integer> y = new ArrayList<>(mParticleSet.NUM_PARTICLES);


        for (Particle p: mParticleSet.mParticles
             ) {
            x.add(p.getX());
            y.add(p.getY());
        }

        Collections.sort(x);
        Collections.sort(y);

        mParticleSet.posX = x.get(mParticleSet.NUM_PARTICLES / 2);
        mParticleSet.posY = y.get(mParticleSet.NUM_PARTICLES / 2);

    }

    public boolean wallCollision(Particle particle, int x, int y)
    {
        if(!crossedWall(new Point(x,y), particle.getPosition()))
            return false;
        else
            return true;
    }

    private boolean crossedWall(Point new_point, Point old_point)
    {
        for (Line wall : mParticleSet.mWalls)
        {
            if (wall.intersectsWithLine(new_point, old_point))
                return true;
        }
        return false;
    }


    public void removeParticle(int i)
    {
        //Log.wtf(tag, "Remove Particle");
        mParticleSet.mParticles.remove(i);
    }
}
