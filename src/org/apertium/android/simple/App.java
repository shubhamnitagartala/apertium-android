package org.apertium.android.simple;

import android.app.*;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.bugsense.trace.BugSenseHandler;
import java.io.File;
import java.io.IOException;
import org.apertium.utils.IOUtils;
import org.apertium.utils.Timing;

public class App extends Application {
  public static boolean isSdk() {
    return Build.PRODUCT.contains("sdk");//.equals(Build.PRODUCT) || "google_sdk".equals(Build.PRODUCT);
  }
  public static App instance;
  public static Handler handler;
  public static SharedPreferences prefs;
  /**
   Last selected mode preference
   */
  public static final String PREF_lastModeTitle = "lastModeTitle";
  /*Cache Preference*/
  public static final String PREF_cacheEnabled = "CachePref";
  /*DisplayMark Preference*/
  public static final String PREF_displayMark = "MarkPref";
  /*ClipBoardPush Preference*/
  public static final String PREF_clipBoardGet = "ClipGetPref";
  public static final String PREF_clipBoardPush = "ClipPushPref";

  public static void reportError(Exception ex) {
    ex.printStackTrace();
    langToast("Error: " + ex);
    langToast("The error will be reported to the developers. sorry for the inconvenience.");
    BugSenseHandler.sendException(ex);
  }
  public static ApertiumInstallation apertiumInstallation;

  @Override
  public void onCreate() {
    super.onCreate();
    prefs = PreferenceManager.getDefaultSharedPreferences(this);

    // If you want to use BugSense for your fork, register with
    // them and place your API key in /assets/bugsense.txt
    try {
      //String key = getString(R.string.bugsensekey);
      byte[] buffer = new byte[16];
      int n = getAssets().open("bugsense.txt").read(buffer);
      String key = new String(buffer, 0, n).trim();

      Log.d("TAG", "Using bugsense key '" + key + "'");
      BugSenseHandler.initAndStartSession(this, key);
    } catch (IOException e) {
      Log.d("TAG", "No bugsense keyfile found");
    }

    instance = this;
    handler = new Handler();

    try {
      Class.forName("android.os.AsyncTask"); // Fix for http://code.google.com/p/android/issues/detail?id=20915
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Set where the app will keep cached indexes
    IOUtils.cacheDir = new File(getExternalCacheDir(), "apertium-index-cache");
    Log.i("TAG", "IOUtils.cacheDir set to " + IOUtils.cacheDir);

    apertiumInstallation = new ApertiumInstallation(this);
    apertiumInstallation.rescanForPackages();

  }

  public static void langToast(final String txt) {
    //new Throwable(txt).printStackTrace();
    instance.handler.post(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(instance, txt, Toast.LENGTH_LONG).show();
      }
    });
  }

  public static void kortToast(final String txt) {
    //new Throwable(txt).printStackTrace();
    instance.handler.post(new Runnable() {
      @Override
      public void run() {
        Toast.makeText(instance, txt, Toast.LENGTH_SHORT).show();
      }
    });
  }


  /* Version fra http://developer.android.com/training/basics/network-ops/managing.html */
  public static boolean isOnline() {
    ConnectivityManager connMgr = (ConnectivityManager) instance.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
    return (networkInfo != null && networkInfo.isConnected());
  }
}
