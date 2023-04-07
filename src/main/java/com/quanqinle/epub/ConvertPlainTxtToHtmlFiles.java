package com.quanqinle.epub;

import com.quanqinle.epub.entity.BookInfo;
import com.quanqinle.epub.entity.FileInfo;
import com.quanqinle.epub.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convert a plain text file `.txt` to some `.xhtml` files.
 *
 * @author quanqinle
 */
public class ConvertPlainTxtToHtmlFiles {

  private static final Logger logger = LoggerFactory.getLogger(ConvertPlainTxtToHtmlFiles.class);
  /** Note: modify this regex if the chapter title is not matched in your book. */
  private final List<String> chapterTitleRegexList = List.copyOf(Constant.CHAPTER_TITLE_REGEX_LIST);
  /** Do not recommend to modify it. */
  private final String SUFFIX = ".xhtml";
  /** file name used when generate new chapter file */
  private final String chapterFileNameFormat = "chapter-%03d" + SUFFIX;

  /**
   * some chart or String have to be trimmed in the whole book. NOTE! If you want to remove
   * something in the book, change them into the parameter.
   */
  private final List<String> trimList = List.of("　");

  /** output book */
  private final BookInfo book;
  /** original plain text file */
  private final Path srcFilePath;
  /** the folder for storing .xhtml files */
  private final Path drtHtmlFolderPath;

  /** key - chapter title, val - html file info */
  private final LinkedHashMap<String, FileInfo> htmlFileMap;

  /**
   * Books are divided into three basic parts: 1. front matter 2. body of the book 3. back matter
   *
   * <p>use LinkedHashMap to guarantee insertion order
   */
  private final LinkedHashMap<String, List<String>> frontMatterMap = new LinkedHashMap<>();
  /** key - chapter title, value - content line */
  private final LinkedHashMap<String, List<String>> chapterMap = new LinkedHashMap<>();
  //    private LinkedHashMap<String, List<String>> backMatterMap = new LinkedHashMap<>();

  /**
   * Constructor
   *
   * @param srcFilePath original plain text file
   * @param book output book
   */
  public ConvertPlainTxtToHtmlFiles(Path srcFilePath, BookInfo book) {
    this.srcFilePath = srcFilePath;
    this.book = book;

    this.drtHtmlFolderPath =
        book.getOutputDir().resolve(Constant.TEMPLATE_NAME).resolve("OEBPS/Text");

    this.htmlFileMap = book.getHtmlFileMap();
  }

  /**
   * Start to convert the file.
   *
   * <p>The method is an all-in-one method, it includes the whole steps of read-parse-rewrite, so
   * use it just after the construction method.
   */
  public void convert() throws IOException {

    List<String> allLines;
    try {
      allLines = Files.readAllLines(srcFilePath, StandardCharsets.UTF_8);
      logger.info("Total {} lines in [{}]", allLines.size(), srcFilePath);
    } catch (IOException e) {
      logger.error("Fail to read file: {}", srcFilePath);
      e.printStackTrace();
      throw e;
    }

    parseLinesToMap(allLines);

    if (!Files.exists(drtHtmlFolderPath)) {
      try {
        Files.createDirectories(drtHtmlFolderPath);
      } catch (IOException e) {
        logger.error("Fail to create HTML folder: {}", drtHtmlFolderPath);
        e.printStackTrace();
      }
    }

    writeFrontMatter(drtHtmlFolderPath);
    writeChapter(drtHtmlFolderPath);

    book.setHtmlFileMap(this.htmlFileMap);
  }

