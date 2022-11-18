package com.kalle.gogame;

import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 * @author xiexincong
 * @since 2022/11/17
 */
public class GoItem extends Circle {

    // 鼠标悬浮在棋子上显示的颜色，透明度0.3

    private static final Color[] HOVER_COLOR_STATUS_MACHINE = {
            Color.rgb(255, 255, 255, 0.3),
            Color.rgb(0, 0, 0, 0.3),
    };
    
    private final int x;
    private final int y;

    private Color color;

    private GoItem up;
    private GoItem down;
    private GoItem left;
    private GoItem right;

    public GoItem(int x, int y, GoGame goGame) {
        this.x = x;
        this.y = y;
        // 设置事件
        this.setCursor(Cursor.HAND);
        
        // 鼠标移入，移出事件
        this.setOnMouseMoved(e -> {
            if (this.getColor() == null) {
                int currentStep = goGame.currentStep();
                int status = (currentStep + 1) % 2;
                this.setFill(HOVER_COLOR_STATUS_MACHINE[status]);
            }
        });
        this.setOnMouseExited(e -> {
            if (this.getColor() == null) {
                this.setFill(Color.TRANSPARENT);
            }
        });
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        // 设置棋子颜色
        if (color == null) {
            color = Color.TRANSPARENT;
        }
        this.setFill(color);
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
