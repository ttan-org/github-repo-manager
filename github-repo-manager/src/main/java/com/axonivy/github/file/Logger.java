package com.axonivy.github.file;

import java.text.MessageFormat;

class Logger {

  void info(String pattern, Object... arguments) {
    System.out.println(MessageFormat.format(pattern, arguments));
  }

  void error(String pattern, Object... arguments) {
    System.err.println(MessageFormat.format(pattern, arguments));
  }
}