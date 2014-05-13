package ru.ifmo.findmyfriend;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkTokenRequestListener;
import ru.ok.android.sdk.util.OkScope;

public class LoginActivity extends Activity implements OkTokenRequestListener {
    private String APP_ID = "927961344";
    private String APP_PUBLIC_KEY = "CBACJQEIDBABABABA";
    private String APP_SECRET_KEY = "BA4A8EBC7AF0F03551156B44";
    private Odnoklassniki mOdnoklassniki;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        mOdnoklassniki = Odnoklassniki.createInstance(getApplicationContext(), APP_ID, APP_SECRET_KEY, APP_PUBLIC_KEY);
        mOdnoklassniki.setTokenRequestListener(this);
        mOdnoklassniki.requestAuthorization(this, false, OkScope.VALUABLE_ACCESS);
    }

    @Override
    public void onSuccess(String token) {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    public void onError() {
        finish();
    }

    @Override
    public void onCancel() {
        finish();
    }
}
