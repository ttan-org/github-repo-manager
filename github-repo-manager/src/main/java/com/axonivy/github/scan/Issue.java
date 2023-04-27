package com.axonivy.github.scan;

import java.util.Comparator;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Issue implements Comparable<Issue> {

  private static final Comparator<Issue> ASC = Comparator.comparing(Issue::project).thenComparing(Issue::number);

  private final String project;
  private final int number;

  public Issue(String project, int number) {
    this.project = project;
    this.number = number;
  }

  public static final Issue fromString(String issue) {
    return new Issue(
        StringUtils.substringBefore(issue, "-").toUpperCase(),
        Integer.parseInt(StringUtils.substringAfter(issue, "-")));
  }

  public String project() {
    return project;
  }

  public int number() {
    return number;
  }

  @Override
  public String toString() {
    return project+"-"+number;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || obj.getClass() != Issue.class) {
      return false;
    }
    var other = (Issue)obj;
    return number == other.number &&
           Objects.equals(project, other.project);
  }

  @Override
  public int hashCode() {
    return new HashCodeBuilder()
        .append(number)
        .append(project)
        .toHashCode();
  }

  @Override
  public int compareTo(Issue other) {
    return ASC.compare(this, other);
  }
}
