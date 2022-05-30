package com.axonivy.github;

import java.io.IOException;

import org.kohsuke.github.GHRepository;

public class GitHubRepoTagger {

  public static void main(String[] args) {
    var github = GitHubProvider.get();

    var dryRun = DryRun.is();
    var branch = args[0];
    var tag = args[1];
    var message = args[2];

    System.out.println("Start GitHub repository tagging");
    System.out.println("dryRun: " + dryRun);
    System.out.println("branch: " + branch);
    System.out.println("tag: " + tag);
    System.out.println("message: " + message);
    try {
      var org = github.getOrganization("axonivy");
      for (var repo : org.getRepositories().values()) {
        new Tagger(repo, dryRun, branch, tag, message).run();
      }
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  public static final class Tagger {

    private final GHRepository repo;
    private final boolean dryRun;
    private final String branch;
    private final String tag;
    private final String message;

    public Tagger(GHRepository repo, boolean dryRun, String branch, String tag, String message) {
      this.repo = repo;
      this.dryRun = dryRun;
      this.branch = branch;
      this.tag = tag;
      this.message = message;
    }

    public void run() {
      try {
        if (repo.isArchived()) {
          System.out.println("Skipping archived repo " + repo.getFullName());
          return;
        }
        if (!repo.getBranches().containsKey(branch)) {
          System.out.println("Skipping repo " + repo.getFullName() + " because there is no " + branch + " branch");
          return;
        }

        var ghBranch = repo.getBranch(branch);
        var sha1 = ghBranch.getSHA1();
        if (dryRun) {
          System.out.print("DRYRUN: ");
        }
        System.out.println("Create tag " + tag + " on " + repo.getFullName() + " ~ " + branch + " ~ " + sha1);
        if (!dryRun) {
          repo.createTag(tag, message, sha1, "commit");
        }
      } catch (IOException ex) {
        throw new RuntimeException(ex);
      }
    }
  }
}
