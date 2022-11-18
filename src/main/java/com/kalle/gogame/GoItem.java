package com.kalle.gogame;

import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.Arrays;
import java.util.List;

/**
 * 围棋棋子
 *
 * @author kalle
 * @since 2022-11-18
 */
public class GoItem extends Circle {

    /**
     * 允许设置的颜色值: BLACK | WHITE | null
     */
    private static final List<Color> ALLOW_COLORS = Arrays.asList(Color.BLACK, Color.WHITE, null);

    /**
     * 鼠标悬浮在棋子上显示的颜色状态机，透明度0.3
     */
    private static final Color[] HOVER_COLOR_STATUS_MACHINE = {
            Color.rgb(255, 255, 255, 0.3),
            Color.rgb(0, 0, 0, 0.3),
    };

    /**
     * 横、纵坐标
     */
    private final int x, y;

    /**
     * 棋子颜色，用于显示时的判断(白/黑)，隐藏时为null
     */
    private Color color;

    /**
     * 当前棋子的上下左右指针
     */
    private GoItem up, down, left, right;

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
        // 只允许设置3个值，BLACK|WHITE|null
        if (!ALLOW_COLORS.contains(color)) {
            throw new IllegalArgumentException("颜色值只允许为: BLACK | WHITE | null");
        }
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
