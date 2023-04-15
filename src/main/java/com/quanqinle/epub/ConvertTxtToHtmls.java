package com.quanqinle.epub;

import com.quanqinle.epub.entity.BookInfo;
import com.quanqinle.epub.entity.FileInfo;
import com.quanqinle.epub.util.EpubUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Convert a plain text file `.txt` to some `.xhtml` files.
 *
 * @author quanqinle
 */
public class ConvertTxtToHtmls {
  private static final Logger logger = LoggerFactory.getLogger(ConvertTxtToHtmls.class);

  /** file name used when generate new sub-book file */
  private final String bookFileNameFormat = "book-%d";
  /** file name used when generate new chapter file */
  private final String chapterFileNameFormat = "chapter-%03d";

  /** original plain text file */
  private final Path srcTxtPath;
  /** output book */
  private final BookInfo book;
  /** the folder for storing .xhtml files */
  private final Path drtHtmlFolderPath;

  /**
   * Constructor
   *
   * @param bookInfo book info
   */
  public ConvertTxtToHtmls(BookInfo bookInfo) {
    this.srcTxtPath = bookInfo.getSrcTxtPath();
    this.book = bookInfo;
    this.drtHtmlFolderPath =
        book.getOutputDir().resolve(bookInfo.getTempFolder()).resolve("OEBPS/Text");
  }

  /**
   * Use this method directly after the Constructor() method.
   */
  public void convert() {
    List<String> allLines = readTxt(srcTxtPath);

    if (book.isHasManyBooks()) {
      parseLinesToBooks(allLines, book);
    } else {
      parseLines(allLines, book);
    }

    try {
      Files.createDirectories(drtHtmlFolderPath);
    } catch (IOException e) {
      logger.error("Fail to create HTML folder: {}", drtHtmlFolderPath);
      e.printStackTrace();
    }

    writeFrontMatter(book, drtHtmlFolderPath);

    if (book.isHasManyBooks()) {
      writeBooks(book, drtHtmlFolderPath);
    } else {
      writeChapters(book, drtHtmlFolderPath);
    }
  }

  /**
   * Read txt file to List
   *
   * @param txtPath txt
   * @return line list
   */
  private List<String> readTxt(Path txtPath) {
    List<String> allLines = null;
    try {
      allLines = Files.readAllLines(txtPath, StandardCharsets.UTF_8);
      logger.info("Total {} lines in [{}]", allLines.size(), txtPath);
    } catch (IOException e) {
      logger.error("Fail to read file: {}", txtPath);
      e.printStackTrace();
    }
    return allLines;
  }

