package com.quanqinle.epub.entity;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Book info
 *
 * @author quanqinle
 */
public class BookInfo {
  /** the directory for output */
  Path outputDir;
  /** UUID.randomUUID().toString() */
  String uuid = "";
  /** ISBN is a numeric commercial book identifier which is intended to be unique. */
  String isbn = "";
  /** the title of the book, which will be used as the name of .epub */
  String bookTitle = "";
  /** author */
  String author = "";
  /** such as: 2021-03-06 */
  String createDate = "";
  /** language, such as: en,zh */
  String language = "";
  /**
   * the chapter title of cover, used in places like these: &lt;title>&lt;h1>, etc.
   */
  String coverTitle = "封面";
  /** full path of the cover picture, ONLY .jpg allowed current */
  Path coverJpgFullPath;

  /**
   * the chapter title of TOC, used in places like these: &lt;title>&lt;h1>, etc.
   */
  String tocTitle = "目录";

  /** the folder name in resource of this project, saving epub template */
  String templateName = "template";

  /** the name of front matter which is just before the 1st chapter */
  String frontMatterTitle = "引言";
  /** the file name of front matter without suffix */
  String frontMatterFile = "front_matter";

  /**
   * front matter in the book
   * <p> title -> file
   */
  LinkedHashMap<String, FileInfo> frontMatter = new LinkedHashMap<>();

  /** Note: modify this regex if the sub-book title is not match in your book. */
  List<String> bookTitleRegexList =
          List.of("^第.{1,10}卷.{1,20}[^完]", "^第.{1,10}册.{1,20}[^完]");

  /** Note: modify this regex if the chapter title is not match in your book. */
  List<String> chapterTitleRegexList =
          List.of("^第.{1,10}章.{1,20}[^完]", "^第.{1,10}节.{1,20}[^完]", "^初章.{1,20}[^完]");
  /**
   * some chart or String have to be trimmed in the whole book. NOTE! If you want to remove
   * something in the book, change them into the parameter.
   */
  List<String> removeList = List.of("　");
  /**
   * More than ONE sub-book/volume in the book.
   * <p>`false` by default.
   */
  boolean hasManyBooks = false;
  /**
   * If only one book/volume in the book, use this variable.
   * <p> chapter title -> file
   * <p> Use {@link #chapterMap} or {@link #subBook}.
   */
  LinkedHashMap<String, FileInfo> chapterMap = new LinkedHashMap<>();
  /**
   * If two books/volumes or more in the book, use this variable.
   * <p> sub-book title -> chapter map [chapter title -> file]
   * <p> Use {@link #chapterMap} or {@link #subBook}.
   */
  LinkedHashMap<String, LinkedHashMap<String, FileInfo>> subBook = new LinkedHashMap<>();

  public BookInfo() {
  }

  public Path getOutputDir() {
    return outputDir;
  }

  public void setOutputDir(Path outputDir) {
    this.outputDir = outputDir;
  }

  public String getUUID() {
    return uuid;
  }

  public void setUUID(String uuid) {
    this.uuid = uuid;
  }

  public String getIsbn() {
    return isbn;
  }

  public void setIsbn(String isbn) {
    this.isbn = isbn;
  }

  public String getBookTitle() {
    return bookTitle;
  }

  public void setBookTitle(String bookTitle) {
    this.bookTitle = bookTitle;
  }

  public String getAuthor() {
    return author;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getCreateDate() {
    return createDate;
  }

  public void setCreateDate(String createDate) {
    this.createDate = createDate;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public Path getCoverJpgFullPath() {
    return coverJpgFullPath;
  }

  public void setCoverJpgFullPath(Path coverJpgFullPath) {
    this.coverJpgFullPath = coverJpgFullPath;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public Map<String, FileInfo> getFrontMatter() {
    return frontMatter;
  }

  public void setFrontMatter(LinkedHashMap<String, FileInfo> frontMatter) {
    this.frontMatter = frontMatter;
  }

  public boolean isHasManyBooks() {
    return hasManyBooks;
  }

  public void setHasManyBooks(boolean hasManyBooks) {
    this.hasManyBooks = hasManyBooks;
  }

  public LinkedHashMap<String, FileInfo> getChapterMap() {
    return chapterMap;
  }

  public void setChapterMap(LinkedHashMap<String, FileInfo> chapterMap) {
    this.chapterMap = chapterMap;
  }

  public LinkedHashMap<String, LinkedHashMap<String, FileInfo>> getSubBook() {
    return subBook;
  }

  public void setSubBook(LinkedHashMap<String, LinkedHashMap<String, FileInfo>> subBook) {
    this.subBook = subBook;
  }

  public String getCoverTitle() {
    return coverTitle;
  }

  public void setCoverTitle(String coverTitle) {
    this.coverTitle = coverTitle;
  }

  public String getTocTitle() {
    return tocTitle;
  }

  public void setTocTitle(String tocTitle) {
    this.tocTitle = tocTitle;
  }

  public String getTemplateName() {
    return templateName;
  }

  public void setTemplateName(String templateName) {
    this.templateName = templateName;
  }

  public String getFrontMatterTitle() {
    return frontMatterTitle;
  }

  public void setFrontMatterTitle(String frontMatterTitle) {
    this.frontMatterTitle = frontMatterTitle;
  }

  public String getFrontMatterFile() {
    return frontMatterFile;
  }

  public void setFrontMatterFile(String frontMatterFile) {
    this.frontMatterFile = frontMatterFile;
  }

  public List<String> getBookTitleRegexList() {
    return bookTitleRegexList;
  }

  public void setBookTitleRegexList(List<String> bookTitleRegexList) {
    this.bookTitleRegexList = bookTitleRegexList;
  }

  public List<String> getChapterTitleRegexList() {
    return chapterTitleRegexList;
  }

  public void setChapterTitleRegexList(List<String> chapterTitleRegexList) {
    this.chapterTitleRegexList = chapterTitleRegexList;
  }

  public List<String> getRemoveList() {
    return removeList;
  }

  public void setRemoveList(List<String> removeList) {
    this.removeList = removeList;
  }
}
