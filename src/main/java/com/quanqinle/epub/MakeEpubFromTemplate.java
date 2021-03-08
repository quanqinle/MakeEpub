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
import java.util.UUID;


/**
 * Make the EPUB using the existing template
 *
 * @author quanqinle
 */
public class MakeEpubFromTemplate {

    public static final Logger logger = LoggerFactory.getLogger(MakeEpubFromTemplate.class);

    /**
     * original plain text file, the full path can be read directly
     */
    Path srcFilePath;
    BookInfo book;
    /**
     * the full path of epub template, copy from it, do not modify the original files
     */
    URL templateSrcUrl;
    /**
     * copy epub template to this folder, then modify it
     */
    Path templateDstPath;

    /**
     * `<navPoint></navPoint>` list in `toc.ncx`
     */
    String navPointList = "";
    /**
     * `<item></item>` list in `content.opf`
     */
    String itemList = "";
    /**
     * `<itemref></itemref>` list in `content.opf`
     */
    String itemrefList = "";
    /**
     * `<reference></reference>` list in `content.opf`
     */
    String referenceList = "";
    /**
     * `<li></li>` list in `toc.xhtml`
     */
    String tocItemList = "";

    public static void main(String[] args) throws IOException, URISyntaxException {
        Path srcFilePath = Paths.get("D:", "book-library", "demo.txt");

        BookInfo book = new BookInfo();
        book.setOutputDir(Paths.get("D:", "epub"));
        book.setCoverJpgFullPath(Paths.get("D:", "book.jpg"));
        book.setUUID(UUID.randomUUID().toString());
        book.setLanguage("zh");
        book.setBookTitle("红楼梦");
        book.setAuthor("曹雪芹");
        book.setCreateDate("2021-03-06");

        /*
         the following is a complete demo, making srcFilePath to a .epub book
         */
        MakeEpubFromTemplate makeEpub = new MakeEpubFromTemplate(srcFilePath, book);
        makeEpub.copyTemplate();
        makeEpub.genBodyHtmls();
        makeEpub.setBookCover();
        makeEpub.setBookTocHtml();
        makeEpub.modifyTocNcx();
        makeEpub.modifyContentOpf();

        makeEpub.zipEpub();
    }

    /**
     *
     * @param srcFilePath - original plain text file
     * @param book -
     */
    public MakeEpubFromTemplate(Path srcFilePath, BookInfo book) {
        this.srcFilePath = srcFilePath;
        this.book = book;
        this.templateDstPath = book.getOutputDir().resolve(Constant.templateName);
        this.templateSrcUrl = this.getClass().getClassLoader().getResource(Constant.templateName);
    }

    /**
     * zip folder to epub.
     * Use {@link com.quanqinle.epub.util.EpubUtils#zipEpub(Path epubSrcFolderPath, Path epubFilePath)}
     * Folder is from the copy of original template.
     * epub is from book.outputdir+book.title+.epub.
     *
     * @throws IOException -
     */
    public void zipEpub() throws IOException {
        String bookName = book.getBookTitle();
        if (null == bookName || bookName.isBlank()) {
            bookName = "ebook";
        }
        EpubUtils.zipEpub(this.getTemplateDstPath(), book.getOutputDir().resolve(bookName + ".epub"));
    }


