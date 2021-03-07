package com.quanqinle.epub;

import com.quanqinle.epub.entity.BookInfo;
import com.quanqinle.epub.entity.FileInfo;
import com.quanqinle.epub.util.Constant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author quanqinle
 */
public class ConvertPlainTxtToHtmlFiles {

    public static final Logger logger = LoggerFactory.getLogger(ConvertPlainTxtToHtmlFiles.class);
    static final String SUFFIX = ".xhtml";

    static String regexChapterTitle = "^第.{1,10}章[^完]";
    static String chapterFileNameFormat = "chapter-%03d.xhtml";


    BookInfo book;
    /**
     * original plain text file
     */
    Path srcFilePath;
    Path drtHtmlFolderPath;

    LinkedHashMap<String, FileInfo> htmlFileMap;

    /**
     * Books are divided into three basic parts:
     * 1. front matter
     * 2. body of the book
     * 3. back matter
     *
     * use LinkedHashMap to guarantee insertion order
     */
    static LinkedHashMap<String, List<String>> frontMatterMap = new LinkedHashMap<>();
    /**
     * key - chapter title
     * value - content line
     */
    static LinkedHashMap<String, List<String>> chapterMap = new LinkedHashMap<>();
//    static LinkedHashMap<String, List<String>> backMatterMap = new LinkedHashMap<>();


    /**
     * some chart or String have to be trimmed
     */
    static List<String> trimList = Arrays.asList("　");

    public static void main(String[] args) {
        logger.info("start main...");
        BookInfo book = new BookInfo();
        book.setOutputDir(Paths.get("D:", "epub"));
        book.setCoverJpgFullPath(Paths.get("D:", "book.jpg"));
        book.setUUID(UUID.randomUUID().toString());
        book.setLanguage("zh");
        book.setBookTitle("红楼梦");
        book.setAuthor("曹雪芹");
        book.setCreateDate("2021-03-06");

        Path srcFilePath = Paths.get("D:", "book-library", "demo.txt");

        ConvertPlainTxtToHtmlFiles parse = new ConvertPlainTxtToHtmlFiles(srcFilePath, book);
        parse.doConvert();

        logger.info("end main...");
    }

    public ConvertPlainTxtToHtmlFiles(Path srcFilePath, BookInfo book) {
        this.srcFilePath = srcFilePath;
        this.book = book;

        drtHtmlFolderPath = book.getOutputDir().resolve(Constant.templateName).resolve("OEBPS/Text");

        htmlFileMap = book.getHtmlFileMap();
    }

    /**
     * 执行转换
     */
    public void doConvert() {

        List<String> allLines = null;
        try {
            allLines = Files.readAllLines(srcFilePath);
            logger.info("all lines size: {}", allLines.size());
        } catch (IOException e) {
            logger.error("fail to read text file");
            e.printStackTrace();
        }

        parseLinesToMap(allLines);
        logger.info("FrontMatter map size {}", frontMatterMap.size());
        logger.info("Chapter map size {}", chapterMap.size());

        writeFrontMatter(drtHtmlFolderPath);
        writeChapter(drtHtmlFolderPath);

        book.setHtmlFileMap(this.htmlFileMap);

    }

    /**
     * convert file lines to chapter map and front matter map
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
                        frontMatterMap.put(Constant.frontMatterTitle, copy);

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
     * save front matter to HTML, index is 0
     *
     * @param htmlFolderPath
     */
    private void writeFrontMatter (Path htmlFolderPath) {
        if (frontMatterMap.size() != 1) {
            logger.error("unexpected size of front matter");
            return;
        }

        String fileName = String.format(chapterFileNameFormat, 0);
        Path htmlPath = htmlFolderPath.resolve(fileName);

        writeHtmlFile(Constant.frontMatterTitle, frontMatterMap.get(Constant.frontMatterTitle), htmlPath);

        FileInfo htmlFile = new FileInfo(0, fileName, Constant.frontMatterTitle);
        htmlFile.setSuffix(SUFFIX);
        htmlFile.setShortName(fileName.replace(SUFFIX, ""));
        htmlFile.setFullPath(htmlPath);
        htmlFileMap.put(Constant.frontMatterTitle, htmlFile);
    }

    /**
     * save chapters to HTMLs, index comes from 1
     *
     * @param htmlFolderPath
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
     * 保存一个章节到 html 文件
     *
     * @author 权芹乐
     * @param chapterName
     * @param bodyLines
     * @param htmlPath
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
    private static boolean isChapterTitle(String line) {
        Pattern p = Pattern.compile(regexChapterTitle);
        Matcher m = p.matcher(line);
        return m.find();
    }

}
