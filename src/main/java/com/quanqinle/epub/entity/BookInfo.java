package com.quanqinle.epub.entity;

import java.nio.file.Path;
import java.util.LinkedHashMap;

/**
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
    String UUID = "";
    String ISBN = "";
    String bookTitle = "";
    String author = "";
    /**
     * such as: 2021-03-06
     */
    String createDate = "";
    /**
     * language, such as: en,zh
     */
    String language = "zh";
    Path coverJpgFullPath;

    /**
     * key - chapter title
     * value - file info
     */
    LinkedHashMap<String, FileInfo> htmlFileMap = new LinkedHashMap<>();

    /**
     * Foreword-Usually a short piece written by someone other than the author, the Foreword may provide a context for the main work. Remember that the Foreword is always signed, usually with its author’s name, place, and date.
     */
    Boolean hasForeword = false;

    /**
     * Preface—Written by the author, the Preface often tells how the book came into being, and is often signed with the name, place and date, although this is not always the case.
     */
    Boolean hasPreface = false;


    public Path getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(Path outputDir) {
        this.outputDir = outputDir;
    }

    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBN) {
        this.ISBN = ISBN;
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

    public Boolean getHasForeword() {
        return hasForeword;
    }

    public void setHasForeword(Boolean hasForeword) {
        this.hasForeword = hasForeword;
    }

    public Boolean getHasPreface() {
        return hasPreface;
    }

    public void setHasPreface(Boolean hasPreface) {
        this.hasPreface = hasPreface;
    }

    public LinkedHashMap<String, FileInfo> getHtmlFileMap() {
        return htmlFileMap;
    }

    public void setHtmlFileMap(LinkedHashMap<String, FileInfo> htmlFileMap) {
        this.htmlFileMap = htmlFileMap;
    }
}
