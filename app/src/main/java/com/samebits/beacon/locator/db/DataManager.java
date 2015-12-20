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

package com.samebits.beacon.locator.db;

import android.content.Context;

import com.samebits.beacon.locator.BeaconLocatorApp;
import com.samebits.beacon.locator.injection.component.DaggerDataComponent;
import com.samebits.beacon.locator.injection.module.DataModule;
import com.samebits.beacon.locator.model.DetectedBeacon;
import com.samebits.beacon.locator.model.TrackedBeacon;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by vitas on 20/12/15.
 */
public class DataManager {

    @Inject
    protected StoreService mStoreService;

    public DataManager(Context context) {
        injectDependencies(context);
    }

    public DataManager(StoreService storeService) {
        mStoreService = storeService;
    }

    protected void injectDependencies(Context context) {
        DaggerDataComponent.builder()
                .applicationComponent(BeaconLocatorApp.from(context).getComponent())
                .dataModule(new DataModule(context))
                .build()
                .inject(this);
    }

    public boolean storeBeacon(TrackedBeacon beacon) {
        return mStoreService.createBeacon(beacon);
    }

    public TrackedBeacon getBeacon(String id) {
        return mStoreService.getBeacon(id);
    }


    public List<TrackedBeacon> getAllBeacons() {
        return mStoreService.getBeacons();
    }
}