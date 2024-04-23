package com.axonivy.github.file;

import static com.axonivy.github.file.GitHubFiles.CODE_OF_CONDUCT;
import static com.axonivy.github.file.GitHubFiles.LICENSE;
import static com.axonivy.github.file.GitHubFiles.SECURITY;

import java.io.IOException;
import java.util.List;

import com.axonivy.github.file.GitHubFiles.FileMeta;

public class GitHubMissingFiles {

  private static final List<String> WORKING_ORGANIZATIONS = List.of("axonivy", "axonivy-market");
  private static final List<FileMeta> REQUIRED_FILES = List.of(LICENSE, SECURITY, CODE_OF_CONDUCT);

  public static void main(String[] args) throws IOException {
    int missingStatus = 0;
    for (var fileMeta : REQUIRED_FILES) {
      var githubMissingFiles = new GitHubMissingFilesDetector(fileMeta, WORKING_ORGANIZATIONS);
      var returnedStatus = githubMissingFiles.checkMissingFile();
      missingStatus = returnedStatus != 0 ? returnedStatus : 0;
    }
    System.exit(missingStatus);
  }

}
