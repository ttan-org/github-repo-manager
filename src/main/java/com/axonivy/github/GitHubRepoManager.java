package com.axonivy.github;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHubBuilder;

public class GitHubRepoManager {

  public static void main(String[] args) throws IOException {
    var path = new File("github.token").toPath();
    var token = Files.readString(path);
    var github = new GitHubBuilder()
            .withOAuthToken(token)
            .build();

    var org = github.getOrganization("axonivy");
    var repos = List.copyOf(org.getRepositories().values());
    for (var repo : repos) {
      new RepoConfigurator(repo).run();
    }
  }

  private static class RepoConfigurator {

    private final GHRepository repo;

    private RepoConfigurator(GHRepository repo) {
      this.repo = repo;
    }

    private void run() throws IOException {
      if (repo.isArchived()) {
        return;
      }

      // bug: always returns false
      if (!repo.isDeleteBranchOnMerge()) {
        //log("delete branch on merge");
        //repo.deleteBranchOnMerge(true);
      }
      if (repo.hasProjects()) {
        log("disable projects");
        repo.enableProjects(false);
      }
      if (repo.hasIssues()) {
        log("disable issues");
        repo.enableIssueTracker(false);
      }
      if (repo.hasWiki()) {
        log("disable wiki");
        repo.enableWiki(false);
      }

      for (var hook : repo.getHooks()) {
        log("delete hook " + hook.getUrl());
        hook.delete();
      }

      // patterns are not possible at the moment
      var existingBranches = repo.getBranches();
      var branches = List.of("master", "release/9.3", "release/9.2", "release/9.1", "release/8.0", "release/7.0");
      for (var name : branches) {
        if (!existingBranches.containsKey(name)) {
          continue;
        }
        var branch = repo.getBranch(name);
        if (!isProtected(branch)) {
          log("protect " + branch.getName() + " branch");
          branch.enableProtection()
                  .requiredReviewers(0)
                  .includeAdmins()
                  .enable();
        }
      }
    }

    private boolean isProtected(GHBranch branch) throws IOException {
      if (branch.isProtected()) {
        var protection = branch.getProtection();
        var reviews = protection.getRequiredReviews();
        if (reviews == null) {
          log("protection of branch " + branch.getName() + " allows to merge without PR");
          branch.disableProtection();
          return false;
        }
        var reviewers = reviews.getRequiredReviewers();
        if (reviewers != 0) {
          log("protection of branch " + branch.getName() + " has not the correct amount of reviewers " + reviewers);
          branch.disableProtection();
          return false;
        }
        if (!protection.getEnforceAdmins().isEnabled()) {
          log("protection of branch " + branch.getName() + " does not enforce admins ");
          branch.disableProtection();
          return false;
        }
        return true;
      }
      return false;
    }

    private void log(String log) {
      System.out.println(repo.getFullName() + ": " + log);
    }
  }
}
