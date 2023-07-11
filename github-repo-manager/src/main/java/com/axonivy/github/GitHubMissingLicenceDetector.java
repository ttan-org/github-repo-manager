package com.axonivy.github;

import java.io.IOException;
import java.util.List;

public class GitHubMissingLicenceDetector {

  public static void main(String[] args) throws IOException {
    var github = GitHubProvider.get();

    for (var r : List.of("axonivy", "axonivy-market")) {
      var org = github.getOrganization(r);
      var repos = List.copyOf(org.getRepositories().values());

      for (var repo : repos) {
        if (repo.isPrivate()) {
          continue;
        }

        var lic = repo.getLicense();
        if (lic == null) {
          System.out.println(repo.getUrl());
        }
      }
    }
  }
}
