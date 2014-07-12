package nl.basst.wearweer;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.wearable.Asset;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.util.Random;
import java.net.*;
import com.google.android.gms.wearable.DataMap;

public class MobileActivity extends Activity {

    private static final String TAG = MobileActivity.class.getSimpleName();
    private GoogleApiClient apiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mobile, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        makeConnection();
    }

    private void makeConnection() {
        if (apiClient != null && apiClient.isConnected()) {

        }
        else {

            apiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(new ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle connectionHint) {
                            Log.d(TAG, "onConnected: " + connectionHint);

                            sendImageToWear("http://www.basst.nl/Develop/Android/Android-Wear.jpg");
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
    }

    private void sendImageToWear(final String URL) {

        Thread thread = new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    PutDataMapRequest request = PutDataMapRequest.create("/image");
                    DataMap map = request.getDataMap();
                    URL url = new URL(URL);
                    Bitmap bmp = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                    Asset asset = createAssetFromBitmap(bmp);
                    Random randomGenerator = new Random();
                    int randomInt = randomGenerator.nextInt(1000);
                    map.putInt("Integer", randomInt);
                    map.putAsset("profileImage", asset);
                    Wearable.DataApi.putDataItem(apiClient, request.asPutDataRequest());
                } catch (Exception e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        });
        thread.start();

        Log.d(TAG, "Image send");
    }

    private static Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }

}
