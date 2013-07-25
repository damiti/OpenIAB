/*******************************************************************************
 * Copyright 2013 One Platform Foundation
 *
 *       Licensed under the Apache License, Version 2.0 (the "License");
 *       you may not use this file except in compliance with the License.
 *       You may obtain a copy of the License at
 *
 *           http://www.apache.org/licenses/LICENSE-2.0
 *
 *       Unless required by applicable law or agreed to in writing, software
 *       distributed under the License is distributed on an "AS IS" BASIS,
 *       WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *       See the License for the specific language governing permissions and
 *       limitations under the License.
 ******************************************************************************/

package org.onepf.oms.appstore;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import org.onepf.oms.*;

import java.util.List;

/**
 * Author: Ruslan Sayfutdinov
 * Date: 16.04.13
 */

public class GooglePlay extends DefaultAppstore {
    private Context mContext;
    private GooglePlayBillingService mBillingService;
    private String mPublicKey;
    private InformationState isBillingSupported = InformationState.UNDEFINED;
    private static final String TAG = "IabHelper";
    private static final String ANDROID_INSTALLER = "com.android.vending";
    private static final String GOOGLE_INSTALLER = "com.google.vending";

    private enum InformationState {
        UNDEFINED, SUPPORTED, UNSUPPORTED
    }

    // isDebugMode = true |-> always returns app installed via Google Play
    private final boolean isDebugMode = false;

    public GooglePlay(Context context, String publicKey) {
        mContext = context;
        mPublicKey = publicKey;
    }

    @Override
    public boolean isAppAvailable(String packageName) {
        return false;
    }

    @Override
    public boolean isInstaller(String packageName) {
        if (isDebugMode) {
            return true;
        }
        PackageManager packageManager = mContext.getPackageManager();
        String installerPackageName = packageManager.getInstallerPackageName(packageName);
        return (installerPackageName != null && installerPackageName.equals(ANDROID_INSTALLER));
    }

    @Override
    public Intent getServiceIntent(String packageName, int serviceType) {
        if (serviceType == 0) {
            return new Intent("com.android.vending.billing.InAppBillingService.BIND");
        }
        return null;
    }

    //TODO: update to match new appstore aidl
    public boolean isServiceSupported(int appstoreService) {
        if (appstoreService == OpenIabHelper.SERVICE_IN_APP_BILLING) {
            Log.d(TAG, "Check google if billing supported");
            if (isBillingSupported != InformationState.UNDEFINED) {
                return isBillingSupported == InformationState.SUPPORTED ? true : false;
            }
            PackageManager packageManager = mContext.getPackageManager();
            List<PackageInfo> allPackages = packageManager.getInstalledPackages(0);
            for (PackageInfo packageInfo : allPackages) {
                if (packageInfo.packageName.equals(GOOGLE_INSTALLER) || packageInfo.packageName.equals(ANDROID_INSTALLER)) {
                    isBillingSupported = InformationState.SUPPORTED;
                    Log.d(TAG, "Google supports billing");
                    return true;
                }
            }
            isBillingSupported = InformationState.UNSUPPORTED;
            return false;
        }
        return false;
    }

    @Override
    public AppstoreInAppBillingService getInAppBillingService() {
        if (mBillingService == null) {
            mBillingService = new GooglePlayBillingService(mContext, mPublicKey, this);
        }
        return mBillingService;
    }

    @Override
    public String getAppstoreName() {
        return OpenIabHelper.NAME_GOOGLE;
    }

}
