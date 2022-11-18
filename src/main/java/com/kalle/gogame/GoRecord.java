package com.kalle.gogame;

import javafx.scene.paint.Color;

import java.util.Set;

/**
 * 落子记录
 *
 * @author xiexincong
 * @since 2022/11/18
 */
public class GoRecord {

    private final Color color;

    private final GoItem killer;

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
