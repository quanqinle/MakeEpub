package com.quanqinle.epub;

import com.quanqinle.epub.entity.BookInfo;
import org.junit.jupiter.api.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author quanqinle
 */
class ConvertPlainTxtToHtmlFilesTest {

  @BeforeEach
  void setUp() {}

  @AfterEach
  void tearDown() {}

  @Test
  void convert() {
    BookInfo book = new BookInfo();
    book.setOutputDir(Paths.get("D:", "epub"));
    book.setCoverJpgFullPath(Paths.get("D:", "book.jpg"));
    book.setUUID(UUID.randomUUID().toString());
    book.setLanguage("zh");
    book.setBookTitle("红楼梦");
    book.setAuthor("曹雪芹");
    book.setCreateDate("2021-03-06");

    Path srcFilePath = Paths.get("D:", "book-library", "demo.txt");

    ConvertPlainTxtToHtmlFiles parse = new ConvertPlainTxtToHtmlFiles(srcFilePath, book);
    try {
      parse.convert();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void setRegexChapterTitle() {}
}
