package com.axonivy.github.file;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;

import com.axonivy.github.DryRun;
import com.axonivy.github.GitHubProvider;

public class GitHubMissingFilesDetector {

  private static final String GITHUB_ORG = ".github";
  private final byte[] fileContent;
  private boolean isMissingRequiredFile;
  private List<String> workingRepos;
  private FileMeta fileMeta;

  public GitHubMissingFilesDetector(FileMeta fileMeta, List<String> workingRepos) throws IOException {
    Objects.requireNonNull(fileMeta);
    Objects.requireNonNull(workingRepos);
    this.fileMeta = fileMeta;
    this.workingRepos = workingRepos;
    try (var is = GitHubMissingFilesDetector.class.getResourceAsStream(fileMeta.requiredFilePath)) {
      if (is == null) {
        throw new IOException(fileMeta.requiredFilePath + " file not found");
      }
      this.fileContent = IOUtils.toByteArray(is);
    }
  }

  public int checkMissingFile() throws IOException {
    var github = GitHubProvider.get();
    printInfoMessage("Working on organizations: {0}.", workingRepos);
    for (var orgName : workingRepos) {
      var org = github.getOrganization(orgName);
      for (var repo : List.copyOf(org.getRepositories().values())) {
        checkMissingFile(repo);
      }
    }
    if (isMissingRequiredFile) {
      printErrorMessage("At least one repository has no {0}.", fileMeta.requiredFilePath);
      printErrorMessage("Add a {0} manually or run the build without DRYRUN to add {0} to the repository.",
          fileMeta.requiredFilePath);
      return -1;
    }
    return 0;
  }

  private void checkMissingFile(GHRepository repo) throws IOException {
    if (repo.isPrivate() || repo.isArchived()) {
      printInfoMessage("Repo {0} is {1}.", repo.getFullName(), repo.isPrivate() ? "private" : "archived");
      return;
    }

    var foundFile = getFileContent(fileMeta.requiredFilePath, repo);
    if (foundFile != null) {
      if (!hasSimilarFilePaths(foundFile)) {
        printInfoMessage("Repo {0} has {1} but no {2} file instead it has {3}.", repo.getFullName(),
            foundFile.getName(), fileMeta.requiredFilePath, foundFile.getPath());
        isMissingRequiredFile = true;
      } else {
        printInfoMessage("Repo {0} has {1}.", repo.getFullName(), foundFile.getName());
      }
    } else if (!GITHUB_ORG.equals(repo.getName())) {
      addMissingFile(repo);
    }
  }

  private GHContent getFileContent(String path, GHRepository repo) {
    try {
      return repo.getFileContent(path);
    } catch (Exception e) {
      printErrorMessage("File {0} in repo {1} is not found.", path, repo.getFullName());
      return null;
    }
  }

  private boolean hasSimilarFilePaths(GHContent existingFile) {
    return existingFile.getPath().equals(fileMeta.requiredFilePath);
  }

  private void addMissingFile(GHRepository repo) throws IOException {
    try {
      if (DryRun.is()) {
        isMissingRequiredFile = true;
        printInfoMessage("DRYRUN: ");
      } else {
        var defaultBranch = repo.getBranch(repo.getDefaultBranch());
        var sha1 = defaultBranch.getSHA1();
        repo.createRef("refs/heads/" + fileMeta.branchName, sha1);
        repo.createContent().branch(fileMeta.branchName).path(fileMeta.requiredFilePath).content(fileContent)
            .message(fileMeta.commitMessage).commit();
        var pr = repo.createPullRequest(fileMeta.pullRequestTitle, fileMeta.branchName, repo.getDefaultBranch(), "");
        pr.merge(fileMeta.commitMessage);
      }
      printInfoMessage("Repo {0} {1} added.", repo.getFullName(), fileMeta.requiredFilePath);
    } catch (IOException ex) {
      printErrorMessage("Cannot add {0} to repo {1}.", repo.getFullName(), fileMeta.requiredFilePath);
      throw ex;
    }
  }

  private void printInfoMessage(String pattern, Object... arguments) {
    System.out.println(MessageFormat.format(pattern, arguments));
  }

  private void printErrorMessage(String pattern, Object... arguments) {
    System.err.println(MessageFormat.format(pattern, arguments));
  }

  public record FileMeta(String requiredFilePath, String pullRequestTitle, String branchName, String commitMessage) { }

}
