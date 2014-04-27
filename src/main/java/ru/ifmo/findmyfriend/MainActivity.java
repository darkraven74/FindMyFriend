package ru.ifmo.findmyfriend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkTokenRequestListener;
import ru.ok.android.sdk.util.OkScope;

public class MainActivity extends Activity implements OkTokenRequestListener {
    private String APP_ID = "927961344";
    private String APP_PUBLIC_KEY = "CBACJQEIDBABABABA";
    private String APP_SECRET_KEY = "BA4A8EBC7AF0F03551156B44";
    private Odnoklassniki mOdnoklassniki;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        startActivity(new Intent(this, MapActivity.class));
        finish();
//        mOdnoklassniki = Odnoklassniki.createInstance(getApplicationContext(), APP_ID, APP_SECRET_KEY, APP_PUBLIC_KEY);
//        mOdnoklassniki.setTokenRequestListener(this);
    }

//    @Override
//    public void onDestroy() {
//        mOdnoklassniki.removeTokenRequestListener();
//        super.onDestroy();
//    }

    @Override
    public void onSuccess(String token) {
        Log.v("APIOK", "auth success! token: " + token);
        startActivity(new Intent(this, MapActivity.class));
        //startActivity(new Intent(MainActivity.this, MainActivity.class));
        finish();
    }

    @Override
    public void onError() {
        Log.v("APIOK", "auth error");
    }

    @Override
    public void onCancel() {
        Log.v("APIOK", "auth cancel");
    }

    public void clickLogin(View view) {
        Log.v("APIOK", "loginButton clicked");
        mOdnoklassniki.requestAuthorization(this, false, OkScope.VALUABLE_ACCESS);
    }
}