  /**
   * When the source book has some sub-books, parse lines into each sub-book
   *
   * @param allLines all lines from txt files
   * @param bookInfo book info
   */
  private void parseLinesToBooks(List<String> allLines, BookInfo bookInfo) {
    logger.info("begin parseLines()...");

    if (allLines == null || allLines.isEmpty()) {
      logger.error("allLines is empty!");
      return;
    }

    String subBookName = "";
    String chapterName = "";
    // html body
    List<String> htmlBodyLines = new ArrayList<>();
    // is previous line in a chapter?
    boolean isPreviousLineInChapter = false;

    boolean isFirstLine = true;
    boolean existFrontMatter = false;

    int idxBook = 0;
    int idxChapter = 0;

    for (String line : allLines) {
      for (String s : bookInfo.getRemoveList()) {
        line = line.replace(s, "").trim();
      }

      // skip empty line
      if (line.isBlank()) {
        continue;
      }

      boolean isSubBookTitle = isSubBookTitle(line, bookInfo.getBookTitleRegexList());
      boolean isChapterTitle = isChapterTitle(line, bookInfo.getChapterTitleRegexList());
      if (isFirstLine) {
        isFirstLine = false;
        if (!isSubBookTitle && !isChapterTitle) {
          htmlBodyLines.add("<p>" + line + "</p>");
          existFrontMatter = true;
          continue;
        }
      }

      if (!isSubBookTitle && !isChapterTitle) {
        htmlBodyLines.add("<p>" + line + "</p>");
      } else {
        if (existFrontMatter && bookInfo.getFrontMatter().isEmpty()) {
          // save the previous lines into `front matter`
          List<String> copy = new ArrayList<>(htmlBodyLines);
          FileInfo fileInfo =
              new FileInfo(bookInfo.getFrontMatterFile(), bookInfo.getFrontMatterTitle(), copy);
          bookInfo.getFrontMatter().put(bookInfo.getFrontMatterTitle(), fileInfo);
          logger.info(
              "Front-matter [{}] has [{}] lines", bookInfo.getFrontMatterTitle(), htmlBodyLines.size());
        } else {
          if (isPreviousLineInChapter) {
            // save the previous chapter body
            String fileName =
                String.format(
                    bookFileNameFormat + "_" + chapterFileNameFormat, idxBook, idxChapter);
            FileInfo fileInfo = new FileInfo(fileName, chapterName, new ArrayList<>(htmlBodyLines));

            bookInfo.getSubBook().get(subBookName).put(chapterName, fileInfo);
            logger.info("Chapter [{}] has [{}] lines", chapterName, htmlBodyLines.size());
          } else {
            // save the previous sub-book body
            String fileName = String.format(bookFileNameFormat, idxBook);
            FileInfo fileInfo = new FileInfo(fileName, subBookName, new ArrayList<>(htmlBodyLines));

            LinkedHashMap<String, FileInfo> chapterMap = new LinkedHashMap<>();
            chapterMap.put(subBookName, fileInfo);
            bookInfo.getSubBook().put(subBookName, chapterMap);
            logger.info("Book [{}] has [{}] lines", subBookName, htmlBodyLines.size());
          }
        }

        htmlBodyLines.clear();

        if (isSubBookTitle) {
          htmlBodyLines.add("<h1>" + line + "</h1>");
          subBookName = line;
          idxBook++;
          idxChapter = 0;
          isPreviousLineInChapter = false;
        } else {
          htmlBodyLines.add("<h2>" + line + "</h2>");
          chapterName = line;
          idxChapter++;
          isPreviousLineInChapter = true;
        }
      }
    } // end for-loop allLines
  }

  /**
   * When the source book has only ONE book, parse lines into each chapter
   *
   * @param allLines all lines from txt files
   * @param bookInfo book info
   */
  private void parseLines(List<String> allLines, BookInfo bookInfo) {
    logger.info("begin parseLines()...");

    if (allLines == null || allLines.isEmpty()) {
      logger.error("allLines is empty!");
      return;
    }

    String chapterName = "";
    // chapter body
    List<String> chapterLines = new ArrayList<>();

    int i = 1;
    for (String line : allLines) {
      for (String s : bookInfo.getRemoveList()) {
        line = line.replace(s, "").trim();
      }

      // skip empty line
      if (line.isBlank()) {
        continue;
      }

      // If chapter title, save chapterLines into the previous chapter.
      // If Not chapter title, save line into chapterLines.
      if (!isChapterTitle(line, bookInfo.getChapterTitleRegexList())) {
        chapterLines.add("<p>" + line + "</p>");
      } else {
        // chapterName is blank means the current line is the 1st chapter title
        if (chapterName.isBlank()) {
          if (chapterLines.isEmpty()) {
            // the 1st chapter title is just the 1st line of the book.
            logger.info("It is the 1st chapter");
          } else {
            // save the previous lines into `front matter`
            List<String> copy = new ArrayList<>(chapterLines);
            FileInfo fileInfo =
                new FileInfo(bookInfo.getFrontMatterFile(), bookInfo.getFrontMatterTitle(), copy);
            bookInfo.getFrontMatter().put(bookInfo.getFrontMatterTitle(), fileInfo);
          }
        } else {
          // save the previous chapter body
          List<String> copy = new ArrayList<>(chapterLines);
          String fileName = String.format(chapterFileNameFormat, i);
          i++;
          FileInfo fileInfo = new FileInfo(fileName, chapterName, copy);

          bookInfo.getChapterMap().put(chapterName, fileInfo);
        }

        logger.info("Chapter [{}] has [{}] lines", chapterName, chapterLines.size());

        chapterLines.clear();
        chapterName = line;
        chapterLines.add("<h1>" + chapterName + "</h1>");
      } // end processing chapter title
    } // end for-loop allLines

    // save the last chapter
    if (!chapterName.isBlank()) {

      bookInfo
          .getChapterMap()
          .put(
              chapterName,
              new FileInfo(
                  String.format(chapterFileNameFormat, i),
                  chapterName,
                  new ArrayList<>(chapterLines)));

      logger.info("Chapter [{}] has [{}] lines", chapterName, chapterLines.size());
    }

    logger.info("end parseLines()...");
  }