  /**
   * Convert all lines of the file to chapter map and front matter map
   *
   * @author quanqinle
   * @param allLines all lines of file
   */
  private void parseLinesToMap(List<String> allLines) {
    logger.info("begin parseLinesToMap()...");

    if (allLines == null || allLines.isEmpty()) {
      logger.error("allLines is empty!");
      return;
    }

    String chapterName = "";
    // chapter body
    List<String> chapterLines = new ArrayList<>();

    for (String line : allLines) {
      for (String s : trimList) {
        line = line.replace(s, "").trim();
      }

      // skip empty line
      if (line.isBlank()) {
        continue;
      }

      // If chapter title, save chapterLines into the previous chapter.
      // If Not chapter title, save line into chapterLines.
      if (!isChapterTitle(line)) {
        chapterLines.add("<p>" + line + "</p>");
      } else {
        // chapterName is blank means the current line is the 1st chapter title
        if (chapterName.isBlank()) {
          if (chapterLines.isEmpty()) {
            // the 1st chapter title is just the 1st line of the book.
          } else {
            // save the previous lines into `front matter`
            List<String> copy = new ArrayList<>(chapterLines);
            frontMatterMap.put(Constant.FRONT_MATTER_TITLE, copy);
          }
        } else {
          // save the previous chapter body
          List<String> copy = new ArrayList<>(chapterLines);
          chapterMap.put(chapterName, copy);
        }

        logger.info("Chapter [{}] has [{}] lines", chapterName, chapterLines.size());

        chapterLines.clear();
        chapterName = line;
        chapterLines.add("<h1>" + chapterName + "</h1>");
      } // end processing chapter title
    } // end for-loop allLines

    // save the last chapter
    if (!chapterName.isBlank()) {
      List<String> copy = new ArrayList<>(chapterLines);
      chapterMap.put(chapterName, copy);
      logger.info("Chapter [{}] has [{}] lines", chapterName, chapterLines.size());
    }

    logger.info("end parseLinesToMap()...");
  }

  /**
   * save front matter to HTML file, index is 0
   *
   * @param htmlFolderPath HTML file folder
   */
  private void writeFrontMatter(Path htmlFolderPath) {
    if (frontMatterMap.size() != 1) {
      logger.error("unexpected size of front matter");
      return;
    }

    int frontMatterHtmlIndex = 0;
    String fileName = String.format(chapterFileNameFormat, frontMatterHtmlIndex);
    Path htmlPath = htmlFolderPath.resolve(fileName);

    writeHtmlFile(
        Constant.FRONT_MATTER_TITLE, frontMatterMap.get(Constant.FRONT_MATTER_TITLE), htmlPath);

    FileInfo htmlFile = new FileInfo(frontMatterHtmlIndex, fileName, Constant.FRONT_MATTER_TITLE);
    htmlFile.setSuffix(SUFFIX);
    htmlFile.setShortName(fileName.replace(SUFFIX, ""));
    htmlFile.setFullPath(htmlPath);
    htmlFileMap.put(Constant.FRONT_MATTER_TITLE, htmlFile);
  }

  /**
   * save all chapters to HTML files, index comes from 1
   *
   * @param htmlFolderPath HTML file folder
   */
  private void writeChapter(Path htmlFolderPath) {
    int i = 1;
    for (String chapterTitle : chapterMap.keySet()) {
      String fileName = String.format(chapterFileNameFormat, i);
      Path htmlPath = htmlFolderPath.resolve(fileName);

      writeHtmlFile(chapterTitle, chapterMap.get(chapterTitle), htmlPath);

      FileInfo htmlFile = new FileInfo(i, fileName, chapterTitle);
      htmlFile.setSuffix(SUFFIX);
      htmlFile.setShortName(fileName.replace(SUFFIX, ""));
      htmlFile.setFullPath(htmlPath);
      htmlFileMap.put(chapterTitle, htmlFile);

      i++;
    }
  }

  /**
   * Write all lines of a chapter in a html file
   *
   * @author quanqinle
   * @param chapterName chapter title, such as "Chapter ONE A Dance with Dragons"
   * @param bodyLines all lines of this chapter
   * @param htmlPath full name of the output file
   */
  private void writeHtmlFile(String chapterName, List<String> bodyLines, Path htmlPath) {
    List<String> chpLines = new ArrayList<>();

    String topPart =
        ""
            + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n"
            + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"\r\n"
            + "  \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\r\n"
            + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\r\n"
            + "<head>\r\n"
            + "  <title>"
            + chapterName
            + "</title>\r\n"
            + "  <link href=\"../Styles/main.css\" type=\"text/css\" rel=\"stylesheet\"/>\r\n"
            + "</head>\r\n"
            + "<body>\r\n";
    String bottomPart = "</body>\r\n</html>";

    chpLines.add(topPart);
    chpLines.addAll(bodyLines);
    chpLines.add(bottomPart);

    try {
      Files.write(htmlPath, chpLines);
      logger.debug("complete saving {}, first line: {}", chapterName, bodyLines.get(0));
    } catch (Exception e) {
      logger.error("Fail to save: {}", chapterName);
      e.printStackTrace();
    }
  }

  /**
   * Check if this line is chapter title
   *
   * @param line -
   * @return -
   */
  private boolean isChapterTitle(String line) {
    for (String reg : chapterTitleRegexList) {
      Pattern p = Pattern.compile(reg);
      Matcher m = p.matcher(line);
      if (m.find()) {
        return true;
      }
    }

    return false;
  }

}
