package com.quanqinle.epub;

import com.quanqinle.epub.entity.BookInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

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
    Path srcFilePath =
        Paths.get("C:\\Users\\quanql\\Desktop\\《亵渎》烟雨江南.txt");

    // 目标文件
    BookInfo book = new BookInfo();
    book.setOutputDir(Paths.get("c:","MyData", "epub"));
    book.setHasManyBooks(true);

    /* 以下属性非必须 */
    book.setBookTitle("《亵渎》");
    book.setAuthor("烟雨江南");
    // 设置 jpg 格式的封面图
//    book.setCoverJpgFullPath(Paths.get("D:", "book.jpg"));

    // 建议设置上面3个

    book.setUUID(UUID.randomUUID().toString());
    book.setLanguage("zh");
    book.setCreateDate("2023-04-07");

    // make srcFilePath to a .epub book
    MakeEpubFromTemplate makeEpub = new MakeEpubFromTemplate(srcFilePath, book);
    try {
      makeEpub.make();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void zipEpub() {

  }

}
