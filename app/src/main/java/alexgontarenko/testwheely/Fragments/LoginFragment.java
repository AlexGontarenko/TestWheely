package alexgontarenko.testwheely.Fragments;

import android.app.Activity;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import alexgontarenko.testwheely.R;

/**
 * Created by Alex on 29.06.2014.
 */
public class LoginFragment extends Fragment implements View.OnClickListener,LocationListener {

    private LocationManager _locationManager;
    private Location _location;

    private EditText _usernameEdit,_passwordEdit;
    private TextView _label;

    private OnCompleteListner _listner;

    public interface OnCompleteListner{
        public abstract void onComplete(String username, String password);
    }

    public void setOnCompleteListner(OnCompleteListner listner){
        _listner = listner;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, container, false);
        _usernameEdit = (EditText) rootView.findViewById(R.id.username_fragment_login);
        _passwordEdit = (EditText) rootView.findViewById(R.id.password_fragment_login);
        Button button = (Button) rootView.findViewById(R.id.button_login_fragment);
        button.setOnClickListener(this);
        _label = (TextView) rootView.findViewById(R.id.label_fragment_login);
        _locationManager = (LocationManager) getActivity().getSystemService(Activity.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = _locationManager.getBestProvider(criteria, false);
        _location = _locationManager.getLastKnownLocation(provider);
        if(_location!=null) _label.setVisibility(View.GONE);
        return rootView;
    }

    @Override
    public void onStart(){
        super.onStart();
        getActivity().getActionBar().setTitle(getString(R.string.login_label_actionbar));
    }

    @Override
    public void onResume() {
        super.onResume();
        if(_location==null) {
            _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 10, 10, this);
            _locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * 10, 10, this);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        _locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location!=null) {
            _location = location;
            _locationManager.removeUpdates(this);
            _label.setVisibility(View.GONE);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onClick(View v) {
        String username, password;
        double lng,lat;
        switch (v.getId()){
            case R.id.button_login_fragment:
                if(_location!=null){
                    username = _usernameEdit.getText().toString();
                    password = _passwordEdit.getText().toString();
                    if(username.isEmpty()||password.isEmpty()){
                        Toast.makeText(getActivity().getApplicationContext(),getString(R.string.empty_label),Toast.LENGTH_SHORT).show();
                    } else {
                        if(_listner!=null) _listner.onComplete(username,password);
                    }
                } else {
                    Toast.makeText(getActivity().getApplicationContext(),getString(R.string.label_login_fragment),Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
