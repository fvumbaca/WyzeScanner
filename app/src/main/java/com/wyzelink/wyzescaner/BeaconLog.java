package com.wyzelink.wyzescaner;

import org.altbeacon.beacon.Beacon;
import org.joda.time.DateTime;

import java.io.Serializable;

/**
 * Created by Frank on 2015-10-22.
 */
public class BeaconLog implements Serializable {
    private DateTime mDateTime;
    private Beacon mBeacon;
    private String mLocalId;


    public String getLocalId() {
        return mLocalId;
    }

    public void setLocalId(String localId) {
        mLocalId = localId;
    }

    public Beacon getBeacon() {
        return mBeacon;
    }

    public void setBeacon(Beacon beacon) {
        mBeacon = beacon;
    }

    public DateTime getDateTime() {
        return mDateTime;
    }

    public void setDateTime(DateTime dateTime) {
        mDateTime = dateTime;
    }
}
