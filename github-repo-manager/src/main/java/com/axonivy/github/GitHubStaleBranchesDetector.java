package com.axonivy.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GitHubStaleBranchesDetector {

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
        if ("master".equals(branch.getName())) {
          continue;
        }
        if ("gh-pages".equals(branch.getName())) {
          continue;
        }
        if ("dev10.0".equals(branch.getName())) {
          continue;
        }
        if ("dev11.1".equals(branch.getName())) {
          continue;
        }
        if (branch.getName().startsWith("release/")) {
          continue;
        }
        if (branch.getName().startsWith("stale/")) {
          continue;
        }
        if (branch.getName().startsWith("dependabot/")) {
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
