package com.ooyala.AdobePassDemoApp;

import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.OptimizedOoyalaPlayerLayoutController;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class AdobePassDemoAppActivity extends Activity implements OnAuthorizationChangedListener {
  private static final String API_KEY = "pqdHc6rN2_wYW2z-pOmDqkUmMnI1.lpezz";
  private static final String SECRET = "zJfIg9IRPSL6lZH0GLgPLrxaEg8abFCDDoqhi6V4";
  private static final String PROVIDER_CODE = "pqdHc6rN2_wYW2z-pOmDqkUmMnI1";
  private static final String DOMAIN = "www.ooyala.com";

  private AdobePassLoginController adobePassController;
  private OptimizedOoyalaPlayerLayoutController ooController;

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    adobePassController = new AdobePassLoginController(this, "fbc-fox",
        getResources().openRawResource(R.raw.adobepass), "adobepass", this);
    adobePassController.checkAuth();

    ooController = new OptimizedOoyalaPlayerLayoutController(
        (OoyalaPlayerLayout) findViewById(R.id.player),
        API_KEY, SECRET, PROVIDER_CODE, DOMAIN, adobePassController);
  }

  public void onLoginClick(View v) {
    Button loginButton = (Button) v;
    if (loginButton.getText().equals("Login")) {
      adobePassController.login();
    } else {
      adobePassController.logout();
    }
  }

  public void onSetEmbedCodeClick(View v) {
    ooController.getPlayer().setEmbedCode("5icXRvNDrl9kxzF_58oV79ApUNRffoLR");
    ooController.getPlayer().play();
  }

  @Override
  public void authChanged(Boolean authorized) {
    Button loginButton = (Button) findViewById(R.id.loginButton);
    if (authorized) {
      loginButton.setText("Logout");
    } else {
      loginButton.setText("Login");
      ooController.getPlayer().setEmbedCode("none");
    }
  }
}