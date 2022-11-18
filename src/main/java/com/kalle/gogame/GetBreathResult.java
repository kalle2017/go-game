package com.kalle.gogame;

import java.util.List;

/**
 * 获取气的结果业务对象
 *
 * @author kalle
 * @since 2022-11-18
 */
public class GetBreathResult {

    /**
     * 气数
     */
    private final int breaths;

    /**
     * 棋子合集（一个整体）
     */
    private final List<GoItem> groupItems;

    public GetBreathResult(int breaths, List<GoItem> groupItems) {
        this.breaths = breaths;
        this.groupItems = groupItems;
    }

    public int getBreaths() {
        return breaths;
    }

    public List<GoItem> getGroupItems() {
        return groupItems;
    }
}
