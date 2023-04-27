package com.axonivy.github;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;

import com.axonivy.github.scan.Issue;
import com.axonivy.github.scan.ScanIssueReporter;

public class GitHubIssueScanner {

  public static void main(String[] args) throws IOException {
    if (args.length != 4) {
      throw new IllegalArgumentException("Wrong number of params (4) got " +args.length+": tagVersion branch releaseNotesFile outputFile");
    }
    var tagVersion = args[0];// "8.0.25";
    var branchName = args[1];
    var releaseNotesFile = args[2];
    var outputFile = args[3];

    if (StringUtils.isEmpty(tagVersion)) {
      throw new IllegalArgumentException("version not set");
    }

    var rnIssues = scanReleaseNotesIssues(releaseNotesFile);
    var tagName = "v" + tagVersion;
    var reporter = new ScanIssueReporter(Paths.get(outputFile));
    var logIssues = scanLogIssues(tagVersion, branchName, tagName, reporter);
    reporter.report(tagName, logIssues, rnIssues);
  }

  private static HashSet<Issue> scanLogIssues(String version, String branchName, String tagName,
          ScanIssueReporter reporter) throws IOException {
    reporter.print("Start scanning issues ...");
    var github = GitHubProvider.get();
    if (StringUtils.isBlank(branchName)) {
      branchName = "release/" + StringUtils.substringBeforeLast(version, ".");
    }
    var issues = new HashSet<Issue>();
    for (var repoName : GitHubRepos.repos(version)) {
      var repo = github.getRepository("axonivy/" + repoName);

      reporter.setRepo(repo);
      reporter.print("Start scanning");
      var since = findSince(repo, tagName);
      if (since == null) {
        reporter.print("Skipping repo because there is no tag " + tagName);
        continue;
      }

      var until = findUntil(repo, branchName);
      if (until == null) {
        reporter.print("Skipping repo because there is no branch " + branchName);
        continue;
      }

      var issuesFound = collectIssues(repo, branchName, since, until);
      if (issuesFound.isEmpty()) {
        reporter.print("No issues found");
      } else {
        var issueList = issuesFound.stream().sorted().map(Issue::toString).collect(Collectors.joining(", ", "[", "]"));
        reporter.print("Found " + issuesFound.size() + " issues " + issueList);
      }
      issues.addAll(issuesFound);
    }
    return issues;
  }

  private static Set<Issue> scanReleaseNotesIssues(String releaseNotesFile) throws IOException {
    return Files.readAllLines(Path.of(releaseNotesFile))
        .stream()
        .map(Issue::fromString)
        .collect(Collectors.toSet());
  }

  private static Set<Issue> collectIssues(GHRepository repo, String branchName, Date since, Date until) throws IOException {
    var issues = new HashSet<Issue>();
    for (var commit : repo.queryCommits().from(branchName).since(since).until(until).list()) {
      var title = parseTitle(commit);
      if (title.toLowerCase().startsWith("xivy-")) {
        var issue = StringUtils.substringBefore(title, " ");
        issues.add(Issue.fromString(issue));
      }
    }
    return issues;
  }

  private static String parseTitle(GHCommit commit) throws IOException {
    return commit.getCommitShortInfo().getMessage().lines().limit(1).collect(Collectors.joining());
  }

  private static Date findSince(GHRepository repo, String tagName) throws IOException {
    var tags = repo.listTags().iterator();
    while (tags.hasNext()) {
      var tag = tags.next();
      if (!tagName.equals(tag.getName())) {
        continue;
      }
      var time =  tag.getCommit().getCommitDate().getTime();
      return new Date(time + 1000); // add one second to have not the commit of this tag itself
    }
    return null;
  }

  private static Date findUntil(GHRepository repo, String branchName) throws IOException {
    if (!repo.getBranches().keySet().contains(branchName)) {
      return null;
    }

    var branch = repo.getBranch(branchName);
    var sha1 = branch.getSHA1();
    return repo.getCommit(sha1).getCommitDate();
  }
}
