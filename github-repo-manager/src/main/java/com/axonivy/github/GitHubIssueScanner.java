package com.axonivy.github;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;

import com.axonivy.github.scan.ScanIssueReporter;

public class GitHubIssueScanner {

  public static void main(String[] args) throws IOException {
    var input = args[0];// "8.0.25";
    var outputFile = args[1];

    if (StringUtils.isEmpty(input)) {
      throw new IllegalArgumentException("version not set");
    }

    var reporter = new ScanIssueReporter(Paths.get(outputFile));
    reporter.print("Start scanning issues ...");
    var github = GitHubProvider.get();
    var tagName = "v" + input;
    var branchName = "release/" + StringUtils.left(input, 3);
    var issues = new HashSet<String>();
    for (var repoName : GitHubRepos.repos(input)) {
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
        reporter.print("Found " + issuesFound.size() + " issues " + issuesFound.stream().collect(Collectors.joining(", ", "[", "]")));
      }
      issues.addAll(issuesFound);
    }
    reporter.report(tagName, issues);
  }

  private static Set<String> collectIssues(GHRepository repo, String branchName, Date since, Date until) throws IOException {
    var issues = new HashSet<String>();
    for (var commit : repo.queryCommits().from(branchName).since(since).until(until).list()) {
      var title = parseTitle(commit);
      if (title.toLowerCase().startsWith("xivy-")) {
        var issue = StringUtils.substringBefore(title, " ");
        issues.add(issue);
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
