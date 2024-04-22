package com.axonivy.github;

import java.io.IOException;

public class GitHubMissingSecurityDetector extends GitHubFileDetector {

	public static void main(String[] args) throws IOException {
		new GitHubMissingSecurityDetector().checkMissingFile();
	}

	public GitHubMissingSecurityDetector() throws IOException {
		super();
	}

	@Override
	protected String getRequiredFilePath() {
		return "SECURITY.md";
	}

	@Override
	protected String getPullRequestTitle() {
		return "Add security file";
	}

	@Override
	protected String getBranchName() {
		return "Add_Security";
	}

	@Override
	protected String getCommitMessage() {
		return "Add Security.md file to repo";
	}
}
