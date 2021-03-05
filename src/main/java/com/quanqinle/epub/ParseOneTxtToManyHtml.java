package com.quanqinle.epub;

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
 * @author quanql
 */
public class ParseOneTxtToManyHtml {

    public static final Logger logger = LoggerFactory.getLogger(ParseOneTxtToManyHtml.class);

    static String regexChapterTitle = "^第.{1,10}章";
    static String chapterFileNameFormat = "chapter-%03d.xhtml";

    /**
     * Books are divided into three basic parts:
     * 1. front matter
     * 2. body of the book
     * 3. back matter
     */
    static TreeMap<String, List<String>> frontMatterMap = new TreeMap<>();
    static TreeMap<String, List<String>> chapterMap = new TreeMap<>();
//    static TreeMap<String, List<String>> backMatterMap = new TreeMap<>();

    /**
     * the chapter title of front matter
     */
    static String frontMatterTitle = "引言";

    static List<String> trimList = Arrays.asList("　");

    public static void main(String[] args) {
        logger.info("start main...");
        makeEpub();
        logger.info("end main...");
    }

    public static void makeEpub() {
        Path baseP = Paths.get("D:", "JL");
        Path srcFilePath = baseP.resolve("DEMO.txt");
        Path drtDirPath = baseP.resolve("HTML");

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

        writeFrontMatter(drtDirPath);
        writeChapter(drtDirPath);
    }

    /**
     * convert file lines to chapter map and front matter map
     *
     * @author quanqinle
     * @param allLines all lines of file
     */
    public static void parseLinesToMap(List<String> allLines) {
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
                        frontMatterMap.put(frontMatterTitle, copy);

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
     * save front matter to HTML
     * @param outDirPath
     */
    public static void writeFrontMatter (Path outDirPath) {
        if (frontMatterMap.size() != 1) {
            logger.error("unexpected size of front matter");
            return;
        }
        Map.Entry<String, List<String>> entry = frontMatterMap.firstEntry();
        writeHTML(entry.getKey(), 0, entry.getValue(), outDirPath);
    }

    /**
     * save chapters to HTMLs
     * @param outDirPath
     */
    public static void writeChapter (Path outDirPath) {
        int i = 1;
        for (String key : chapterMap.keySet()) {
            writeHTML(key, i, chapterMap.get(key), outDirPath);
            i++;
        }
    }
    /**
     * 保存一个章节到 html 文件
     *
     * @author 权芹乐
     * @param chapterName
     * @param bodyLines
     * @param outDirPath
     */
    public static void writeHTML (String chapterName, int chapterId, List<String> bodyLines, Path outDirPath) {
        List<String> chpLines = new ArrayList<>();

        String topPart = new String(""
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
            Files.write(outDirPath.resolve(String.format(chapterFileNameFormat, chapterId)), chpLines);
//            logger.debug("complete saving {}, first line: {}", chapterName, bodyLines.get(0));
        } catch (Exception e) {
            logger.error("fail to save: {}", chapterName);
            e.printStackTrace();
        }
    }

    /**
     * 是否章节表头行
     * @param str
     * @return
     */
    private static boolean isChapterTitle(String str) {
        Pattern p = Pattern.compile(regexChapterTitle);
        Matcher m = p.matcher(str);
        return m.find();
    }

}
