package com.axonivy.github.file;

import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.axonivy.github.file.GitHubFiles.FileMeta;


public record FileReference(FileMeta meta, byte[] content) {

  public FileReference(FileMeta meta) throws IOException {
    this(meta, load(meta));
  }

  private static byte[] load(FileMeta meta) throws IOException {
    try (var is = GitHubMissingFilesDetector.class.getResourceAsStream(meta.filePath())) {
      if (is == null) {
        throw new IOException(meta.filePath() + " file not found");
      }
      return IOUtils.toByteArray(is);
    }
  }

}
