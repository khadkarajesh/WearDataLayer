package com.rajesh.datalayertest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, DataApi.DataListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private TextView mTextView;
    private ImageView imageView;
    GoogleApiClient googleApiClient;
    private static final String IMAGE_PATH = "/image";
    private static final String IMAGE_TO_SEND = "image";
    private static final String IMAGE_TITLE = "imageTitle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createGoogleApiClient();
        setContentView(R.layout.activity_main);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                imageView = (ImageView) stub.findViewById(R.id.imageView);
            }
        });

        getData();

    }

    private void getData() {
        Wearable.NodeApi.getLocalNode(googleApiClient).setResultCallback(new ResultCallback<NodeApi.GetLocalNodeResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetLocalNodeResult getLocalNodeResult) {
                Log.d(TAG, "onResult: node id " + getLocalNodeResult.getNode().getId());
                Uri uri = new Uri.Builder()
                        .scheme(PutDataRequest.WEAR_URI_SCHEME)
                        .path(IMAGE_PATH)
                        .authority(getLocalNodeResult.getNode().getId())
                        .build();

                Log.d(TAG, "onResult: uri " + uri.toString());

                Wearable.DataApi.getDataItem(googleApiClient, uri).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                    @Override
                    public void onResult(@NonNull DataApi.DataItemResult dataItemResult) {
                        if (dataItemResult.getStatus().isSuccess() && dataItemResult.getDataItem() != null) {
                            Log.d(TAG, "onResult: success result");
                            DataMap dataMap = DataMap.fromByteArray(dataItemResult.getDataItem().getData());
                            Asset asset = dataMap.getAsset(IMAGE_TO_SEND);
                            new LoadBitmapAsyncTask().execute(asset);
                        } else {
                            Log.d(TAG, "onResult: failed     result");
                        }
                    }
                });
            }
        });
    }

    private void createGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Wearable.DataApi.addListener(googleApiClient, this);
        getData();
        //Wearable.NodeApi.addListener(googleApiClient,this);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        Log.d(TAG, "onDataChanged: reached");
        final ArrayList<DataEvent> events = FreezableUtils.freezeIterable(dataEventBuffer);
        for (DataEvent event : events) {
            if (event.getDataItem().getUri().getPath().equalsIgnoreCase(IMAGE_PATH)) {
                DataItem dataItem = event.getDataItem();
                DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItem);
                String result = dataMapItem.getDataMap().getString(IMAGE_TITLE);
                Asset asset = dataMapItem.getDataMap().getAsset(IMAGE_TO_SEND);
                new LoadBitmapAsyncTask().execute(asset);
                mTextView.setText(result);
                Log.d(TAG, "onDataChanged: result found :: " + result);
            }
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }


    /*
    * Extracts {@link android.graphics.Bitmap} data from the
    * {@link com.google.android.gms.wearable.Asset}
    */
    private class LoadBitmapAsyncTask extends AsyncTask<Asset, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Asset... params) {

            if (params.length > 0) {

                Asset asset = params[0];

                InputStream assetInputStream = Wearable.DataApi.getFdForAsset(
                        googleApiClient, asset).await().getInputStream();

                if (assetInputStream == null) {
                    Log.w(TAG, "Requested an unknown Asset.");
                    return null;
                }
                return BitmapFactory.decodeStream(assetInputStream);

            } else {
                Log.e(TAG, "Asset must be non-null");
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            Log.d(TAG, "onPostExecute: bitmap null");
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
                Log.d(TAG, "onPostExecute: bitmap not null");
            }
        }
    }
}
