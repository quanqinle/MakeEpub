package com.quanqinle.epub.entity;

import java.nio.file.Path;
import java.util.LinkedHashMap;

/**
 * Book info
 *
 * @author quanqinle
 */
public class BookInfo {
    /**
     * the directory for output
     */
    Path outputDir;
    /**
     * UUID.randomUUID().toString()
     */
    String uuid = "";
    /**
     * ISBN is a numeric commercial book identifier which is intended to be unique.
     */
    String isbn = "";
    /**
     * the title of the book, which will be used as the name of .epub
     */
    String bookTitle = "";
    /**
     * author
     */
    String author = "";
    /**
     * such as: 2021-03-06
     */
    String createDate = "";
    /**
     * language, such as: en,zh
     */
    String language = "";
    /**
     * full path of the cover picture, ONLY .jpg allowed now
     */
    Path coverJpgFullPath;

    /**
     * key - chapter title,
     * value - file info
     */
    LinkedHashMap<String, FileInfo> htmlFileMap = new LinkedHashMap<>();

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

    public LinkedHashMap<String, FileInfo> getHtmlFileMap() {
        return htmlFileMap;
    }

    public void setHtmlFileMap(LinkedHashMap<String, FileInfo> htmlFileMap) {
        this.htmlFileMap = htmlFileMap;
    }
}
