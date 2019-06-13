package com.hankus.activitymonitoring;

import android.graphics.Point;

public class Line {

    public Point startPoint;
    public Point endPoint;

    public Line(Point start, Point end)
    {
        startPoint = start;
        endPoint = end;
    }

    public Line(int x_start, int y_start, int x_end, int y_end)
    {
        startPoint = new Point(x_start, y_start);
        endPoint = new Point(x_end, y_end);
    }

    //https://gist.github.com/coleww/9403691
    public boolean intersectsWithLine(Point start, Point end) {
        float bx = (float) (endPoint.x - startPoint.x);
        float by = (float) (endPoint.y - startPoint.y);
        float dx = end.x - start.x;
        float dy = end.y - start.y;
        float b_dot_d_perp = bx * dy - by * dx;
        if (b_dot_d_perp == 0) {
            return false;
        }
        float cx = start.x - startPoint.x;
        float cy = start.y - startPoint.y;
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
