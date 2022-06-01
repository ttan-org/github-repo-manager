package com.axonivy.github.scan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Set;
import java.util.stream.Collectors;

import org.kohsuke.github.GHRepository;

public class ScanIssueReporter {

  private final Path path;
  private GHRepository repo;

  public ScanIssueReporter(Path path) {
    this.path = path;
  }

  public void setRepo(GHRepository repo) {
    this.repo = repo;
  }

  public void print(String message) {
    try {
      if (!Files.exists(path)) {
        Files.createFile(path);
      }
      var msg = repo == null ? message : "<b>" + repo.getFullName() + "</b>: " + message;
      msg = msg + "<br />";
      Files.writeString(path, msg, StandardOpenOption.APPEND);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

  public void report(String tag, Set<String> issues) {
    repo = null;

    var sortedIssues = issues.stream()
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .collect(Collectors.toList());
    print("");
    print("");
    print("<b>Issues Report</b>");
    if (sortedIssues.isEmpty()) {
      print("Found no issues since tag " + tag);
    } else {
      print("Found " + sortedIssues.size() + " issues since tag " + tag);
      print("--------------------------------------------------");
      for (var issue : sortedIssues) {
        print("<a target=\"_blank\" href=\"https://axonivy.atlassian.net/browse/" + issue + "\">" + issue + "</a>");
      }
    }
  }
}
