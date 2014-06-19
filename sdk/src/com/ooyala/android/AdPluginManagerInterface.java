package com.ooyala.android;

import com.ooyala.android.plugin.AdPluginInterface;

public interface AdPluginManagerInterface {
  /**
   * Register an Ad plugin
   * 
   * @param plugin
   *          the plugin to be registered
   * @return true on success, false otherwise
   */
  public boolean registerPlugin(final AdPluginInterface plugin);

  /**
   * called when plugin exits ad mode
   * 
   * @param plugin
   *          the plugin that exits
   * @return true on success, false otherwise
   */
  public boolean exitAdMode(final AdPluginInterface plugin);
}
