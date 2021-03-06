package com.hankus.activitymonitoring;

import android.graphics.Point;

public class Line {

    public Point mStartPoint;
    public Point mEndPoint;

    public Line(Point start, Point end)
    {
        mStartPoint = start;
        mEndPoint = end;
    }

    public Line(int x_start, int y_start, int x_end, int y_end)
    {
        mStartPoint = new Point(x_start, y_start);
        mEndPoint = new Point(x_end, y_end);
    }

    /**
     * Tries to find an intersection pont between two lines.
     *
     * @param start start point of line
     * @param end end point of line
     * @return
     */
    //https://gist.github.com/coleww/9403691
    public boolean intersectsWithLine(Point start, Point end) {
        float bx = (float) (mEndPoint.x- mStartPoint.x);
        float by = (float) (mEndPoint.y - mStartPoint.y) + 2;
        float dx = end.x - start.x;
        float dy = end.y - start.y;
        float b_dot_d_perp = bx * dy - by * dx;
        if (b_dot_d_perp == 0) {
            return false;
        }
        float cx = start.x - mStartPoint.x;
        float cy = start.y - mStartPoint.y;
        float t = (cx * dy - cy * dx) / b_dot_d_perp;
        if (t < 0 || t > 1) {
            return false;
        }
        float u = (cx * by - cy * bx) / b_dot_d_perp;
        if (u < 0 || u > 1) {
            return false;
        }
        return true;
    }

}
