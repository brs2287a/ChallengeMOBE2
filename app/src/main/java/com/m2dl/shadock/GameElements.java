package com.m2dl.shadock;

public abstract class GameElements {
    private int x;
    private int y;

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }


    public boolean updatePosition(int height){
        boolean isHigher = false;
        if(getY()>height+50){
            isHigher = true;
        }
        y = y + 2;
        return isHigher;
    }
}
