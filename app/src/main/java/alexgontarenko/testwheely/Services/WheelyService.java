package alexgontarenko.testwheely.Services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import alexgontarenko.testwheely.Fragments.MapFragment;
import alexgontarenko.testwheely.MainActivity;
import alexgontarenko.testwheely.R;
import de.tavendo.autobahn.WebSocket;
import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketConnectionHandler;
import de.tavendo.autobahn.WebSocketException;

/**
 * Created by Alex on 30.06.2014.
 */
public class WheelyService extends Service {

    private final String LOG_TAG = "WheelyService";

    public static final String USERNAME_TAG="Wheely_USERNAME_TAG";
    public static final String PASSWORD_TAG="Wheely_PASSWORD_TAG";

    private LocationManager _locationManager;
    private Location _location;
    private static boolean _isStarted = false;

    private String _username, _password, _URL;

    private final WebSocketConnection _socketConnection = new WebSocketConnection();

    public static boolean isStarted(){
        return _isStarted;
    }

    private WebSocketConnectionHandler handler = new WebSocketConnectionHandler() {

        @Override
        public void onOpen() {
            Log.d(LOG_TAG, "Status: Connected to " + _URL);
            StringBuilder builder = new StringBuilder();
            builder.append("{\"lat\":");
            builder.append(_location.getLatitude());
            builder.append(",\"lon\":");
            builder.append(_location.getLongitude());
            builder.append("}");
            _socketConnection.sendTextMessage(builder.toString());
        }

        @Override
        public void onTextMessage(String payload) {
            Log.d(LOG_TAG, "Got echo: " + payload);
            Intent intent = new Intent(MapFragment.BROADCAST_ACTION);
            intent.putExtra(MapFragment.PARAM_STATUS, MapFragment.STATUS_START);
            intent.putExtra(MapFragment.PARAM_RESULT, payload);
            sendBroadcast(intent);
        }

        @Override
        public void onClose(int code, String result) {
            Log.d(LOG_TAG, "onClose: code " + code);
            if(code== WebSocket.ConnectionHandler.CLOSE_RECONNECT)
                start();
            else {
                Intent intent = new Intent(MapFragment.BROADCAST_ACTION);
                intent.putExtra(MapFragment.PARAM_STATUS, MapFragment.STATUS_FINISH);
                sendBroadcast(intent);
                stopSelf();
            }
        }
    };


    public void onCreate() {
        super.onCreate();
        _locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 60, 30, _locationListener);
        _locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 60, 30, _locationListener);
        Criteria criteria = new Criteria();
        String provider = _locationManager.getBestProvider(criteria, false);
        _location = _locationManager.getLastKnownLocation(provider);
        _isStarted=true;
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Bundle bundle = intent.getExtras();
        _username = bundle.getString(USERNAME_TAG);
        _password = bundle.getString(PASSWORD_TAG);
        start();
        Notification note=new Notification(R.drawable.ic_launcher,"TestWheely", System.currentTimeMillis());
        Intent noteIntent=new Intent(this, MainActivity.class);

        noteIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendeingIntent=PendingIntent.getActivity(this, 0, noteIntent, 0);

        note.setLatestEventInfo(this, "TestWheely", "TestWheely!!!", pendeingIntent);
        note.flags|=Notification.FLAG_NO_CLEAR;

        startForeground(startId, note);
        return START_REDELIVER_INTENT;
    }

    public void onDestroy() {
        _locationManager.removeUpdates(_locationListener);
        _socketConnection.disconnect();
        _isStarted=false;
        stopForeground(true);
        super.onDestroy();
    }


    public IBinder onBind(Intent intent) {
        return null;
    }

    private LocationListener _locationListener = new LocationListener() {

        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                _location = location;
                _socketConnection.disconnect();
                start();
            }
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };

    private void start(){
        StringBuilder builder = new StringBuilder();
        builder.append("ws://mini-mdt.wheely.com?username=");
        builder.append(_username);
        builder.append("&password=");
        builder.append(_password);
        _URL = builder.toString();
        try {
            _socketConnection.connect(_URL, handler);
        } catch (WebSocketException e) {
            Log.e(LOG_TAG, e.toString());
        }
    }
}