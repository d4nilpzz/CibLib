package dev.d4nilpzz.cib.client;

import dev.d4nilpzz.cib.api.Banner;
import java.util.HashMap;
import java.util.Map;

public final class HoverAnimations {

    private final Map<Integer, Long> progress = new HashMap<>();

    private int hoveredRow = -1;

    private long hoveredSince;

    public void clear() {
        this.progress.clear();
        this.hoveredRow = -1;
    }

    public void hover(int row, long now) {
        if (row == this.hoveredRow) {
            return;
        }
        if (this.hoveredRow != -1) {
            this.progress.put(this.hoveredRow, this.elapsed(this.hoveredRow, now));
        }
        this.hoveredRow = row;
        this.hoveredSince = now;
    }

    public int frame(int row, Banner banner, long now) {
        return banner.frameAt(this.elapsed(row, now));
    }

    private long elapsed(int row, long now) {
        long stored = this.progress.getOrDefault(row, 0L);
        return row == this.hoveredRow ? stored + now - this.hoveredSince : stored;
    }
}
