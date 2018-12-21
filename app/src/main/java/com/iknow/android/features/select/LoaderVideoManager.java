package com.iknow.android.features.select;

import android.content.Context;
import iknow.android.utils.callback.SimpleCallback;


public class LoaderVideoManager {

  private ILoader mLoader;

  public void setLoader(ILoader loader) {
    this.mLoader = loader;
  }

  public void load(final Context context, final SimpleCallback listener) {
    mLoader.load(context, listener);
  }
}
