package com.hankus.activitymonitoring;

import android.graphics.Point;
import java.util.ArrayList;

public class Walls {

    private ArrayList<Line> mWalls;
    private ArrayList<Line> mScaledWalls;

    public Walls()
    {
        mWalls = new ArrayList<>();
        mScaledWalls = new ArrayList<Line>();

        Line Wall1 = new Line( new Point(2, 105), new Point( 154, 105));
        Line Wall2 = new Line( new Point(154, 105), new Point( 154, 25));
        Line Wall3 = new Line( new Point(154, 25), new Point( 176, 25));
        Line Wall4 = new Line( new Point(176, 25), new Point( 176, 105));
        Line Wall5 = new Line( new Point(176, 105), new Point( 402, 105));
        Line Wall6 = new Line( new Point(402, 105), new Point( 402, 66));
        Line Wall7 = new Line( new Point(418, 66), new Point( 402, 66));
        Line Wall8 = new Line( new Point(418, 66), new Point( 418, 25));
        Line Wall9 = new Line( new Point(438, 66), new Point( 418, 25));
        Line Wall10 = new Line( new Point(438, 25), new Point( 438, 161));
        Line Wall11 = new Line( new Point(419, 161), new Point( 438, 161));
        Line Wall12 = new Line( new Point(419, 161), new Point( 419, 119));
        Line Wall13 = new Line( new Point(176, 119), new Point( 419, 119));
        Line Wall14 = new Line( new Point(176, 119), new Point( 176, 161));
        Line Wall15 = new Line( new Point(155, 161), new Point( 176, 161));
        Line Wall16 = new Line( new Point(155, 161), new Point( 155, 119));
        Line Wall17 = new Line( new Point(86, 119), new Point( 155, 119));
        Line Wall18 = new Line( new Point(86, 119), new Point( 86, 161));
        Line Wall19 = new Line( new Point(2, 161), new Point( 86, 161));
        Line Wall20 = new Line( new Point(2, 161), new Point( 437, 161));
        Line Wall21 = new Line( new Point(2, 161), new Point( 2, 105));
        Line Wall22 = new Line( new Point(221, 105), new Point(221, 119));

        //Tables
        Line Wall23 = new Line( new Point(21, 161), new Point( 21, 121));
        Line Wall24 = new Line( new Point(34, 161), new Point( 34, 121));
        Line Wall25 = new Line( new Point(21, 121), new Point( 34, 121));
        Line Wall26 = new Line( new Point(56, 161), new Point( 56, 121));
        Line Wall27 = new Line( new Point(70,  161), new Point( 70, 121));
        Line Wall28 = new Line( new Point(56, 121), new Point( 70, 121));


        mWalls.add(Wall1);
        mWalls.add(Wall2);
        mWalls.add(Wall3);
        mWalls.add(Wall4);
        mWalls.add(Wall5);
        mWalls.add(Wall6);
        mWalls.add(Wall7);
        mWalls.add(Wall8);
        mWalls.add(Wall9);
        mWalls.add(Wall10);
        mWalls.add(Wall11);
        mWalls.add(Wall12);
        mWalls.add(Wall13);
        mWalls.add(Wall14);
        mWalls.add(Wall15);
        mWalls.add(Wall16);
        mWalls.add(Wall17);
        mWalls.add(Wall18);
        mWalls.add(Wall19);
        mWalls.add(Wall20);
        mWalls.add(Wall21);
        mWalls.add(Wall22);
        mWalls.add(Wall23);
        mWalls.add(Wall24);
        mWalls.add(Wall25);
        mWalls.add(Wall26);
        mWalls.add(Wall27);
        mWalls.add(Wall28);

    }


    public ArrayList<Line> getScaledWalls()
    {
        return mScaledWalls;
    }

    public void scaleWalls(float scale_x, float scale_y)
    {
        mScaledWalls.clear();
        for (Line w: mWalls
        ) {
            mScaledWalls.add(new Line((int)(w.mStartPoint.x * scale_x), (int)(w.mStartPoint.y * scale_y),
                    (int)(w.mEndPoint.x * scale_x), (int)(w.mEndPoint.y * scale_y)));
        }
    }
}
