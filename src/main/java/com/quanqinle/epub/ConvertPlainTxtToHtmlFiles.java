package com.quanqinle.epub;

import com.quanqinle.epub.entity.BookInfo;
import com.quanqinle.epub.entity.FileInfo;
import com.quanqinle.epub.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Convert a plain text file `.txt` to some `.xhtml` files.
 *
 * @author quanqinle
 */
public class ConvertPlainTxtToHtmlFiles {

    public static final Logger logger = LoggerFactory.getLogger(ConvertPlainTxtToHtmlFiles.class);

    private final String SUFFIX = ".xhtml";
    private final String regexChapterTitle = "^第.{1,10}章[^完]";
    private String chapterFileNameFormat = "chapter-%03d.xhtml";
    /**
     * some chart or String have to be trimmed in the whole book.
     * NOTE!
     * If you want to remove some thing in the book, change them into the parameter.
     */
    private List<String> trimList = Arrays.asList("　");

    private BookInfo book;
    /**
     * original plain text file
     */
    private Path srcFilePath;
    /**
     * the folder for storing .xhtml files
     */
    private Path drtHtmlFolderPath;

    /**
     * key - chapter title
     * val - html file info
     */
    private LinkedHashMap<String, FileInfo> htmlFileMap;

    /**
     * Books are divided into three basic parts:
     * 1. front matter
     * 2. body of the book
     * 3. back matter
     *
     * use LinkedHashMap to guarantee insertion order
     */
    private LinkedHashMap<String, List<String>> frontMatterMap = new LinkedHashMap<>();
    /**
     * key - chapter title
     * value - content line
     */
    private LinkedHashMap<String, List<String>> chapterMap = new LinkedHashMap<>();
//    private LinkedHashMap<String, List<String>> backMatterMap = new LinkedHashMap<>();

    public static void main(String[] args) {

        BookInfo book = new BookInfo();
        book.setOutputDir(Paths.get("D:", "epub"));
        book.setCoverJpgFullPath(Paths.get("D:", "book.jpg"));
        book.setUuid(UUID.randomUUID().toString());
        book.setLanguage("zh");
        book.setBookTitle("红楼梦");
        book.setAuthor("曹雪芹");
        book.setCreateDate("2021-03-06");

        Path srcFilePath = Paths.get("D:", "book-library", "demo.txt");

        ConvertPlainTxtToHtmlFiles parse = new ConvertPlainTxtToHtmlFiles(srcFilePath, book);
        parse.doConvert();
    }

    public ConvertPlainTxtToHtmlFiles(Path srcFilePath, BookInfo book) {
        this.srcFilePath = srcFilePath;
        this.book = book;

        this.drtHtmlFolderPath = book.getOutputDir().resolve(Constant.TEMPLATE_NAME).resolve("OEBPS/Text");

        this.htmlFileMap = book.getHtmlFileMap();
    }

    /**
     * start to convert the file.
     * The method is an all in one method, it includes read-parse-rewrite,
     * so use it just after the construction method.
     */
    public void doConvert() {

        List<String> allLines = null;
        try {
            allLines = Files.readAllLines(srcFilePath);
            logger.info("Total {} lines in [{}]", allLines.size(), srcFilePath);
        } catch (IOException e) {
            logger.error("Fail to read file: {}", srcFilePath);
            e.printStackTrace();
        }

        parseLinesToMap(allLines);

        writeFrontMatter(drtHtmlFolderPath);
        writeChapter(drtHtmlFolderPath);

        book.setHtmlFileMap(this.htmlFileMap);
    }

    /**
     * convert all lines of the file to chapter map and front matter map
     *
     * @author quanqinle
     * @param allLines all lines of file
     */
    private void parseLinesToMap(List<String> allLines) {
        logger.info("begin parseLinesToMap...");
        if (allLines == null || allLines.isEmpty()) {
            logger.error("allLines is empty!");
            return;
        }

        String chapterName = "";
        List<String> chapterLines = new ArrayList<>();

        for (String line : allLines) {
            for (String s : trimList) {
                line = line.replace(s, "").trim();
            }

            if (line.isBlank()) {
                continue;
            }

            if (isChapterTitle(line)) {
                /**
                 * do not add chapter title into book body lines
                 */

                if (chapterName.isBlank()) {
                    /**
                     * when it comes to 1st chapter title, save the previous lines into `front matter`
                     */
                    if (!chapterLines.isEmpty()) {
                        List<String> copy = new ArrayList<String>(chapterLines);
                        frontMatterMap.put(Constant.FRONT_MATTER_TITLE, copy);

//                        logger.debug("complete parsing {}, size {}, first line: {}", chapterName, ""+chapterLines.size(), chapterLines.get(0));

                        chapterLines.clear();
                    }
                } else {
                    /*
                     * save the previous chapter
                     */

                    List<String> copy = new ArrayList<String>(chapterLines);
                    chapterMap.put(chapterName, copy);
//                    logger.debug("complete parsing {}, size {}, first line: {}", chapterName, ""+chapterLines.size(), chapterLines.get(0));

                    chapterLines.clear();
                }

                chapterName = line;
            } else {
                chapterLines.add("<p>" + line + "</p>");
            }

        } // end for allLines

        /*
         * save the last chapter
         */
        if (!chapterName.isBlank()) {
//            logger.debug("complete parsing {}, size {}, first line: {}", chapterName, chapterLines.size(), chapterLines.get(0));
            List<String> copy = new ArrayList<>(chapterLines);
            chapterMap.put(chapterName, copy);
        }

        logger.info("end parseLinesToMap...");
    }

    /**
     * save front matter to HTML file, index is 0
     *
     * @param htmlFolderPath HTML file folder
     */
    private void writeFrontMatter (Path htmlFolderPath) {
        if (frontMatterMap.size() != 1) {
            logger.error("unexpected size of front matter");
            return;
        }

        String fileName = String.format(chapterFileNameFormat, 0);
        Path htmlPath = htmlFolderPath.resolve(fileName);

        writeHtmlFile(Constant.FRONT_MATTER_TITLE, frontMatterMap.get(Constant.FRONT_MATTER_TITLE), htmlPath);

        FileInfo htmlFile = new FileInfo(0, fileName, Constant.FRONT_MATTER_TITLE);
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
    private void writeChapter (Path htmlFolderPath) {
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

        String topPart = (""
                + "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n"
                + "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\"\r\n"
                + "  \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">\r\n"
                + "<html xmlns=\"http://www.w3.org/1999/xhtml\">\r\n" + "<head>\r\n" + "<title>CHAPTER_TITLE</title>\r\n"
                + "<link href=\"../Styles/main.css\" type=\"text/css\" rel=\"stylesheet\"/>\r\n" + "</head>\r\n"
                + "<body>\r\n" + "<h1>CHAPTER_TITLE</h1>").replace("CHAPTER_TITLE", chapterName);
        String bottomPart = "</body></html>";

        chpLines.add(topPart);
        chpLines.addAll(bodyLines);
        chpLines.add(bottomPart);

        try {
            Files.write(htmlPath, chpLines);
//            logger.debug("complete saving {}, first line: {}", chapterName, bodyLines.get(0));
        } catch (Exception e) {
            logger.error("fail to save: {}", chapterName);
            e.printStackTrace();
        }
    }

    /**
     * check if this line is chapter title
     *
     * @param line -
     * @return
     */
    private boolean isChapterTitle(String line) {
        Pattern p = Pattern.compile(regexChapterTitle);
        Matcher m = p.matcher(line);
        return m.find();
    }

}
