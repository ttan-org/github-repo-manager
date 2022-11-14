package com.axonivy.github;

import java.io.IOException;
import java.util.List;

import org.kohsuke.github.GHBranch;
import org.kohsuke.github.GHRepository;

public class GitHubRepoSettingsManager {

  public static void main(String[] args) throws IOException {
    var github = GitHubProvider.get();

    var org = github.getOrganization("axonivy");
    var repos = List.copyOf(org.getRepositories().values());
    for (var repo : repos) {
      if (repo.isArchived()) {
        continue;
      }
      new RepoConfigurator(repo)
              .deleteHeadBranchOnMerge()
              .disableProjects()
              .disableIssues()
              .disableWiki()
              .deleteHooks()
              .protectBranches(true);
    }

    org = github.getOrganization("axonivy-market");
    repos = List.copyOf(org.getRepositories().values());
    for (var repo : repos) {
      if (repo.isArchived()) {
        continue;
      }
      new RepoConfigurator(repo)
              .deleteHeadBranchOnMerge()
              .disableProjects()
              .deleteHooks()
              .protectBranches(false);
    }
  }

  public static class RepoConfigurator {
    private final GHRepository repo;

    public RepoConfigurator(GHRepository repo) {
      this.repo = repo;
    }

    RepoConfigurator deleteHeadBranchOnMerge() {
      // bug: always returns false
      if (!repo.isDeleteBranchOnMerge()) {
        log("delete branch on merge");
        //repo.deleteBranchOnMerge(true);
      }
      return this;
    }

    RepoConfigurator protectBranches(boolean orgWithPlan) throws IOException {
      if (repo.isPrivate() && !orgWithPlan) {
        // can't use branch protection feature in org with no plan and private repo
        log("can't protect private repo in org with no plan");
        return this;
      }

      for (var branch : repo.getBranches().values()) {
        if (!branch.getName().equals("master") && !branch.getName().startsWith("release/")) {
          continue;
        }

        if (!isProtected(branch)) {
          log("protect " + branch.getName() + " branch");
          branch.enableProtection()
                  .requiredReviewers(0)
                  .includeAdmins()
                  .enable();
        }
      }
      return this;
    }

    RepoConfigurator deleteHooks() throws IOException {
      for (var hook : repo.getHooks()) {
        log("delete hook " + hook.getUrl());
        hook.delete();
      }
      return this;
    }

    RepoConfigurator disableWiki() throws IOException {
      if (repo.hasWiki()) {
        log("disable wiki");
        repo.enableWiki(false);
      }
      return this;
    }

    RepoConfigurator disableIssues() throws IOException {
      if (repo.hasIssues()) {
        log("disable issues");
        repo.enableIssueTracker(false);
      }
      return this;
    }

    RepoConfigurator disableProjects() throws IOException {
      if (repo.hasProjects()) {
        log("disable projects");
        repo.enableProjects(false);
      }
      return this;
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
