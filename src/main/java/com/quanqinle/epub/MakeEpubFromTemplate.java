package com.quanqinle.epub;

import com.quanqinle.epub.entity.BookInfo;
import com.quanqinle.epub.entity.FileInfo;
import com.quanqinle.epub.util.Constant;
import com.quanqinle.epub.util.EpubUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.LinkedHashMap;

/**
 * Make the EPUB using the existing template
 *
 * @author quanqinle
 */
public class MakeEpubFromTemplate {

  private static final Logger logger = LoggerFactory.getLogger(MakeEpubFromTemplate.class);

  private final BookInfo book;

  /** the full path of epub template, copy from it, do not modify the original files */
  private Path templateSrcPath;
  /** the temp path is inside output path. copy epub template to this folder, then modify files inside it */
  private final Path tempPath;

  /** `&lt;navPoint>&lt;/navPoint>` list in `toc.ncx`, for making toc tree */
  private String navPointList = "";
  /** `&lt;item\>&lt;/item>` list in `content.opf` */
  private String itemList = "";
  /** `&lt;itemref>&lt;/itemref>` list in `content.opf` */
  private String itemrefList = "";
  /** `&lt;reference>&lt;/reference>` list in `content.opf` */
  private String referenceList = "";
  /**
   * `&lt;li>&lt;/li>` list in `toc.xhtml`
   */
  private String tocItemList = "";

  /**
   * Constructor
   *
   * @param bookInfo output book
   */
  public MakeEpubFromTemplate(BookInfo bookInfo) {
    this.book = bookInfo;
    this.tempPath = book.getOutputDir().resolve(bookInfo.getTempFolder());

    if (bookInfo.getTemplateSrcPath() != null) {
      this.templateSrcPath = bookInfo.getTemplateSrcPath();
    } else {
      try {
        // The codes below will run error, if execute jar created from this project which resource folder in it.
        URL templateSrcUrl = getClass().getClassLoader().getResource(Constant.TEMPLATE_FOLDER);
        assert templateSrcUrl != null;
        this.templateSrcPath = Paths.get(templateSrcUrl.toURI());
      } catch (URISyntaxException e) {
        logger.error("Fail to find the epub source template: {}", this.templateSrcPath);
        System.exit(0);
      }
    }
  }

  /**
   * Start to make the .epub file.
   * <p>
   * The method is an all-in-one method, it includes the whole steps of read-parse-rewrite, etc.,
   * so you can use it to make a .epub book just after the Constructor() method.
   *
   * @throws IOException -
   */
  public void make() throws IOException {
    copyTemplateToTempPath();
    genBodyHtmls();

    makeContentForTocNcxAndContentOpf();
    setBookCover();
    setBookTocHtml();
    modifyTocNcx();
    modifyContentOpf();

    zipEpub();
  }

  /**
   * Zip folder to epub.
   * <p>
   * Use {@link com.quanqinle.epub.util.EpubUtils#zipEpub(Path epubSrcFolderPath, Path epubFilePath)}.
   * Folder is from the copy of original template.
   * epub is from book.outputDir+book.title+'.epub'.
   *
   * @throws IOException -
   */
  private void zipEpub() throws IOException {
    EpubUtils.zipEpub(this.tempPath, book.getOutputDir().resolve(book.getBookTitle() + ".epub"));
  }

  /**
   * Copy epub source template to the output directory.
   * This method will delete (if exist) and create the output directory.
   */
  private void copyTemplateToTempPath() throws IOException {

    Path src = templateSrcPath;
    Path dst = tempPath;
    // Files.walk(src).forEach(System.out::println);

    logger.info("source template = {}", src);
    logger.info("output folder = {}", dst);

    if (Files.exists(dst)) {
      Files.walk(dst)
              .sorted(Comparator.reverseOrder())
              .map(Path::toFile)
              .forEach(File::delete);
    }
    Files.createDirectories(dst);

    Files.walk(src)
        .forEach(
            source -> {
              try {
                Files.copy(
                    source,
                    dst.resolve(src.relativize(source)),
                    StandardCopyOption.REPLACE_EXISTING);
              } catch (IOException e) {
                e.printStackTrace();
              }
            });
  }

  /**
   * Modify cover.xhtml
   *
   * @throws IOException -
   */
  private void setBookCover() throws IOException {
    Path coverHtml = tempPath.resolve("OEBPS/Text/cover.xhtml");
    String content = Files.readString(coverHtml);
    content = content.replace("[BOOK'S TITLE]", book.getBookTitle());
    Files.writeString(coverHtml, content);

    Path src = book.getCoverJpgFullPath();
    Path dst = tempPath.resolve("OEBPS/Images/cover.jpg");
    if (src != null && Files.exists(src)) {
      Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
    } else {
      // TODO maybe we should create a jpg based on the book title
    }
  }

  /**
   * Modify toc.xhtml
   *
   * @throws IOException -
   */
  private void setBookTocHtml() throws IOException {
    Path tocHtml = tempPath.resolve("OEBPS/Text/toc.xhtml");
    String content = Files.readString(tocHtml);
    content =
        content.replace("[toc item]", this.tocItemList)
                .replace("[TOC TITLE]", book.getTocTitle());
    Files.writeString(tocHtml, content);
  }

  /**
   * Generate HTML files of the book,
   * <p>and init some parameters which could be used when setting toc.xhtml, toc.ncx, content.opf, etc.
   *
   * @author quanqinle
   */
  private void genBodyHtmls() {
    ConvertTxtToHtmls parse = new ConvertTxtToHtmls(book);
    parse.convert();
  }

