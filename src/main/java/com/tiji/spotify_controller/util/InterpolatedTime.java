package com.tiji.spotify_controller.util;

import net.minecraft.Util;

public class InterpolatedTime {
    protected long interpolateStart;
    protected long startTime;

    public InterpolatedTime(long startTime) {
        resetTime(startTime);
    }

    public long getInterpolatedTime() {
        long elapsed = Util.getMillis() - interpolateStart;
        return startTime + elapsed;
    }

    public void resetTime(long startTime) {
        this.interpolateStart = Util.getMillis();
        this.startTime = startTime;
    }

    public static InterpolatedTime nonProgressing(int time) {
        return new InterpolatedTime(time) {
            @Override
            public long getInterpolatedTime() {
                return startTime;
            }
        };
    }

    public static InterpolatedTime nonProgressing() {
        return nonProgressing(0);
    }
}