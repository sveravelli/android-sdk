package com.ooyala.android;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import android.content.Context;
import android.util.Log;

/**
 * Help create and cleanup automatically uniquely named files in the given Context's internal cache directory.
 * The first use case was for Analytics.java javascript loading, where we do not have a good way of knowing when it is
 * safe to delete files so we use a window of time for reaping.
 */
final public class TemporaryInternalStorageFileManager {
  private static final String TAG = "TemporaryInternalStorageFiles";
  private static final String PRE_PRE_FIX = "OOTISF_";
  public static final long TMP_LIFESPAN_MSEC = 5 * 60 * 1000; // 5 minutes.
  private static AtomicLong s_nextTmpId = new AtomicLong();

  /**
   * The final name of the file inside the returned TemporaryInternalStorageFile will include the given prefix and ext, but will
   * be concatentated with other strings to make it unique vs. any other TemporaryInternalStorageFile created via this call.
   * If somebody creates a TemporaryInternalStorageFile without going through here then collisions are possible.
   */
  public TemporaryInternalStorageFile next( final Context context, final String prefix, final String ext ) throws IOException {
    cleanup( context );
    final long id = s_nextTmpId.getAndIncrement();
    return new TemporaryInternalStorageFile( context, PRE_PRE_FIX + s_nextTmpId.get() + "_" + prefix, ext );
  }

  /**
   * Remove any temporary files that (1) match our managed file name structure and (2) are too old.
   */
  public void cleanup( final Context context ) {
    final File dir = context.getCacheDir();
    Log.d( TAG, "cleanup(): dir=" + dir );
    if( dir != null && dir.isDirectory() ) {
      final long now = new Date().getTime();
      for( final File f : dir.listFiles( new FileFilter() {
        @Override
        public boolean accept( final File f ) {
          final boolean isFile = f.isFile();
          final boolean nameMatches = f.getName().startsWith( PRE_PRE_FIX );
          final boolean isOld = now - f.lastModified() >= TMP_LIFESPAN_MSEC;
          Log.d( TAG, "cleanup(): f=" + f.getAbsolutePath() + ", isFile=" + isFile + ", nameMatches=" + nameMatches + ", isOld=" + isOld );
          return isFile && nameMatches && isOld;
        }
      } ) ) {
        Log.d( TAG, "cleanup(): deleting f=" + f.getAbsolutePath() + ", name=" + f.getName() );
        f.delete(); // in Android Java, File.delete() doesn't throw exceptions.
      }
    }
  }
}
