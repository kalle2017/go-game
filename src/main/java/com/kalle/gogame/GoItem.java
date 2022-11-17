package com.kalle.gogame;

import javafx.scene.shape.Circle;

/**
 * @author xiexincong
 * @since 2022/11/17
 */
public class GoItem extends Circle {

    private final int x;
    private final int y;

    private GoColor color;

    private GoItem up;
    private GoItem down;
    private GoItem left;
    private GoItem right;

    public GoItem(int x, int y, int road) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public GoColor getColor() {
        return color;
    }

    public void setColor(GoColor color) {
        this.color = color;
    }

    public GoItem getUp() {
        return up;
    }

    public void setUp(GoItem up) {
        this.up = up;
    }

    public GoItem getDown() {
        return down;
    }

    public void setDown(GoItem down) {
        this.down = down;
    }

    public GoItem getLeft() {
        return left;
    }

    public void setLeft(GoItem left) {
        this.left = left;
    }

    public GoItem getRight() {
        return right;
    }

    public void setRight(GoItem right) {
        this.right = right;
    }
}
