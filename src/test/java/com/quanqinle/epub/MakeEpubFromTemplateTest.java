package com.quanqinle.epub;

import com.quanqinle.epub.entity.BookInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author quanqinle
 */
class MakeEpubFromTemplateTest {

  @BeforeEach
  void setUp() {}

  @AfterEach
  void tearDown() {}

  @Test
  void make() {

    // 源文件
    Path srcFilePath = Paths.get("D:", "book-library", "demo.txt");

    // 目标文件
    BookInfo book = new BookInfo();
    book.setOutputDir(Paths.get("D:", "epub"));

    /* 以下属性非必须 */
    book.setBookTitle("红楼梦");
    book.setAuthor("曹雪芹");
    // 设置 jpg 格式的封面图
    book.setCoverJpgFullPath(Paths.get("D:", "book.jpg"));

    // 建议设置上面3个

    book.setUUID(UUID.randomUUID().toString());
    book.setLanguage("zh");
    book.setCreateDate("2021-03-06");

    // make srcFilePath to a .epub book
    MakeEpubFromTemplate makeEpub = new MakeEpubFromTemplate(srcFilePath, book);
    try {
      makeEpub.make();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void zipEpub() {}

}
