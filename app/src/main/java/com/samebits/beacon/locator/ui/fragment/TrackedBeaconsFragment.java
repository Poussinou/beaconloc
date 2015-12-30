/*
 *
 *  Copyright (c) 2015 SameBits UG. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.samebits.beacon.locator.ui.fragment;


import android.content.Intent;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ProgressBar;

import com.samebits.beacon.locator.BeaconLocatorApp;
import com.samebits.beacon.locator.R;
import com.samebits.beacon.locator.data.DataManager;
import com.samebits.beacon.locator.model.ActionBeacon;
import com.samebits.beacon.locator.model.IManagedBeacon;
import com.samebits.beacon.locator.model.TrackedBeacon;
import com.samebits.beacon.locator.ui.adapter.TrackedBeaconAdapter;
import com.samebits.beacon.locator.util.Constants;
import com.samebits.beacon.locator.util.PreferencesUtil;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by vitas on 9/11/15.
 */
public class TrackedBeaconsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    public static final float ALPHA_FULL = 1.0f;
    @Bind(R.id.recycler_beacons)
    RecyclerView mListBeacons;
    @Bind(R.id.progress_indicator)
    ProgressBar mProgressBar;
    @Bind(R.id.empty_view)
    ViewStub mEmpty;
    EmptyView mEmptyView;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    private TrackedBeaconAdapter mBeaconsAdapter;
    private DataManager mDataManager;

    public static TrackedBeaconsFragment newInstance() {
        TrackedBeaconsFragment beaconsFragment = new TrackedBeaconsFragment();
        return beaconsFragment;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQ_UPDATED_ACTION_BEACON) {
            if (data != null && data.hasExtra(Constants.ARG_ACTION_BEACON)) {
                ActionBeacon actionBeacon = data.getParcelableExtra(Constants.ARG_ACTION_BEACON);
                if (actionBeacon != null) {
                    mBeaconsAdapter.updateBeaconAction(actionBeacon);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBeaconsAdapter = new TrackedBeaconAdapter(this);
        mDataManager = BeaconLocatorApp.from(getActivity()).getComponent().dataManager();
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_tracked_beacons, container, false);
        ButterKnife.bind(this, fragmentView);

        setupToolbar();
        setupRecyclerView();

        //setupSwipe();
        loadBeacons();

        return fragmentView;
    }

    private void loadBeacons() {
        showLoadingViews();

        mBeaconsAdapter.insertBeacons(mDataManager.getAllBeacons());

        getExtras();

        emptyListUpdate();
        hideLoadingViews();
    }

    private void getExtras() {
        if (getArguments() != null && !getArguments().isEmpty()) {

            TrackedBeacon beacon = getArguments().getParcelable(Constants.ARG_BEACON);
            if (beacon != null) {
                if (!mBeaconsAdapter.isItemExists(beacon.getId())) {
                    if (mDataManager.createBeacon(beacon)) {
                        mBeaconsAdapter.insertBeacon(beacon);
                    } else {
                        //TODO
                    }
                } else {
                    //FIXME make selected somehow!
                    if (mDataManager.updateBeacon(beacon)) {
                        mBeaconsAdapter.insertBeacon(beacon);
                    } else {
                        //TODO
                    }
                }
            }
        }

    }

    public void removeBeacon(String beaconId) {
        if (mDataManager.deleteBeacon(beaconId)) {
            mBeaconsAdapter.removeBeaconById(beaconId);
            emptyListUpdate();
        } else {
            //TODO error
        }
    }

    public void removeBeaconAction(String beaconId, int id) {
        if (mDataManager.deleteActionBeacon(id)) {
            mBeaconsAdapter.removeBeaconAction(beaconId, id);
        } else {
            //TODO error
        }
    }

    public void newBeaconAction(String beaconId) {
        ActionBeacon newAction = new ActionBeacon(beaconId, PreferencesUtil.getDefaultRegionName(getActivity()));
        if (mDataManager.createBeaconAction(newAction)) {
            mBeaconsAdapter.addBeaconAction(newAction);
        } else {
            //TODO error
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ButterKnife.unbind(this);
    }

    private void setupToolbar() {
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.title_fragment_tracked_beacons);
        }
    }

    private void setupRecyclerView() {
        View viewFromEmpty = mEmpty.inflate();
        mEmptyView = new EmptyView(viewFromEmpty);
        mEmptyView.text.setText(getString(R.string.text_empty_list_tracked_beacons));

        mListBeacons.setLayoutManager(new LinearLayoutManager(getActivity()));
        mListBeacons.setHasFixedSize(true);
        mProgressBar.setVisibility(View.GONE);
        mListBeacons.setAdapter(mBeaconsAdapter);

    }

    private void setupSwipe() {

        ItemTouchHelper swipeToDismissTouchHelper = new ItemTouchHelper(new UndoSwipableCallback(
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT));
        swipeToDismissTouchHelper.attachToRecyclerView(mListBeacons);
    }

    private void emptyListUpdate() {
        if (mBeaconsAdapter.getItemCount() == 0) {
            mEmpty.setVisibility(View.VISIBLE);
            mEmptyView.text.setText(getString(R.string.text_empty_list_tracked_beacons));
        } else {
            mEmpty.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRefresh() {
        loadBeacons();
    }

    private void hideLoadingViews() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
    }

    private void showLoadingViews() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }


    class UndoSwipableCallback extends ItemTouchHelper.SimpleCallback {

        public UndoSwipableCallback(int dragDirs, int swipeDirs) {
            super(dragDirs, swipeDirs);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            mBeaconsAdapter.removeBeacon(viewHolder.getAdapterPosition());
        }

    }


}