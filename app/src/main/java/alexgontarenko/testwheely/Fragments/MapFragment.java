package alexgontarenko.testwheely.Fragments;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import alexgontarenko.testwheely.R;

/**
 * Created by Alex on 29.06.2014.
 */
public class MapFragment extends Fragment implements View.OnClickListener {

    public final static int STATUS_START = 1;
    public final static int STATUS_FINISH = 2;
    public final static String PARAM_RESULT = "result";
    public final static String PARAM_STATUS = "status";

    public final static String BROADCAST_ACTION = "alexgontarenko.testwheely.servicebackbroadcast";
    private final static String LOG_TAG = "MapFragment";

    private FrameLayout _loadingDialog;
    private MapView _map;
    private GoogleMap _googleMap;
    private OnDisconnectListner _listner;
    private BroadcastReceiver _receiver;

    public interface OnDisconnectListner{
        public abstract void onDisconnect();
    }

    public void setOnDisconnectListner(OnDisconnectListner listner){
        _listner = listner;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        _loadingDialog = (FrameLayout) rootView.findViewById(R.id.loading_dialog);
        _map = (MapView) rootView.findViewById(R.id.map);
        _map.onCreate(savedInstanceState);
        _googleMap = _map.getMap();
        _googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        _googleMap.setMyLocationEnabled(true);
        MapsInitializer.initialize(getActivity().getApplicationContext());
        Button button = (Button) rootView.findViewById(R.id.button_map_fragment);
        button.setOnClickListener(this);
        // создаем BroadcastReceiver
        _receiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int status = intent.getIntExtra(PARAM_STATUS, STATUS_FINISH);
                if (status  == STATUS_START) {
                    _loadingDialog.setVisibility(View.GONE);
                    _googleMap.clear();
                    String result = intent.getStringExtra(PARAM_RESULT);
                    try {
                        JSONArray array = new JSONArray(result);
                        for(int i=0;i<array.length();i++){
                            JSONObject jsonObject = (JSONObject) array.get(i);
                            MarkerOptions marker =new MarkerOptions();
                            marker.position(new LatLng(jsonObject.getDouble("lat"),jsonObject.getDouble("lon")));
                            marker.title("id:"+jsonObject.getInt("id"));
                            _googleMap.addMarker(marker);
                        }
                    } catch (JSONException e) {
                        Log.e(LOG_TAG,e.toString());
                    }


                }
                if (status == STATUS_FINISH) {
                    if(_listner!=null)
                        _listner.onDisconnect();
                }
            }
        };

        return rootView;
    }

    @Override
    public void onStart(){
        super.onStart();
        getActivity().getActionBar().setTitle(getString(R.string.map_label_actionbar));
        IntentFilter intFilt = new IntentFilter(BROADCAST_ACTION);
        getActivity().registerReceiver(_receiver, intFilt);
    }

    @Override
    public void onResume(){
        super.onResume();
        _map.onResume();
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Activity.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, false);
        Location location = locationManager.getLastKnownLocation(provider);
        if(location!=null) {
            //_googleMap.animateCamera(CameraUpdateFactory.zoomIn());
            CameraPosition camera = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .zoom(11)
                .bearing(0)
                .tilt(90)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(camera);
        _googleMap.animateCamera(cameraUpdate);
        }
    }

    @Override
    public void onPause() {
        _map.onPause();
        getActivity().unregisterReceiver(_receiver);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        _map.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        _map.onLowMemory();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.button_map_fragment:
                if(_listner!=null) _listner.onDisconnect();
                break;
        }
    }
}
