package com.m2dl.shadock;

import java.util.Random;

public class Ennemy {
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

    public Ennemy(int x){
        setX(x);
        setY(0);
    }

    public void updatePosition(int height){
        if(y<height){
            y = y +1;
        }
    }
}
