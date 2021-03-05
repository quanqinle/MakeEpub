package com.quanqinle.epub.entity;

import java.nio.file.Path;

public class BookInfo {
    /**
     * 导出路径
     */
    Path outputDir;

    String UUID;
    String ISBN;
    String bookTitle;
    String author;
    String createDate;
    /**
     * 语言。en、zh
     */
    String language;
    String jpgCoverSrc;

    /**
     * Foreword-Usually a short piece written by someone other than the author, the Foreword may provide a context for the main work. Remember that the Foreword is always signed, usually with its author’s name, place, and date.
     */
    Boolean hasForeword = false;

    /**
     * Preface—Written by the author, the Preface often tells how the book came into being, and is often signed with the name, place and date, although this is not always the case.
     */
    Boolean hasPreface = false;

    String frontMatter;
}
