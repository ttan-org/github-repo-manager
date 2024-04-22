package com.axonivy.github;

import java.io.IOException;

public class GitHubMissingCodeOfConductDetector extends GitHubFileDetector {

	public static void main(String[] args) throws IOException {
		new GitHubMissingCodeOfConductDetector().checkMissingFile();
	}

	public GitHubMissingCodeOfConductDetector() throws IOException {
		super();
	}

	@Override
	protected String getRequiredFilePath() {
		return "CODE_OF_CONDUCT.md";
	}

	@Override
	protected String getPullRequestTitle() {
		return "Add code of conduct file";
	}

	@Override
	protected String getBranchName() {
		return "Add_Code_of_Conduct";
	}

	@Override
	protected String getCommitMessage() {
		return "Add CODE_OF_CONDUCT.md file to repo";
	}
}
