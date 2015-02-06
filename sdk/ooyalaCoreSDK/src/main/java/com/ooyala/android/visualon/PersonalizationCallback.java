package com.ooyala.android.visualon;

interface PersonalizationCallback {
  /**
   * This callback is used for the PersonalizationAsyncTask asynchronous calls
   * @param result true if the PersonalizationAsyncTask call succeeded, false otherwise
   */
  public void afterPersonalization(Exception success);
}