  /**
   * Modify toc.ncx
   */
  private void modifyTocNcx() throws IOException {
    Path tocNcxPath = tempPath.resolve("OEBPS/toc.ncx");

    String content = Files.readString(tocNcxPath);
    content =
        content
            .replace("[ISBN]", book.getIsbn())
            .replace("[BOOK'S TITLE]", book.getBookTitle())
            .replace("[navPoint LIST]", this.navPointList);

    Files.writeString(tocNcxPath, content);
  }

  /**
   * Modify content.opf
   */
  private void modifyContentOpf() throws IOException {
    Path contentOpfPath = tempPath.resolve("OEBPS/content.opf");

    String content = Files.readString(contentOpfPath);
    content =
        content
            .replace("[UUID]", book.getUuid())
            .replace("[ISBN]", book.getIsbn())
            .replace("[BOOK'S TITLE]", book.getBookTitle())
            .replace("[NAME LASTNAME]", book.getAuthor())
            .replace("[LASTNAME, NAME]", book.getAuthor())
            .replace("[LANGUAGE]", book.getLanguage())
            .replace("[DATE]", book.getCreateDate())
            .replace("[manifest item list]", this.itemList)
            .replace("[spine itemref list]", this.itemrefList)
            .replace("[guide reference list]", this.referenceList);

    Files.writeString(contentOpfPath, content);
  }

  /**
   * Organize contents for toc.ncx and content.opf.
   */
  private void makeContentForTocNcxAndContentOpf() {
    int index = 1;

    // cover
    navPointList =
        navPointList.concat(
            String.format(
                Constant.FORMAT_NAV_POINT, index, index, book.getCoverTitle(), "cover.xhtml"));
    referenceList =
        referenceList.concat(
            String.format(Constant.FORMAT_REFERENCE, "cover", "cover.xhtml", book.getCoverTitle()));
    tocItemList =
        tocItemList.concat(
            String.format(Constant.FORMAT_TOC_ITEM, "cover.xhtml", book.getCoverTitle()));
    index++;

    // TOC
    navPointList =
        navPointList.concat(
            String.format(
                Constant.FORMAT_NAV_POINT, index, index, book.getTocTitle(), "toc.xhtml"));
    referenceList =
        referenceList.concat(
            String.format(Constant.FORMAT_REFERENCE, "toc", "toc.xhtml", book.getTocTitle()));
    tocItemList =
        tocItemList.concat(
            String.format(Constant.FORMAT_TOC_ITEM, "toc.xhtml", book.getTocTitle()));
    index++;

    // front matter
    if (!book.getFrontMatter().isEmpty()) {
      String frontMatterTitle = book.getFrontMatterTitle();
      FileInfo frontMatter = book.getFrontMatter().get(frontMatterTitle);
      String fileName = frontMatter.getFullName();
      navPointList =
              navPointList.concat(
                      String.format(Constant.FORMAT_NAV_POINT, index, index, frontMatterTitle, fileName));
      itemList =
              itemList.concat(String.format(Constant.FORMAT_ITEM, fileName, fileName));
      itemrefList =
              itemrefList.concat(String.format(Constant.FORMAT_ITEMREF, fileName));
      referenceList =
              referenceList.concat(
                      String.format(Constant.FORMAT_REFERENCE, "text", fileName, frontMatterTitle));
      tocItemList =
              tocItemList.concat(String.format(Constant.FORMAT_TOC_ITEM, fileName, frontMatterTitle));
      index++;
    }

    if (book.isHasManyBooks()) {
      String navPointEnd = "</navPoint>";
      // some sub-book in it
      for (LinkedHashMap<String, FileInfo> chapterMap : book.getSubBook().values()) {
        boolean isFirst = true;
        for (String chapterTitle : chapterMap.keySet()) {
          FileInfo fileInfo = chapterMap.get(chapterTitle);

          String fileName = fileInfo.getName();
          String fileFullName = fileInfo.getFullName();

          if (isFirst) {
            String subBookNavPoint = Constant.FORMAT_NAV_POINT.split(navPointEnd)[0];
            navPointList =
                    navPointList.concat(
                            String.format(subBookNavPoint, index, index, chapterTitle, fileFullName));
            isFirst = false;
          } else {
            navPointList =
                    navPointList.concat(
                            String.format(Constant.FORMAT_NAV_POINT, index, index, chapterTitle, fileFullName));
          }

          itemList =
                  itemList.concat(String.format(Constant.FORMAT_ITEM, fileFullName, fileName));
          itemrefList =
                  itemrefList.concat(String.format(Constant.FORMAT_ITEMREF, fileName));
          referenceList =
                  referenceList.concat(
                          String.format(Constant.FORMAT_REFERENCE, "text", fileFullName, chapterTitle));
          tocItemList =
                  tocItemList.concat(String.format(Constant.FORMAT_TOC_ITEM, fileFullName, chapterTitle));

          index++;
        }
        navPointList = navPointList.concat(navPointEnd + "\n");
      }
    } else {
      // chapters in ONE book
      for (String chapterTitle : book.getChapterMap().keySet()) {
        FileInfo fileInfo = book.getChapterMap().get(chapterTitle);

        String fileName = fileInfo.getName();
        String fileFullName = fileInfo.getFullName();
        navPointList =
                navPointList.concat(
                        String.format(Constant.FORMAT_NAV_POINT, index, index, chapterTitle, fileFullName));
        itemList =
                itemList.concat(String.format(Constant.FORMAT_ITEM, fileFullName, fileName));
        itemrefList =
                itemrefList.concat(String.format(Constant.FORMAT_ITEMREF, fileName));
        referenceList =
                referenceList.concat(
                        String.format(Constant.FORMAT_REFERENCE, "text", fileFullName, chapterTitle));
        tocItemList =
                tocItemList.concat(String.format(Constant.FORMAT_TOC_ITEM, fileFullName, chapterTitle));

        index++;
      }
    }

  }

}
