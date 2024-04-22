package com.axonivy.github.file;

import java.io.IOException;
import java.util.List;

import com.axonivy.github.file.GitHubMissingFilesDetector.FileMeta;

public class GitHubMissingFiles {

  private static final List<String> WORKING_REPOS = List.of("axonivy", "axonivy-market");
  private static final FileMeta LICENSE = new FileMeta("LICENSE", "Add license", "Add_License",
      "Add Apache 2.0 license");
  private static final FileMeta SECURITY = new FileMeta("SECURITY.md", "Add security file", "Add_Security",
      "Add Security.md file to repo");
  private static final FileMeta CODE_OF_CONDUCT = new FileMeta("CODE_OF_CONDUCT.md", "Add code of conduct file",
      "Add_Code_of_Conduct", "Add CODE_OF_CONDUCT.md file to repo");

  public static void main(String[] args) throws IOException {
    int missingStatus = 0;
    for (var fileMeta : List.of(LICENSE, SECURITY, CODE_OF_CONDUCT)) {
      var githubMissingFiles = new GitHubMissingFilesDetector(fileMeta, WORKING_REPOS);
      var returnedStatus = githubMissingFiles.checkMissingFile();
      missingStatus = returnedStatus != 0 ? returnedStatus : 0;
    }
    System.exit(missingStatus);
  }

}
