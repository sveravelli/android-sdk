package com.ooyala.android.configuration;


public class VisualOnConfiguration {
  public boolean disableLibraryVersionChecks;
  private static boolean DEFAULT_DISABLE_LIBRARY_VERSION_CHECKS = false;

  /**
   * Build the object of VisualOn configurations
   * @author michael.len
   *
   */
  public static class Builder {
    private boolean disableLibraryVersionChecks = false;

    public Builder() {
    }

    /**
     * Generates a fully initialized VisualOnConfiguration
     * @return a VisualOnConfiguration for providing in the Options
     */
    public VisualOnConfiguration build() {
      return new VisualOnConfiguration( disableLibraryVersionChecks );
    }

    /**
     * Disables the version check when using the VisualOn or SecurePlayer libraries.
     * @param DisableLibraryVersionChecks true if you want to allow playback with unexpected VisualOn versions (default false)
     * @return the Builder object to continue building
     */
    public Builder setDisableLibraryVersionChecks( boolean disableLibraryVersionChecks ) {
      this.disableLibraryVersionChecks = disableLibraryVersionChecks;
      return this;
    }
  }

  /**
   * Provides the default VisualOn configuration
   * @return the default VisualOn configuration
   */
  public static final VisualOnConfiguration s_getDefaultVisualOnConfiguration() {
    return new VisualOnConfiguration( DEFAULT_DISABLE_LIBRARY_VERSION_CHECKS );
  }

  public VisualOnConfiguration( boolean disableLibraryVersionChecks ) {
    this.disableLibraryVersionChecks = disableLibraryVersionChecks;
  }
}
