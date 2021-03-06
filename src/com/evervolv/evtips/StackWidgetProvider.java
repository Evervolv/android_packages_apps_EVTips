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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.evervolv.evtips.R;

public class StackWidgetProvider extends AppWidgetProvider {

    public static final String TAG = "EVTips";

    public static final String CLICK_ACTION = "com.evervolv.evtips.CLICK_ACTION";
    public static final String EXTRA_ITEM = "com.evervolv.evtips.EXTRA_ITEM";
    
    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(CLICK_ACTION)) {
            //Load the uri's for all the tips
            Resources res = context.getResources();
            String[] tipUri = res.getStringArray(R.array.tip_uri);
            int viewIndex = intent.getIntExtra(EXTRA_ITEM, 0);
            if (!tipUri[viewIndex].equals("null")) {
                //This is only going to catch a URISyntaxException, ActivityNotFound or any
                //others won't be handled. So we'll have to get it right.
                try {
                    Intent uriIntent = new Intent(Intent.parseUri(tipUri[viewIndex], Intent.URI_INTENT_SCHEME));
                    uriIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(uriIntent);
                } catch (java.net.URISyntaxException ex) {
                    Log.e(TAG, "Invalid intent: " + tipUri[viewIndex]);
                    ex.printStackTrace();
                }
            } else {
                Log.d(TAG, "Intent is [null], was this on purpose?");
            }
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // update each of the widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {
            // Here we setup the intent which points to the StackViewService which will
            // provide the views for this collection.
            Intent intent = new Intent(context, StackWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            // When intents are compared, the extras are ignored, so we need to embed the extras
            // into the data so that the extras will not be ignored.
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            rv.setRemoteAdapter(appWidgetIds[i], R.id.stack_view, intent);
            
            // The empty view is displayed when the collection has no items. It should be a sibling
            // of the collection view.
            rv.setEmptyView(R.id.stack_view, R.id.empty_view);

            // Here we setup the a pending intent template. Individuals items of a collection
            // cannot setup their own pending intents, instead, the collection as a whole can
            // setup a pending intent template, and the individual items can set a fillInIntent
            // to create unique before on an item to item basis.
            Intent clickIntent = new Intent(context, StackWidgetProvider.class);
            clickIntent.setAction(StackWidgetProvider.CLICK_ACTION);
            clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, clickIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.stack_view, toastPendingIntent);

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }
}
