package com.quanqinle.epub;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.quanqinle.epub.entity.BookInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
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
  void makeByBookInfoInYaml() throws IOException {

    // 书的配置文件
    ObjectMapper mapper = new YAMLMapper();
    BookInfo book = mapper.readValue(Files.newInputStream(Path.of("bookinfo.yaml")), BookInfo.class);

    // make srcFilePath to a .epub book
    MakeEpubFromTemplate makeEpub = new MakeEpubFromTemplate(book);
    makeEpub.make();
  }

  @Test
  void make() {

    BookInfo book = new BookInfo();
    // 源文件
    book.setSrcTxtPath(Paths.get("D:", "红楼梦.txt"));
    // 目标文件
    book.setOutputDir(Paths.get("D:","demo"));
    book.setHasManyBooks(true);

    /* 以下属性非必须 */
    book.setBookTitle("红楼梦");
    book.setAuthor("曹雪芹");
    // 设置 jpg 格式的封面图
    book.setCoverJpgFullPath(Paths.get("D:", "book.jpg"));

    // 建议设置上面3个

    book.setUuid(UUID.randomUUID().toString());
    book.setLanguage("zh");
    book.setCreateDate("2023-04-07");

    // make srcFilePath to a .epub book
    MakeEpubFromTemplate makeEpub = new MakeEpubFromTemplate(book);
    try {
      makeEpub.make();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
