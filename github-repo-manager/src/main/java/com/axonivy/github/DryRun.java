package com.axonivy.github;

public class DryRun {

  public static boolean is() {
    return Boolean.parseBoolean(System.getProperty("DRY_RUN", "true"));
  }
}
