package com.ooyala.android.secureplayersample;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.ooyala.android.DefaultPlayerInfo;
import com.ooyala.android.EmbedTokenGenerator;
import com.ooyala.android.EmbedTokenGeneratorCallback;
import com.ooyala.android.OoyalaPlayer;
import com.ooyala.android.OoyalaPlayer.State;
import com.ooyala.android.OoyalaPlayerLayout;
import com.ooyala.android.PlayerDomain;
import com.ooyala.android.PlayerInfo;
import com.ooyala.android.configuration.Options;
import com.ooyala.android.configuration.VisualOnConfiguration;
import com.ooyala.android.freewheelsdk.OoyalaFreewheelManager;
import com.ooyala.android.imasdk.OoyalaIMAManager;
import com.ooyala.android.player.StreamPlayer;
import com.ooyala.android.ui.AbstractOoyalaPlayerLayoutController;
import com.ooyala.android.ui.AbstractOoyalaPlayerLayoutController.DefaultControlStyle;
import com.ooyala.android.ui.OoyalaPlayerLayoutController;
import com.ooyala.android.ui.OptimizedOoyalaPlayerLayoutController;
import com.ooyala.android.util.DebugMode;

public class SecurePlayerSampleAppActivity extends Activity implements Observer, EmbedTokenGenerator {
  private static final String TAG = SecurePlayerSampleAppActivity.class.getSimpleName();

  OoyalaPlayer player;
  ArrayAdapter<String> playerAdapter;
  Spinner playerSpinner;
  Spinner embedSpinner;
  HashMap<String, Pair<String,String>> embedMap;
  ArrayAdapter<String> embedAdapter;
  String pcode;

