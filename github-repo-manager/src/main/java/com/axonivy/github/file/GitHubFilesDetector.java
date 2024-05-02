package com.axonivy.github.file;

import java.io.IOException;
import java.io.Reader;
import java.text.MessageFormat;
import java.util.List;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.CharSequenceReader;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;

import com.axonivy.github.DryRun;
import com.axonivy.github.GitHubProvider;
import com.axonivy.github.file.GitHubFiles.FileMeta;

public class GitHubFilesDetector {

  private static final String GITHUB_ORG = ".github";
  private boolean isMissingRequiredFile;
  private final List<String> workingRepos;
  private final FileReference reference;

  public GitHubFilesDetector(FileMeta fileMeta, List<String> workingRepos) throws IOException {
    Objects.requireNonNull(fileMeta);
    Objects.requireNonNull(workingRepos);
    this.reference = new FileReference(fileMeta);
    this.workingRepos = workingRepos;
  }

  public int missingFile() throws IOException {
    var github = GitHubProvider.get();
    printInfoMessage("Working on organizations: {0}.", workingRepos);
    for (var orgName : workingRepos) {
      var org = github.getOrganization(orgName);
      for (var repo : List.copyOf(org.getRepositories().values())) {
        missingFile(repo);
      }
    }
    if (isMissingRequiredFile) {
      printErrorMessage("At least one repository has no {0}.", reference.meta().filePath());
      printErrorMessage("Add a {0} manually or run the build without DRYRUN to add {0} to the repository.",
          reference.meta().filePath());
      return -1;
    }
    return 0;
  }

  private void missingFile(GHRepository repo) throws IOException {
    if (GITHUB_ORG.equals(repo.getName())) {
      return;
    }
    if (repo.isPrivate() || repo.isArchived()) {
      printInfoMessage("Repo {0} is {1}.", repo.getFullName(), repo.isPrivate() ? "private" : "archived");
      return;
    }

    var foundFile = getFileContent(reference.meta().filePath(), repo);
    if (foundFile != null) {
      if (!hasSimilarContent(foundFile)) {
        printInfoMessage("Repo {0} has {1} but the content is different from required file {2}.", repo.getFullName(),
            foundFile.getName(), reference.meta().filePath());
        isMissingRequiredFile = true;
      } else {
        printInfoMessage("Repo {0} has {1}.", repo.getFullName(), foundFile.getName());
      }
    } else {
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

  private boolean hasSimilarContent(GHContent existingFile) throws IOException {
    Reader targetContent = new CharSequenceReader(new String(reference.content()));
    Reader actualContent = new CharSequenceReader(new String(existingFile.read().readAllBytes()));
    return IOUtils.contentEqualsIgnoreEOL(targetContent, actualContent);
  }

  private void addMissingFile(GHRepository repo) throws IOException {
    try {
      if (DryRun.is()) {
        isMissingRequiredFile = true;
        printInfoMessage("DRYRUN: ");
      } else {
        var defaultBranch = repo.getBranch(repo.getDefaultBranch());
        var sha1 = defaultBranch.getSHA1();
        repo.createRef("refs/heads/" + reference.meta().branchName(), sha1);
        repo.createContent().branch(reference.meta().branchName()).path(reference.meta().filePath()).content(reference.content())
            .message(reference.meta().commitMessage()).commit();
        var pr = repo.createPullRequest(reference.meta().pullRequestTitle(), reference.meta().branchName(), repo.getDefaultBranch(), "");
        pr.merge(reference.meta().commitMessage());
      }
      printInfoMessage("Repo {0} {1} added.", repo.getFullName(), reference.meta().filePath());
    } catch (IOException ex) {
      printErrorMessage("Cannot add {0} to repo {1}.", repo.getFullName(), reference.meta().filePath());
      throw ex;
    }
  }

  private void printInfoMessage(String pattern, Object... arguments) {
    System.out.println(MessageFormat.format(pattern, arguments));
  }

  private void printErrorMessage(String pattern, Object... arguments) {
    System.err.println(MessageFormat.format(pattern, arguments));
  }
}
