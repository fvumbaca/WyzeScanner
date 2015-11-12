package com.wyzelink.wyzescaner;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;


import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.joda.time.DateTime;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Frank on 2015-10-22.
 */
public class ScannerApplication extends Application implements BeaconConsumer, RangeNotifier,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "ScannerApplication";
    private BeaconManager mBeaconManager;
    private Region mRegion;

    private boolean mBeaconServiceConnected = false;

    public static final String ACTION_BEACON_UPDATE =
            "beacon_update";

    public static final String EXTRA_BEACON_LOG =
            "beacon_info";

    public static final String UUID_FOB = "72636a69-d9c1-475d-99dc-641899af656e";
    public static final String UUID_ASSET = "86ad02d7-0097-4b13-8d97-16aa31e464d6";
    public static final String UUID_TRUCK = "6838324f-143f-49ff-aea6-6ac92dba1568";

    public void onCreate() {
        super.onCreate();
        mBeaconManager = BeaconManager.getInstanceForApplication(this);

        mRegion = new Region("backgroundRegion", Identifier.parse(UUID_FOB), null, null);
        mBeaconManager.getBeaconParsers().add(new BeaconParser()
                .setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        int scan_period = Integer.parseInt(sharedPref.getString("time_scan", "1000"));
        int sleep_period = Integer.parseInt(sharedPref.getString("time_sleep", "1000"));


        mBeaconManager.setForegroundScanPeriod(scan_period);
        mBeaconManager.setForegroundBetweenScanPeriod(sleep_period);
        mBeaconManager.setBackgroundScanPeriod(scan_period);
        mBeaconManager.setBackgroundBetweenScanPeriod(sleep_period);
        mBeaconManager.setAndroidLScanningDisabled(sharedPref.getBoolean("scan_android_l_defaults", false));


        mBeaconManager.bind(this);

    }

    @Override
    public void onBeaconServiceConnect() {
        mBeaconServiceConnected = true;
        Log.i(TAG, "onBeaconServiceConnect");
        mBeaconManager.setRangeNotifier(this);
    }

    @Override
    public void didRangeBeaconsInRegion(Collection<Beacon> collection, Region region) {
        Iterator<Beacon> iterator = collection.iterator();
        LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(this);
        while (iterator.hasNext()) {
            Beacon beacon = iterator.next();

            BeaconLog log = new BeaconLog();
            log.setBeacon(beacon);
            log.setLocalId(getLocalId(beacon.getId2().toInt()));
            log.setDateTime(DateTime.now());

            Intent intent = new Intent(ACTION_BEACON_UPDATE);
            intent.putExtra(EXTRA_BEACON_LOG, log);
            broadcastManager.sendBroadcast(intent);
            Log.d(TAG, "Beacon: " + getLocalId(beacon.getId2().toInt()) + "   " +beacon);
        }
    }

    public String getLocalId(int major) {
        int t = (major >> 1) & 0b111111111111;
        return Integer.toHexString(t);
    }

    public void startScanning() {
        if (mBeaconServiceConnected) {
            try {
                mBeaconManager.startRangingBeaconsInRegion(mRegion);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopScanning() {
        try {
            mBeaconManager.stopRangingBeaconsInRegion(mRegion);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String prefName) {
        switch (prefName) {
            case "beacon_uuid":
                break;
            case "time_scan":
                int scan_period = Integer.parseInt(sharedPreferences.getString("time_scan", "1000"));
                mBeaconManager.setForegroundScanPeriod(scan_period);
                mBeaconManager.setBackgroundScanPeriod(scan_period);
                try {
                    mBeaconManager.updateScanPeriods();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case "time_sleep":
                int sleep_period = Integer.parseInt(sharedPreferences.getString("time_scan", "1000"));
                mBeaconManager.setForegroundBetweenScanPeriod(sleep_period);
                mBeaconManager.setBackgroundBetweenScanPeriod(sleep_period);
                try {
                    mBeaconManager.updateScanPeriods();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case "scan_android_l_default":
                mBeaconManager.setAndroidLScanningDisabled(sharedPreferences.getBoolean("scan_android_l_default", false));
                try {
                    mBeaconManager.updateScanPeriods();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;

        }
    }

    public void printSettings() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean l_settings = sharedPref.getBoolean("scan_android_l_defaults", false);
        int scan_period = Integer.parseInt(sharedPref.getString("time_scan", "1000"));
        int sleep_period = Integer.parseInt(sharedPref.getString("time_sleep", "1000"));

        if (!l_settings){
            Toast.makeText(getApplicationContext(), "Settings set to android L defaults", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Settings Scan: " + scan_period + " Sleep: " + sleep_period, Toast.LENGTH_SHORT).show();
        }
    }
}
