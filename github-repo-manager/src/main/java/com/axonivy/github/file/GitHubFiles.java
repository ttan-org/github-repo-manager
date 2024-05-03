package com.axonivy.github.file;

public interface GitHubFiles {

  FileMeta LICENSE = new FileMeta("LICENSE", "Add license", "Add_License",
      "Add Apache 2.0 license");
  FileMeta SECURITY = new FileMeta("SECURITY.md", "Add security file", "Add_Security",
      "Add Security.md file to repo");
  FileMeta CODE_OF_CONDUCT = new FileMeta("CODE_OF_CONDUCT.md", "Remove yet unaligned code of conduct file", "Remove_Code_of_Conduct",
      "Remove CODE_OF_CONDUCT.md file from repo until aligned");

  public record FileMeta(String filePath, String pullRequestTitle, String branchName, String commitMessage) {

    public String filePath() {
      return filePath;
    }

    public String pullRequestTitle() {
      return pullRequestTitle;
    }

    public String branchName() {
      return branchName;
    }

    public String commitMessage() {
      return commitMessage;
    }
  }

}
