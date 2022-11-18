package com.kalle.gogame;

import javafx.scene.paint.Color;

import java.util.Set;

/**
 * 落子记录
 *
 * @author kalle
 * @since 2022-11-18
 */
public class GoRecord {

    /**
     * 落子时的颜色
     */
    private final Color color;

    /**
     * 落子的棋，或者时杀子的棋
     */
    private final GoItem killer;

    /**
     * 被杀的棋子
     */
    private final Set<GoItem> killGoItems;

    public GoRecord(Color color, GoItem killer, Set<GoItem> killGoItems) {
        this.color = color;
        this.killer = killer;
        this.killGoItems = killGoItems;
    }

    public Color getColor() {
        return color;
    }

    public GoItem getKiller() {
        return killer;
    }

    public Set<GoItem> getKillGoItems() {
        return killGoItems;
    }
}
