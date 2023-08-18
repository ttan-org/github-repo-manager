package com.axonivy.github;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;

public class GitHubMissingLicenceDetector {

  private static final String PR = "Add license";
  private static final String GITHUB_ORG = ".github";
  private static final String COMMIT_MESSAGE = "Add Apache 2.0 license";
  private static final String BRANCH = "Add_License";
  private static final String LICENSE = "LICENSE";
  private final byte[] licenseContent;
  private boolean missingLicense = false;

  public static void main(String[] args) throws IOException {
    new GitHubMissingLicenceDetector().checkMissingLicense();
  }

  public GitHubMissingLicenceDetector() throws IOException {
    try (var is = GitHubMissingLicenceDetector.class.getResourceAsStream(LICENSE)) {
      if (is == null) {
        throw new IOException("License file not found");
      }
      this.licenseContent = IOUtils.toByteArray(is);
    }
  }

  private void checkMissingLicense() throws IOException {
    var github = GitHubProvider.get();
    for (var r : List.of("axonivy", "axonivy-market")) {
      var org = github.getOrganization(r);
      var repos = List.copyOf(org.getRepositories().values());

      for (var repo : repos) {
        checkMissingLicense(repo);
      }
    }
    if (missingLicense) {
      System.err.println("At least one repository has no license.");
      System.err.println("Add a license manually or run the build without DRYRUN to add Apache 2.0 license to the repository.");
      System.exit(-1);
    }
    System.exit(0);
  }

  private void checkMissingLicense(GHRepository repo) throws IOException {
    if (repo.isPrivate()) {
      System.out.println("Repo " + repo.getFullName() +" is private.");
      return;
    }
    if (repo.isArchived()) {
      System.out.println("Repo " + repo.getFullName() +" is archived.");
      return;
    }
    var license = repo.getLicense();
    if (license != null) {
      var licenseFile = repo.getLicenseContent();
      if (!hasLicenseOrCopyingFile(licenseFile)) {
        System.err.println("Repo " + repo.getFullName() +" has license " + license.getName() + " but no LICENSE or COPYING file instead it has "+ licenseFile.getPath());
        missingLicense = true;
        return;
      }
      System.out.println("Repo " + repo.getFullName() +" has license " + license.getName());
      return;
    }
    if (GITHUB_ORG.equals(repo.getName())) {
      return;
    }
    addLicense(repo);
  }

  private boolean hasLicenseOrCopyingFile(GHContent licenseFile) {
    return licenseFile.getPath().equals("LICENSE") || licenseFile.getPath().equals("COPYING");
 }

  private void addLicense(GHRepository repo) throws IOException {
    try {
      if (DryRun.is()) {
        missingLicense = true;
        System.err.print("DRYRUN: ");
      } else {
        var defaultBranch = repo.getBranch(repo.getDefaultBranch());
        var sha1 = defaultBranch.getSHA1();
        repo.createRef("refs/heads/" + BRANCH, sha1);
        repo.createContent().branch(BRANCH).path(LICENSE).content(licenseContent).message(COMMIT_MESSAGE).commit();
        var pr = repo.createPullRequest(PR, BRANCH, repo.getDefaultBranch(), "");
        pr.merge(COMMIT_MESSAGE);
      }
      System.err.println("Repo "+ repo.getFullName() +" license added");
    } catch (IOException ex) {
      System.err.println("Cannot add license to repo "+repo.getFullName());
      throw ex;
    }
  }
}