  /**
   * Check if this line is chapter title
   *
   * @param line -
   * @param chapterTitleRegexList -
   * @return -
   */
  private boolean isChapterTitle(String line, List<String> chapterTitleRegexList) {
    for (String regex : chapterTitleRegexList) {
      if (line.matches(regex)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Check if this line is sub-book title
   *
   * @param line -
   * @param chapterTitleRegexList -
   * @return -
   */
  private boolean isSubBookTitle(String line, List<String> chapterTitleRegexList) {
    for (String regex : chapterTitleRegexList) {
      if (line.matches(regex)) {
        return true;
      }
    }

    return false;
  }

  /**
   * Save front matter to HTML file
   *
   * @param bookInfo book
   * @param htmlFolderPath HTML file folder
   */
  private void writeFrontMatter(BookInfo bookInfo, Path htmlFolderPath) {
    if (bookInfo.getFrontMatter().size() != 1) {
      logger.error("unexpected size of front matter");
      return;
    }

    FileInfo frontMatter = bookInfo.getFrontMatter().get(bookInfo.getFrontMatterTitle());
    Path htmlPath = htmlFolderPath.resolve(frontMatter.getFullName());
    frontMatter.setFullPath(htmlPath);

    EpubUtils.writeHtmlFile(bookInfo.getFrontMatterTitle(), frontMatter.getLines(), htmlPath);
  }

  /**
   * Save all chapters to HTML files
   *
   * @param bookInfo book
   * @param htmlFolderPath HTML file folder
   */
  private void writeChapters(BookInfo bookInfo, Path htmlFolderPath) {
    LinkedHashMap<String, FileInfo> chapterMap = bookInfo.getChapterMap();

    for (String chapterTitle : chapterMap.keySet()) {
      FileInfo fileInfo = chapterMap.get(chapterTitle);
      Path htmlPath = htmlFolderPath.resolve(fileInfo.getFullName());
      fileInfo.setFullPath(htmlPath);

      EpubUtils.writeHtmlFile(chapterTitle, fileInfo.getLines(), htmlPath);
    }
  }

  /**
   * Save all chapters (within sub-books) to HTML files
   *
   * @param bookInfo book
   * @param htmlFolderPath HTML file folder
   */
  private void writeBooks(BookInfo bookInfo, Path htmlFolderPath) {
    LinkedHashMap<String, LinkedHashMap<String, FileInfo>> bookMap = bookInfo.getSubBook();

    bookMap.values().forEach(chapterMap -> {
      chapterMap.forEach((chapterTitle, fileInfo) -> {
        Path htmlPath = htmlFolderPath.resolve(fileInfo.getFullName());
        fileInfo.setFullPath(htmlPath);

        EpubUtils.writeHtmlFile(fileInfo.getDescribe(), fileInfo.getLines(), htmlPath);
      });
    });

  }
}
