package com.axonivy.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class GitHubStaleBranchesDetector {

  private static Set<String> IGNORE_BRANCHES = Set.of(
          "master",
          "gh-pages",
          "dev10.0",
          "dev11.1",
          "release/",
          "stale/",
          "dependabot/"
  );

  public static void main(String[] args) throws IOException {
    var github = GitHubProvider.get();
    var org = github.getOrganization("axonivy");
    var repos = List.copyOf(org.getRepositories().values());

    var lines = new ArrayList<LastCommit>();

    for (var repo : repos) {
      if (repo.isArchived()) {
        continue;
      }
      if ("xivy-3.9".equals(repo.getName())) {
        continue;
      }

      for (var branch : repo.getBranches().values()) {
        var ignore = IGNORE_BRANCHES.stream()
          .anyMatch(b -> branch.getName().startsWith(b));
        if (ignore) {
          continue;
        }
        var lastCommit = repo.getCommit(branch.getSHA1());
        var author = lastCommit.getAuthor();
        var authorName = "?";
        if (author != null) {
          authorName = author.getLogin();
        }

        var line = new LastCommit(repo.getName(), branch.getName(), authorName);
        lines.add(line);
      }
    }

    var group = lines.stream()
      .collect(Collectors.groupingBy(LastCommit::author, Collectors.toSet()));

    for (var entry : group.entrySet()) {
      System.out.println(entry.getKey());
      for (var commit : entry.getValue()) {
        System.out.println("https://github.com/axonivy/" + commit.repo + "/tree/" + commit.branch);
      }
      System.out.println();
    }
  }

  public record LastCommit(String repo, String branch, String author) {

    public String author() {
      return author == null ? "?" : author;
    }
  }
}
