package com.axonivy.github.scan;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class TestIssue {

  @Test
  void fromString_noMatch() {
    assertThat(Issue.fromString("")).isEmpty();
    assertThat(Issue.fromString("Hello-1234")).isEmpty();
    assertThat(Issue.fromString("XIVY-gugus")).isEmpty();
  }

  @Test
  void fromString_oneMatch() {
    assertThat(Issue.fromString("XIVY-1")).contains(new Issue("XIVY", 1));
    assertThat(Issue.fromString("XIVY-123456")).contains(new Issue("XIVY", 123456));
    assertThat(Issue.fromString("prefixXIVY-123suffix")).contains(new Issue("XIVY", 123));
  }

  @Test
  void fromString_moreMatches() {
    assertThat(Issue.fromString("XIVY-1XIVY-2")).contains(new Issue("XIVY", 1), new Issue("XIVY", 2));
    assertThat(Issue.fromString("blah XIVY-123 gugus XIVY-456 sugus XIVY-789")).contains(new Issue("XIVY", 123), new Issue("XIVY", 456), new Issue("XIVY", 789));
  }
}