    /**
     * 拷贝epub模板
     */
    public void copyTemplate() throws IOException, URISyntaxException {

        Path src = Paths.get(templateSrcUrl.toURI());
//        Files.walk(src).forEach(System.out::println);

        Path dst = templateDstPath;

        logger.info("src={}", src);
        logger.info("dst={}", dst);

        if (Files.exists(dst)) {
            Files.walk(dst)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.createDirectories(dst);

        Files.walk(src)
                .forEach(
                        source ->
                        {
                            try {
                                Files.copy(source, dst.resolve(src.relativize(source)), StandardCopyOption.REPLACE_EXISTING);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                );
    }

    public void setBookCover() throws IOException {
        Path coverHtml = templateDstPath.resolve("OEBPS/Text/cover.xhtml");
        String content =Files.readString(coverHtml);
        content = content.replace("[BOOK'S TITLE]", book.getBookTitle());
        Files.writeString(coverHtml, content);

        Path src = book.getCoverJpgFullPath();
        Path dst = templateDstPath.resolve("OEBPS/Images/cover.jpg");
        if (src != null) {
            Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
        } else {
            // TODO
        }
    }

    /**
     * 设置 toc.xhtml
     *
     * @throws IOException -
     */
    public void setBookTocHtml() throws IOException {
        Path tocHtml = templateDstPath.resolve("OEBPS/Text/toc.xhtml");
        String content =Files.readString(tocHtml);
        content = content.replace("[toc item]", this.tocItemList)
                .replace("[TOC TITLE]", "目录");
        Files.writeString(tocHtml, content);
    }

    /**
     * generate HTML files of the book,
     * and init some parameters which could be used when setting toc.xhtml, toc.ncx, content.opf, etc.
     *
     * @author quanqinle
     */
    public void genBodyHtmls() {
        ConvertPlainTxtToHtmlFiles parse = new ConvertPlainTxtToHtmlFiles(this.srcFilePath, book);
        parse.doConvert();

        makeContentForTocNcxAndContentOpf();
    }

    /**
     * toc.ncx
     */
    public void modifyTocNcx() throws IOException {
        Path tocNcxPath = templateDstPath.resolve("OEBPS/toc.ncx");

        String content = Files.readString(tocNcxPath);
        content = content.replace("[ISBN]", book.getISBN())
                .replace("[BOOK'S TITLE]", book.getBookTitle())
                .replace("[navPoint LIST]", this.navPointList);

        Files.writeString(tocNcxPath, content);
    }

    /**
     * content.opf
     */
    public void modifyContentOpf() throws IOException {
        Path contentOpfPath = templateDstPath.resolve("OEBPS/content.opf");

        String content =Files.readString(contentOpfPath);
        content = content.replace("[UUID]", book.getUUID())
                .replace("[ISBN]", book.getISBN())
                .replace("[BOOK'S TITLE]", book.getBookTitle())
                .replace("[NAME LASTNAME]", book.getAuthor())
                .replace("[LASTNAME, NAME]", book.getAuthor())
                .replace("[LANGUAGE]", book.getLanguage())
                .replace("[DATE]", book.getCreateDate());


        content = content.replace("[manifest item list]", this.itemList)
                .replace("[spine itemref list]", this.itemrefList)
                .replace("[guide reference list]", this.referenceList);

        Files.writeString(contentOpfPath, content);
    }


    /**
     * organize contents for toc.ncx and content.opf.
     *
     * This method has been used in `genBodyHtmls()`
     */
    private void makeContentForTocNcxAndContentOpf() {
        int index = 1;

        navPointList = navPointList.concat(String.format(Constant.navPointFormat, index, index, "封面", "cover.xhtml"));
        referenceList = referenceList.concat(String.format(Constant.referenceFormat, "cover", "cover.xhtml", "封面"));
        tocItemList = tocItemList.concat(String.format(Constant.tocItemFormat, "cover.xhtml", "封面"));
        index++;

        navPointList = navPointList.concat(String.format(Constant.navPointFormat, index, index, "目录", "toc.xhtml"));
        referenceList = referenceList.concat(String.format(Constant.referenceFormat, "toc", "toc.xhtml", "目录"));
        tocItemList = tocItemList.concat(String.format(Constant.tocItemFormat, "toc.xhtml", "目录"));
        index++;

        int i = 0;
        for (String chapterTitle: book.getHtmlFileMap().keySet()) {
            if (0 == i && !Constant.frontMatterTitle.equals(chapterTitle)) {
                // the chapter just before the 1st chapter
                i++;
                continue;
            }

            FileInfo file = book.getHtmlFileMap().get(chapterTitle);
            String fileName = file.getName();

            navPointList = navPointList.concat(String.format(Constant.navPointFormat, index, index, chapterTitle, fileName));
            itemList = itemList.concat(String.format(Constant.itemFormat, fileName, i));
            itemrefList = itemrefList.concat(String.format(Constant.itemrefFormat, i));
            referenceList = referenceList.concat(String.format(Constant.referenceFormat, "text", fileName, chapterTitle));
            tocItemList = tocItemList.concat(String.format(Constant.tocItemFormat, fileName, chapterTitle));

            index++;
            i++;
        }

    }

    public Path getTemplateDstPath() {
        return templateDstPath;
    }

    public void setTemplateDstPath(Path templateDstPath) {
        this.templateDstPath = templateDstPath;
    }
}
