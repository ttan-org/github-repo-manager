package com.axonivy.github;

import java.util.List;

public interface GitHubRepos {

  public static List<String> repos(String version) {
    if (version.startsWith("7")) {
      return REPOS7;
    } else if (version.startsWith("8")) {
      return REPOS8;
    }
    return REPOS;
  }

  List<String> REPOS7 = List.of("core-7", "ulc-ria", "admin-ui", "rules", "maven-plugins", "webeditor");

  List<String> REPOS8 = List.of(
          "rules",
          "engine-cockpit",
          "maven-plugins",
          "webeditor",
          "core",
          "primefaces-themes",
          "ws-axis",
          "case-map-ui",
          "thirdparty-libs",
          "p2-targetplatform",
          "doc-images",
          "engine-launchers",
          "core-icons");

  List<String> REPOS = List.of(
          "glsp-editor-client",
          "rules",
          "engine-cockpit",
          "maven-plugins",
          "dev-workflow-ui",
          "webeditor",
          "core",
          "primefaces-themes",
          "process-editor-core",
          "ws-axis",
          "case-map-ui",
          "thirdparty-libs",
          "p2-targetplatform",
          "doc-images",
          "engine-launchers",
          "core-icons",
          "branding-images");
}
