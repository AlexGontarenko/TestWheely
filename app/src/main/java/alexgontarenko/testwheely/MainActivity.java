package alexgontarenko.testwheely;

import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import alexgontarenko.testwheely.Fragments.LoginFragment;
import alexgontarenko.testwheely.Fragments.MapFragment;
import alexgontarenko.testwheely.Services.WheelyService;


public class MainActivity extends ActionBarActivity implements LoginFragment.OnCompleteListner,MapFragment.OnDisconnectListner {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart(){
        super.onStart();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(WheelyService.isStarted()){
            MapFragment fragment = new MapFragment();
            fragment.setOnDisconnectListner(this);
            transaction.add(R.id.container, fragment);

        } else {
            LoginFragment fragment = new LoginFragment();
            fragment.setOnCompleteListner(this);
            transaction.add(R.id.container, fragment);
        }
        transaction.commit();
    }

    @Override
    protected void onStop(){
        super.onStop();
    }

    @Override
    public void onComplete(String username, String password) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        MapFragment fragment = new MapFragment();
        fragment.setOnDisconnectListner(this);
        transaction.replace(R.id.container, fragment);
        transaction.commit();
        Intent intent = new Intent(WheelyService.class.getName());
        intent.putExtra(WheelyService.USERNAME_TAG,username);
        intent.putExtra(WheelyService.PASSWORD_TAG,password);
        startService(intent);
    }

    @Override
    public void onDisconnect() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        LoginFragment fragment = new LoginFragment();
        fragment.setOnCompleteListner(this);
        transaction.replace(R.id.container, fragment);
        transaction.commit();
        Intent intent = new Intent(WheelyService.class.getName());
        stopService(intent);
    }
}