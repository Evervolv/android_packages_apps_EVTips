/*
 * Copyright (C) 2011 The Android Open Source Project
 * Copyright (C) 2011 The Evervolv Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evervolv.evtips;

import java.util.ArrayList;
import java.util.List;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.evervolv.evtips.R;

public class StackWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new StackRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class StackRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    
    public static final String TAG = "EVTips";

    private int mCount;
    private List<TipItem> mTipItems = new ArrayList<TipItem>();
    private int[] mIconResourceIds;
    private String[] mTipText;
    private Context mContext;
    private int mAppWidgetId;
    private String[] mTipUri;

    public StackRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    public void onCreate() {
        
        Resources res = mContext.getResources();
        TypedArray ar = res.obtainTypedArray(R.array.tip_icons);
        mTipText = res.getStringArray(R.array.tip_entries);
        mTipUri = res.getStringArray(R.array.tip_uri);
        mCount = mTipText.length; 
        mIconResourceIds = new int[mCount];
        
        for (int i = 0; i < mCount; i++) {
            mIconResourceIds[i] = ar.getResourceId(i, 0);
            // If the Icon's resource Id does not exist, use a default. 
            // Although this shoud not occur.
            if (i >= (mCount - 1)) {
                mTipItems.add(new TipItem(mTipText[i], R.drawable.default_icon, i + 1));
            } else {
                mTipItems.add(new TipItem(mTipText[i], mIconResourceIds[i], i + 1));
            }
            
        }

        // We sleep for 3 seconds here to show how the empty view appears in the interim.
        // The empty view is set in the StackWidgetProvider and should be a sibling of the
        // collection view.
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void onDestroy() {
        mTipItems.clear();
    }

    public int getCount() {
        return mCount;
    }

    public RemoteViews getViewAt(int position) {
        // position will always range from 0 to getCount() - 1.

        RemoteViews rv = new RemoteViews(mContext.getPackageName(),
                R.layout.widget_item);
        rv.setTextViewText(R.id.widget_item, mTipItems.get(position).mText);
        rv.setImageViewResource(R.id.widget_icon, mTipItems.get(position).mIconId);
        rv.setTextViewText(R.id.widget_count, String.format(mContext.getString(
                R.string.item_counts, mTipItems.get(position).mIndex, mCount)));
        
        Bundle extras = new Bundle();
        extras.putInt(StackWidgetProvider.EXTRA_ITEM, position);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.widget_item, fillInIntent);

        // You can do heaving lifting in here, synchronously. For example, if you need to
        // process an image, fetch something from the network, etc., it is ok to do it here,
        // synchronously. A loading view will show up in lieu of the actual contents in the
        // interim.
        try {
            System.out.println("Loading view " + position);
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return rv;
    }

    public RemoteViews getLoadingView() {
        return null;
    }

    public int getViewTypeCount() {
        return 1;
    }

    public long getItemId(int position) {
        return position;
    }

    public boolean hasStableIds() {
        return true;
    }

    public void onDataSetChanged() {
        // This is triggered when you call AppWidgetManager notifyAppWidgetViewDataChanged
        // on the collection view corresponding to this factory. You can do heaving lifting in
        // here, synchronously. For example, if you need to process an image, fetch something
        // from the network, etc., it is ok to do it here, synchronously. The widget will remain
        // in its current state while work is being done here, so you don't need to worry about
        // locking up the widget.
    }
}
