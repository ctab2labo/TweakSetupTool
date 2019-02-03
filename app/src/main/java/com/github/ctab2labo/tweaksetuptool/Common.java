package com.github.ctab2labo.tweaksetuptool;

import android.os.Environment;

import java.io.File;

public final class Common {
  public static final String TAG = "TweakSetupTool";

  public static final String SHARED_PREFERNCE_KEY = "com.github.ctab2labo.tweaksetuptool";
  public static final String KEY_ENABLED_KEEP_SERVICE = "enabled_keep_service";

  private static final String DIRECTORY_NAME = "com.github.ctab2labo.tweaksetuptool";
  public static final File SAVE_DIRECTORY = new File(Environment.getExternalStorageDirectory(), DIRECTORY_NAME);

  public static final String EXTRA_APP_PACKAGES = "extra_app_packages";
}
