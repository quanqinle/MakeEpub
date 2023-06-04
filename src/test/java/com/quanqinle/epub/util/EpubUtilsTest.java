package com.quanqinle.epub.util;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

class EpubUtilsTest {
public static void main(String[] args){
  Path epubSrcFolderPath = Paths.get("C:", "MyData", "epub3","epub-temp");
  Path epubFilePath = Paths.get("C:", "MyData","《阿里布达年代祭》.epub");

  try {
    EpubUtils.zipEpub(epubSrcFolderPath, epubFilePath);
  } catch (IOException e) {
    e.printStackTrace();
  }
}
  @Test
  void zipEpub() {
    Path epubSrcFolderPath = Paths.get("C:", "MyData", "epub","epub-temp");
    Path epubFilePath = Paths.get("C:", "MyData","《亵渎》.epub");

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
