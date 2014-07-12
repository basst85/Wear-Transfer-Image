package nl.basst.wearweer;

import android.os.Bundle;
import android.app.Activity;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;
import android.widget.ImageView;
import android.os.Handler;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.wearable.Asset;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

public class WearActivity  extends Activity {

    private TextView mTextView;
    private ImageView imageView;
    private static final String TAG = WearActivity.class.getSimpleName();
    private GoogleApiClient apiClient;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);

        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
            mTextView = (TextView) stub.findViewById(R.id.text);
            imageView = (ImageView) stub.findViewById(R.id.imageView1);
            }
        });

        apiClient = new GoogleApiClient.Builder(this)
            .addConnectionCallbacks(new ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle connectionHint) {
                    Log.d(TAG, "onConnected: " + connectionHint);
                    Wearable.DataApi.addListener(apiClient, onDataChangedListener);
                }
                @Override
                public void onConnectionSuspended(int cause) {
                    Log.d(TAG, "onConnectionSuspended: " + cause);
                }
            })
            .addOnConnectionFailedListener(new OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        Log.d(TAG, "onConnectionFailed: " + result);
                }
                })
            .addApi(Wearable.API)
            .build();

        apiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    public DataApi.DataListener onDataChangedListener = new DataApi.DataListener() {
        @Override
        public void onDataChanged(DataEventBuffer dataEvents) {
        Log.d(TAG, "Data changed: " + dataEvents);
        for (DataEvent event : dataEvents) {
            Log.d(TAG, "Data received: " + event.getDataItem().getUri());

            if (event.getType() == DataEvent.TYPE_CHANGED &&
                event.getDataItem().getUri().getPath().equals("/image")) {
                    DataMapItem dataMapItem = DataMapItem.fromDataItem(event.getDataItem());
                    Asset profileAsset = dataMapItem.getDataMap().getAsset("profileImage");
                    Bitmap bitmap = loadBitmapFromAsset(profileAsset);
                    // Do something with the bitmap
                    handler.post(onNewBitmap(bitmap));
                }
            }
        }
    };

    private Runnable onNewBitmap(final Bitmap bitmap) {
        return new Runnable() {
            @Override
            public void run() {
                if (imageView != null) {
                    // Show received image in ImageView
                    imageView.setImageBitmap(bitmap);
                }
            }
        };
    }

    public Bitmap loadBitmapFromAsset(Asset asset) {
        if (asset == null) {
            throw new IllegalArgumentException("Asset must be non-null");
        }
        ConnectionResult result = apiClient.blockingConnect(3000, TimeUnit.MILLISECONDS);
        if (!result.isSuccess()) {
            return null;
        }
        // Convert asset into a file descriptor and block until it's ready
        InputStream assetInputStream = Wearable.DataApi.getFdForAsset(apiClient, asset).await().getInputStream();
        apiClient.disconnect();

        if (assetInputStream == null) {
            Log.w(TAG, "Requested an unknown Asset.");
            return null;
        }
        // Decode the stream into a bitmap
        return BitmapFactory.decodeStream(assetInputStream);
    }


}