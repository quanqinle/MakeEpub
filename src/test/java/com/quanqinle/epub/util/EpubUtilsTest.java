package com.quanqinle.epub.util;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

class EpubUtilsTest {

  @Test
  void zipEpub() {}

  @Test
  void validateEpubFile() {
    Path epubSrcFolderPath = Paths.get("D:", "book-template");
    Path epubFilePath = Paths.get("D:", "book-library", "my-book.epub");

    try {
      EpubUtils.zipEpub(epubSrcFolderPath, epubFilePath);
    } catch (IOException e) {
      e.printStackTrace();
    }
    EpubUtils.validateEpubFile(epubFilePath.toFile());
  }
}
