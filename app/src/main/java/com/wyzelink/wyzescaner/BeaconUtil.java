package com.wyzelink.wyzescaner;

/**
 * Created by Frank on 2015-10-22.
 */
public class BeaconUtil {

    public static String getLocalId(int major) {
        int t = (major >> 1) & 0b111111111111;
        return Integer.toHexString(t);
    }

}
