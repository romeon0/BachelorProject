package com.romeon0;

import java.awt.*;
import java.util.Vector;

/**
 * Created by Romeon0 on 3/2/2018.
 */
public class Figure {
    private int mode=0;
    private Vector<Point> points;

    public Figure(int mode){
        this.mode = mode;
        points = new Vector<>();
    }
    void addPoint(int x, int y){
        Point p = new Point(x,y);
        points.add(p);
    }
    public Vector<Point> getPoints(){
        return points;
    }
    public int getMode(){return mode;}
}
