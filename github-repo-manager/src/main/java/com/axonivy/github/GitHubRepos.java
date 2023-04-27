package com.axonivy.github;

import java.util.ArrayList;
import java.util.List;

public class GitHubRepos {

  public static List<String> repos(String version) {
    if (version.startsWith("7")) {
      return REPOS7;
    } else if (version.startsWith("8")) {
      return REPOS8;
    }
    return REPOS;
  }

  public static final List<String> REPOS7 = List.of("core-7", "ulc-ria", "admin-ui", "rules", "maven-plugins", "webeditor");

  public static final  List<String> REPOS8 = List.of(
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

  public static final  List<String> REPOS_TO_BRANCH = List.of(
          "process-editor-client",
          "rules",
          "engine-cockpit",
          "maven-plugins",
          "dev-workflow-ui",
          "webeditor",
          "core",
          "primefaces-themes",
          "process-editor-core",
          "doc-images",
          "case-map-ui",
          "branding-images",
          "thirdparty-libs",
          "inscription-client",
          "project-build-examples");

  public static final  List<String> REPOS = new ArrayList<>();

  static {
    REPOS.addAll(REPOS_TO_BRANCH);
    REPOS.addAll(List.of("p2-targetplatform", "engine-launchers", "core-icons"));
  }
}
