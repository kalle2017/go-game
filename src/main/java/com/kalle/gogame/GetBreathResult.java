package com.kalle.gogame;

import java.util.List;

public class GetBreathResult {

    private final int breaths;

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
