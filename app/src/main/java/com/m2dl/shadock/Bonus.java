package com.m2dl.shadock;

import java.util.Random;

public class Bonus extends GameElements {
    public Bonus(int x,int y){
        Random r = new Random();
        setX(r.nextInt(x));
        setY(y);
    }
}
