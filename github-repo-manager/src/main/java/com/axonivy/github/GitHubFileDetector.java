package com.axonivy.github;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHRepository;

public abstract class GitHubFileDetector {

	protected static final String GITHUB_ORG = ".github";
	protected final byte[] fileContent;
	protected boolean isMissingRequiredFile = false;
	protected List<String> workingRepos = List.of("axonivy", "axonivy-market");

	protected abstract String getRequiredFilePath();

	protected abstract String getPullRequestTitle();

	protected abstract String getBranchName();

	protected abstract String getCommitMessage();

	public GitHubFileDetector() throws IOException {
		try (var is = GitHubFileDetector.class.getResourceAsStream(getRequiredFilePath())) {
			if (is == null) {
				throw new IOException(getRequiredFilePath() + " file not found");
			}
			this.fileContent = IOUtils.toByteArray(is);
		}
	}

	protected void checkMissingFile() throws IOException {
		var github = GitHubProvider.get();
		printMessage("Working on organizations: {0}.", workingRepos);
		for (var orgName : workingRepos) {
			var org = github.getOrganization(orgName);
			for (var repo : List.copyOf(org.getRepositories().values())) {
				checkMissingFile(repo);
			}
		}
		if (isMissingRequiredFile) {
			printMessage("At least one repository has no {0}.", getRequiredFilePath());
			printMessage("Add a {0} manually or run the build without DRYRUN to add {0} to the repository.",
					getRequiredFilePath());
			System.exit(-1);
		}
		System.exit(0);
	}

	protected void checkMissingFile(GHRepository repo) throws IOException {
		if (repo.isPrivate() || repo.isArchived()) {
			printMessage("Repo {0} is {1}.", repo.getFullName(), repo.isPrivate() ? "private" : "archived");
			return;
		}

		var foundFile = getFileContent(getRequiredFilePath(), repo);
		if (foundFile != null) {
			if (!hasSimilarFilePaths(foundFile)) {
				printMessage("Repo {0} has {1} but no {2} file instead it has {3}.", repo.getFullName(),
						foundFile.getName(), getRequiredFilePath(), foundFile.getPath());
				isMissingRequiredFile = true;
			} else {
				printMessage("Repo {0} has {1}.", repo.getFullName(), foundFile.getName());
			}
		} else if (!GITHUB_ORG.equals(repo.getName())) {
			addMissingFile(repo);
		}
	}

	protected GHContent getFileContent(String path, GHRepository repo) {
		try {
			return repo.getFileContent(path);
		} catch (Exception e) {
			printMessage("File {0} in repo {1} is not found.", path, repo.getFullName());
			return null;
		}
	}

	protected boolean hasSimilarFilePaths(GHContent existingFile) {
		return existingFile.getPath().equals(getRequiredFilePath());
	}

	protected void addMissingFile(GHRepository repo) throws IOException {
		try {
			if (DryRun.is()) {
				isMissingRequiredFile = true;
				printMessage("DRYRUN: ");
			} else {
				var defaultBranch = repo.getBranch(repo.getDefaultBranch());
				var sha1 = defaultBranch.getSHA1();
				repo.createRef("refs/heads/" + getBranchName(), sha1);
				repo.createContent().branch(getBranchName()).path(getRequiredFilePath()).content(fileContent)
						.message(getCommitMessage()).commit();
				var pr = repo.createPullRequest(getPullRequestTitle(), getBranchName(), repo.getDefaultBranch(), "");
				pr.merge(getCommitMessage());
			}
			printMessage("Repo {0} {1} added.", repo.getFullName(), getRequiredFilePath());
		} catch (IOException ex) {
			printMessage("Cannot add {0} to repo {1}.", repo.getFullName(), getRequiredFilePath());
			throw ex;
		}
	}

	protected void printMessage(String pattern, Object... arguments) {
		System.err.println(MessageFormat.format(pattern, arguments));
	}
}
