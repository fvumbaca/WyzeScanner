package com.wyzelink.wyzescaner.fragments;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.wyzelink.wyzescaner.BeaconLog;
import com.wyzelink.wyzescaner.R;
import com.wyzelink.wyzescaner.ScannerApplication;
import com.wyzelink.wyzescaner.activities.PreferenceActivity;
import com.wyzelink.wyzescaner.misc.DividerItemDecoration;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Frank on 2015-10-21.
 */
public class ScanFragment extends Fragment {
    public static final String TAG = "ScanFragment";

    private static final String STATE_BEACON_MAP =
            "com.wyzelink.wyzescaner.fragment.scanfragment.beacon_map";

    private RecyclerView mRecyclerView;
    private Adapter mAdapter;
    private Handler mRefreshHandler;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        localBroadcastManager.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mAdapter.add((BeaconLog) intent.getExtras().get(ScannerApplication.EXTRA_BEACON_LOG));
            }
        }, new IntentFilter(ScannerApplication.ACTION_BEACON_UPDATE));

        mRefreshHandler = new Handler();
        mRefreshHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mAdapter != null)
                    mAdapter.notifyDataSetChanged();
                if (mRefreshHandler != null)
                    mRefreshHandler.postDelayed(this, 1000);
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_scan, container, false);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.fragment_scan_recycler);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL_LIST));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));


        mAdapter = new Adapter();
        if (savedInstanceState != null && savedInstanceState.containsKey(STATE_BEACON_MAP))
            mAdapter.setBeaconMap((HashMap<String, BeaconLog>) savedInstanceState.getSerializable(STATE_BEACON_MAP));
        mRecyclerView.setAdapter(mAdapter);
        return v;
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private HashMap<String, BeaconLog> mBeaconMap = new HashMap<>();
        private List<BeaconLog> mRows = new ArrayList<>();

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_beacon, parent, false));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final BeaconLog log = mRows.get(position);
            holder.mLocalIdTextView.setText(log.getLocalId() + "\n(" + Integer.parseInt(log.getLocalId(), 16) + ")");
            String lastSeen = getLastSeenString(log.getDateTime());
            if (lastSeen.isEmpty()) {
                holder.mJustSeenImageView.setVisibility(View.VISIBLE);
                holder.mLastSeenTextView.setText("");
            } else {
                holder.mJustSeenImageView.setVisibility(View.GONE);
                holder.mLastSeenTextView.setText(lastSeen);
            }

            final int minor = log.getBeacon().getId3().toInt();
            holder.mJobInterval0TextView.setText("" + getJob(minor, 0));
            holder.mJobInterval1TextView.setText("" + getJob(minor, 1));
            holder.mJobInterval2TextView.setText("" + getJob(minor, 2));

            holder.mRssiTextView.setText("rssi: " + log.getBeacon().getRssi());
        }

        private String getLastSeenString(DateTime lastSeen) {
            Period period = new Duration(lastSeen, DateTime.now()).toPeriod();
            PeriodFormatter minutesAndSeconds = new PeriodFormatterBuilder()
                    .appendDays()
                    .appendSuffix("day", "days")
                    .appendHours()
                    .appendSuffix("hour", "hours")
                    .appendMinutes()
                    .appendSuffix("min", "mins")
                    .appendSeconds()
                    .appendSuffix("sec", "secs")
                    .toFormatter();
            return minutesAndSeconds.print(period);
        }

        @Override
        public int getItemCount() {
            return mRows.size();
        }

        public void setBeaconMap(HashMap<String, BeaconLog> map) {
            mBeaconMap.clear();
            if (map == null)
                return;
            mBeaconMap.putAll(map);
            mRows.clear();
            mRows.addAll(mBeaconMap.values());
            notifyDataSetChanged();
        }

        public void add(BeaconLog log) {
            if (log == null) return;
            mBeaconMap.put(log.getLocalId(), log);
            mRows.clear();
            mRows.addAll(mBeaconMap.values());

            notifyDataSetChanged();
        }

        public void clear() {
            mBeaconMap.clear();
            mRows.clear();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView mLocalIdTextView;
            TextView mLastSeenTextView;
            ImageView mJustSeenImageView;
            TextView mJobInterval0TextView;
            TextView mJobInterval1TextView;
            TextView mJobInterval2TextView;
            TextView mRssiTextView;
            public ViewHolder(View v) {
                super(v);
                mLocalIdTextView = (TextView) v.findViewById(R.id.row_beacon_localid);
                mLastSeenTextView = (TextView) v.findViewById(R.id.row_beacon_lastseen);
                mJustSeenImageView = (ImageView) v.findViewById(R.id.row_beacon_justseen);
                mJobInterval0TextView = (TextView) v.findViewById(R.id.row_beacon_job_0);
                mJobInterval1TextView = (TextView) v.findViewById(R.id.row_beacon_job_1);
                mJobInterval2TextView = (TextView) v.findViewById(R.id.row_beacon_job_2);
                mRssiTextView = (TextView) v.findViewById(R.id.row_beacon_rssi);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(STATE_BEACON_MAP, mAdapter.mBeaconMap);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_scanner, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Application app = getActivity().getApplication();
        switch (item.getItemId()) {
            case R.id.action_scanner_start:
                if (app != null && app instanceof ScannerApplication)
                    ((ScannerApplication)app).startScanning();
                return true;
            case R.id.action_scanner_stop:
                if (app != null && app instanceof ScannerApplication)
                    ((ScannerApplication)app).stopScanning();
                return true;
            case R.id.action_scanner_settings:
                Intent intent = new Intent(getActivity(), PreferenceActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_scanner_clear:
                if (mAdapter != null)
                    mAdapter.clear();
                return true;
            case R.id.action_scanner_info:
                if (app != null
                        && app instanceof ScannerApplication)
                    ((ScannerApplication) app).printSettings();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRefreshHandler = null;
    }

    private int getJob(int minor, int jobNum) {
        int t = minor >> (5 * (3-jobNum));
        return t & 0b11111;
    }
}
