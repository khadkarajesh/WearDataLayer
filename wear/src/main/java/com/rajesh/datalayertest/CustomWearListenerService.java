package com.rajesh.datalayertest;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by rajesh on 3/2/16.
 */
public class CustomWearListenerService extends WearableListenerService {
    private static final String TAG = CustomWearListenerService.class.getSimpleName();
    GoogleApiClient googleApiClient;
    private static final String IMAGE_PATH = "/image";
    private static final String IMAGE_TITLE = "imageTitle";
    private static final String IMAGE_TO_SEND = "image";

    @Override
    public void onCreate() {
        super.onCreate();
        googleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(@Nullable Bundle bundle) {

            }

            @Override
            public void onConnectionSuspended(int i) {

            }
        }).addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

            }
        }).build();
        googleApiClient.connect();

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);

        for (DataEvent dataEvent : dataEvents) {
            if (dataEvent.getDataItem().getUri().getPath().equalsIgnoreCase(IMAGE_PATH)) {
                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataEvent.getDataItem());
                String result = dataMapItem.getDataMap().getString(IMAGE_TITLE);
                Log.d(TAG, "onDataChanged: result ::" + result);
            }
        }

    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

    }
}