  private final String APIKEY = "Use this for testing, don't keep your secret in the application";
  private final String SECRET = "Use this for testing, don't keep your secret in the application";
  private final String ACCOUNT_ID = "accountID";
  final String DOMAIN = "http://www.ooyala.com";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);

    OoyalaPlayer.enableCustomPlayreadyPlayer = true;

    //Initialize the bottom controls
    playerSpinner = (Spinner) findViewById(R.id.playerSpinner);
    playerAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
    playerSpinner.setAdapter(playerAdapter);
    playerAdapter.add( "VisualOn/SecurePlayer" );
    playerAdapter.add("Native Player");
    playerAdapter.notifyDataSetChanged();

    //Populate the embed map
    embedMap = new LinkedHashMap<String, Pair<String,String>>();
    embedMap.put("Device Management - Device Bind to Entitlement", new Pair<String,String>("N5dGEyOrMsKgdLgNp2B0wirtpqm7","Q3NmpoczpUH__SVSKRI0BbFl3A9CtHSL"));
    embedMap.put("Device Management - Device Limit", new Pair<String,String>("N5dGEyOrMsKgdLgNp2B0wirtpqm7","0xNmpoczpeNkx6Pq8ZOPwPUu6CuzFKeY"));
    embedMap.put("Ooyala-Ingested Playready Smooth VOD", new Pair<String,String>("FoeG863GnBL4IhhlFC1Q2jqbkH9m","5jNzJuazpFtKmloYZQmgPeC_tqDKHX9r"));
    embedMap.put("Playready HLS VOD with IMA Ads", new Pair<String,String>("FoeG863GnBL4IhhlFC1Q2jqbkH9m","xpcGYydDpUfj8eOSFSm7-Hr8-Eaag52i"));
    embedMap.put("Playready HLS VOD with Freewheel Ads", new Pair<String,String>("FoeG863GnBL4IhhlFC1Q2jqbkH9m","xtcGYydDrhnBc9InRIjM4w7ig6w3ay9E"));
    embedMap.put("Playready HLS VOD with Closed Captions", new Pair<String,String>("FoeG863GnBL4IhhlFC1Q2jqbkH9m","xrcGYydDq1wU7nSmX7AQB3Uq4Fu3BjuE"));
    embedMap.put("Ooyala-Ingested Playready HLS VOD", new Pair<String,String>("FoeG863GnBL4IhhlFC1Q2jqbkH9m","92eGNjcjpbo561vVTXE-8GDAk05LHYBh"));
    embedMap.put("Microsoft-Ingested Playready Smooth VOD", new Pair<String,String>("FoeG863GnBL4IhhlFC1Q2jqbkH9m","V2NWk2bTpI1ac0IaicMaFuMcIrmE9U-_"));
    embedMap.put("Microsoft-Ingested Clear Smooth VOD", new Pair<String,String>("FoeG863GnBL4IhhlFC1Q2jqbkH9m","1nNGk2bTq5ECsz5cRlZ4ONAAk96drr6T"));
    embedMap.put("Ooyala-Ingested Clear HLS VOD", new Pair<String,String>("FoeG863GnBL4IhhlFC1Q2jqbkH9m","Y1ZHB1ZDqfhCPjYYRbCEOz0GR8IsVRm1"));
    embedMap.put("OPL Test - A150 C500 U301", new Pair<String,String>("N5dGEyOrMsKgdLgNp2B0wirtpqm7","01Nmpoczq_GLtFUuTyy6mfQzkGjTIl9F"));
    embedMap.put("OPL Test - A150 C500 U300", new Pair<String,String>("N5dGEyOrMsKgdLgNp2B0wirtpqm7","0zNmpoczrbFOt-jK9wWNABrpKlSDduxN"));
    embedMap.put("OPL Test - A150 C500 U250", new Pair<String,String>("N5dGEyOrMsKgdLgNp2B0wirtpqm7","15NWpoczoxGzZRc2g_rqNA7WSMrSrdak"));
    embedMap.put("OPL Test - A201 C500 U250", new Pair<String,String>("N5dGEyOrMsKgdLgNp2B0wirtpqm7","13NWpoczpBVeg8eUyswxFioYmJIOzTje"));
    embedAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item);
    embedSpinner = (Spinner) findViewById(R.id.embedSpinner);
    embedSpinner.setAdapter(embedAdapter);
    embedAdapter.addAll( embedMap.keySet() );
    embedAdapter.notifyDataSetChanged();

    Button setButton = (Button) findViewById(R.id.setButton);

    setButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        if( player != null ) {
          player.suspend();
          player.deleteObserver(SecurePlayerSampleAppActivity.this);
          player = null;
        }
        final Pair<String,String> asset = embedMap.get(embedSpinner.getSelectedItem());
        Log.d( TAG, "asset = " + asset );
        SecurePlayerSampleAppActivity.this.pcode = asset.first;
        final String embed = asset.second;
        VisualOnConfiguration visualOnConfiguration = new VisualOnConfiguration.Builder().setDisableLibraryVersionChecks(false).build();
        Options.Builder builder = new Options.Builder().setVisualOnConfiguration( visualOnConfiguration );
        Options options = builder.setPreloadContent(false).build();
        OoyalaPlayerLayout playerLayout = (OoyalaPlayerLayout) findViewById(R.id.ooyalaPlayer);
        OoyalaPlayer ooPlayer = new OoyalaPlayer(pcode, new PlayerDomain(DOMAIN), SecurePlayerSampleAppActivity.this, options);
        final OoyalaPlayerLayoutController playerLayoutController = new OoyalaPlayerLayoutController(playerLayout, ooPlayer, DefaultControlStyle.AUTO);
        player = playerLayoutController.getPlayer();
        StreamPlayer.defaultPlayerInfo = new DefaultPlayerInfo() {

            @Override
            public Set<String> getSupportedFormats() {
              Set set = new HashSet<String>();
                      set.add("playready_hls");
              return set;
            }

          };
        OoyalaFreewheelManager fwManager = new OoyalaFreewheelManager(SecurePlayerSampleAppActivity.this, playerLayoutController);
        if (((String)embedSpinner.getSelectedItem()).contains("IMA")) {
          OoyalaIMAManager imaManager = new OoyalaIMAManager(player);
        }

        player.addObserver(SecurePlayerSampleAppActivity.this);
        if (player.setEmbedCode(embed)) {
          TextView urlText = (TextView) findViewById(R.id.urlText);
          urlText.setText("");
          if(playerSpinner.getSelectedItem().toString()  == "Native Player") {
            OoyalaPlayer.enableCustomHLSPlayer = false;
            OoyalaPlayer.enableCustomPlayreadyPlayer = false;
          } else {
            OoyalaPlayer.enableCustomHLSPlayer = true;
            OoyalaPlayer.enableCustomPlayreadyPlayer = true;
          }
          player.play();
        } else {
          Log.d(TAG, "Something Went Wrong!");
        }
      }
    });

  }

  @Override
  protected void onPause() {
    super.onPause();
    if (player != null && player.getState() != State.SUSPENDED) {
      player.suspend();
    }
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (player != null && player.getState() == State.SUSPENDED) {
      player.resume();
    }
  }

  @Override
  public void update(Observable observable, Object data) {
    DebugMode.logV( TAG, "update: " + observable + ", " + data );
    if( data == OoyalaPlayer.ERROR_NOTIFICATION ) {
      new AlertDialog.Builder(this)
        .setTitle( "Error" )
        .setMessage( "An error was encountered: " + player.getError().toString() )
        .setIcon( android.R.drawable.ic_dialog_alert )
        .setNeutralButton( android.R.string.ok, null )
        .show();
    }
  }

  // This is a local method of generating an embed token for debugging.
  // It is unsafe to have your key and secrets in a production application
  @Override
  public void getTokenForEmbedCodes(List<String> embedCodes,
      EmbedTokenGeneratorCallback callback) {
    String embedCodesString = "";
    for (String ec : embedCodes) {
      if(ec.equals("")) embedCodesString += ",";
      embedCodesString += ec;
    }

    HashMap<String, String> params = new HashMap<String, String>();
    params.put("account_id", ACCOUNT_ID);

    String uri = "/sas/embed_token/" + pcode + "/" + embedCodesString;
    EmbeddedSecureURLGenerator urlGen = new EmbeddedSecureURLGenerator(APIKEY, SECRET);

    URL tokenUrl  = urlGen.secureURL("http://player.ooyala.com", uri, params);

    callback.setEmbedToken(tokenUrl.toString());
  }
}
