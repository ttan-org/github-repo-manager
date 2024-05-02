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
  private boolean isNotSync;
  private final FileReference reference;

  public GitHubFilesDetector(FileMeta fileMeta) throws IOException {
    Objects.requireNonNull(fileMeta);
    this.reference = new FileReference(fileMeta);
  }

  public int requireFile(List<String> orgNames) throws IOException {
    Objects.requireNonNull(orgNames);
    var github = GitHubProvider.get();
    printInfoMessage("Working on organizations: {0}.", orgNames);
    for (var orgName : orgNames) {
      var org = github.getOrganization(orgName);
      for (var repo : List.copyOf(org.getRepositories().values())) {
        missingFile(repo);
      }
    }
    if (isNotSync) {
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
        isNotSync = true;
      } else {
        printInfoMessage("Repo {0} has {1}.", repo.getFullName(), foundFile.getName());
      }
    } else {
      handleMissingFile(repo);
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

  private void handleMissingFile(GHRepository repo) throws IOException {
    try {
      if (DryRun.is()) {
        isNotSync = true;
        printInfoMessage("DRYRUN: ");
      } else {
        addMissingFile(repo);
      }
      printInfoMessage("Repo {0} {1} synced.", repo.getFullName(), reference.meta().filePath());
    } catch (IOException ex) {
      printErrorMessage("Cannot add {0} to repo {1}.", repo.getFullName(), reference.meta().filePath());
      throw ex;
    }
  }

  private void addMissingFile(GHRepository repo) throws IOException {
    var defaultBranch = repo.getBranch(repo.getDefaultBranch());
    var sha1 = defaultBranch.getSHA1();
    repo.createRef("refs/heads/" + reference.meta().branchName(), sha1);
    repo.createContent().branch(reference.meta().branchName()).path(reference.meta().filePath()).content(reference.content())
        .message(reference.meta().commitMessage()).commit();
    var pr = repo.createPullRequest(reference.meta().pullRequestTitle(), reference.meta().branchName(), repo.getDefaultBranch(), "");
    pr.merge(reference.meta().commitMessage());
  }

  private void printInfoMessage(String pattern, Object... arguments) {
    System.out.println(MessageFormat.format(pattern, arguments));
  }

  private void printErrorMessage(String pattern, Object... arguments) {
    System.err.println(MessageFormat.format(pattern, arguments));
  }
}
