package com.aoxnivy.github;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.axonivy.github.GitHubProvider;
import com.axonivy.github.GitHubRepoTagger;

class TestGitHubRepoTagger {

  @Test
  void run() throws IOException {
    var repo = GitHubProvider.get().getRepository("axonivy/github-repo-manager");
    new GitHubRepoTagger.Tagger(repo, false, "master", "v9.4.0").run();
  }
}
