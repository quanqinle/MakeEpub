package com.quanqinle.epub.util;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

class EpubUtilsTest {

  @Test
  void zipEpub() {
    Path epubSrcFolderPath = Paths.get("D:", "book-template");
    Path epubFilePath = Paths.get("D:", "my-book.epub");

    try {
      EpubUtils.zipEpub(epubSrcFolderPath, epubFilePath);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  void validateEpubFile() {
    Path epubFilePath = Paths.get("D:", "my-book.epub");
    assertTrue(EpubUtils.validateEpubFile(epubFilePath.toFile()));
  }

}
