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

    BookInfo book;
    /**
     * original plain text file, the full path can be read directly
     */
    Path srcFilePath;
    /**
     * the full path of epub template, copy from it, do not modify the original files
     */
    private Path templateSrcPath;
    /**
     * copy epub template to this folder, then modify it
     */
    private Path templateDstPath;

    /**
     * `<navPoint></navPoint>` list in `toc.ncx`
     */
    private String navPointList = "";
    /**
     * `<item></item>` list in `content.opf`
     */
    private String itemList = "";
    /**
     * `<itemref></itemref>` list in `content.opf`
     */
    private String itemrefList = "";
    /**
     * `<reference></reference>` list in `content.opf`
     */
    private String referenceList = "";
    /**
     * `<li></li>` list in `toc.xhtml`
     */
    private String tocItemList = "";

    public static void main(String[] args) throws IOException, URISyntaxException {
        Path srcFilePath = Paths.get("D:", "book-library", "DEMO.txt");

        BookInfo book = new BookInfo();
        book.setOutputDir(Paths.get("D:", "epub"));
        book.setCoverJpgFullPath(Paths.get("D:", "book.jpg"));
        book.setUuid(UUID.randomUUID().toString());
        book.setLanguage("zh");
        book.setBookTitle("红楼梦");
        book.setAuthor("曹雪芹");
        book.setCreateDate("2021-03-06");

        /*
         * demo: make srcFilePath to a .epub book
         */
        MakeEpubFromTemplate makeEpub = new MakeEpubFromTemplate(srcFilePath, book);
        makeEpub.make();
    }

    /**
     * overload {@link #MakeEpubFromTemplate(Path srcFilePath, Path templateSrcPath, BookInfo book)} with templateSrcPath=null.
     *
     * @param srcFilePath original plain text file
     * @param book output book
     */
    public MakeEpubFromTemplate(Path srcFilePath, BookInfo book)  {
        this(srcFilePath, null, book);
    }

    /**
     *
     * @param srcFilePath original plain text file
     * @param templateSrcPath source path of epub template folder. If null, use default value which is from this project
     * @param book output book
     */
    public MakeEpubFromTemplate(Path srcFilePath, Path templateSrcPath, BookInfo book) {
        this.srcFilePath = srcFilePath;
        this.book = book;
        this.templateDstPath = book.getOutputDir().resolve(Constant.TEMPLATE_NAME);

        if (templateSrcPath != null) {
            this.templateSrcPath = templateSrcPath;
        } else {
            try {
                // The codes below will run error, if execute jar created from this project which resource folder in it.
                URL templateSrcUrl = getClass().getClassLoader().getResource(Constant.TEMPLATE_NAME);
                assert templateSrcUrl != null;
                this.templateSrcPath = Paths.get(templateSrcUrl.toURI());
            } catch (URISyntaxException e) {
                logger.error("Fail to find the epub source template: {}", templateSrcPath);
                System.exit(0);
            }
        }
    }

    /**
     * Start to make the .epub file.
     *
     * The method is an all in one method, it includes the whole steps of read-parse-rewrite, etc.,
     * so you can use it to make a .epub book just after the construction method.
     *
     * @throws IOException -
     */
    public void make() throws IOException {
        copyTemplate();
        genBodyHtmls();
        setBookCover();
        setBookTocHtml();
        modifyTocNcx();
        modifyContentOpf();

        zipEpub();
    }

    /**
     * Zip folder to epub.
     *
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
        EpubUtils.zipEpub(this.templateDstPath, book.getOutputDir().resolve(bookName + ".epub"));
    }


    /**
     * copy epub source template to the output directory.
     * This method will delete (if exist) and create the output directory.
     */
    public void copyTemplate() throws IOException {

        Path src = templateSrcPath;
        Path dst = templateDstPath;
//        Files.walk(src).forEach(System.out::println); // debug

        logger.info("source template = {}", src);
        logger.info("output folder = {}", dst);

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

    /**
     * Modify cover.xhtml
     *
     * @throws IOException -
     */
    public void setBookCover() throws IOException {
        Path coverHtml = templateDstPath.resolve("OEBPS/Text/cover.xhtml");
        String content =Files.readString(coverHtml);
        content = content.replace("[BOOK'S TITLE]", book.getBookTitle());
        Files.writeString(coverHtml, content);

        Path src = book.getCoverJpgFullPath();
        Path dst = templateDstPath.resolve("OEBPS/Images/cover.jpg");
        if (src != null && Files.exists(src)) {
            Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
        } else {
            // TODO maybe we should create a jpg based on book title
        }
    }

    /**
     * Modify toc.xhtml
     *
     * @throws IOException -
     */
    public void setBookTocHtml() throws IOException {
        Path tocHtml = templateDstPath.resolve("OEBPS/Text/toc.xhtml");
        String content =Files.readString(tocHtml);
        content = content.replace("[toc item]", this.tocItemList)
                .replace("[TOC TITLE]", Constant.TOC_TITLE);
        Files.writeString(tocHtml, content);
    }

    /**
     * Generate HTML files of the book,
     * and init some parameters which could be used when setting toc.xhtml, toc.ncx, content.opf, etc.
     *
     * @author quanqinle
     */
    public void genBodyHtmls() {
        ConvertPlainTxtToHtmlFiles parse = new ConvertPlainTxtToHtmlFiles(this.srcFilePath, book);
        parse.convert();

        makeContentForTocNcxAndContentOpf();
    }

    /**
     * Modify toc.ncx
     */
    public void modifyTocNcx() throws IOException {
        Path tocNcxPath = templateDstPath.resolve("OEBPS/toc.ncx");

        String content = Files.readString(tocNcxPath);
        content = content.replace("[ISBN]", book.getIsbn())
                .replace("[BOOK'S TITLE]", book.getBookTitle())
                .replace("[navPoint LIST]", this.navPointList);

        Files.writeString(tocNcxPath, content);
    }

    /**
     * Modify content.opf
     */
    public void modifyContentOpf() throws IOException {
        Path contentOpfPath = templateDstPath.resolve("OEBPS/content.opf");

        String content =Files.readString(contentOpfPath);
        content = content.replace("[UUID]", book.getUuid())
                .replace("[ISBN]", book.getIsbn())
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
     * This method has been used in {@link #genBodyHtmls()}
     */
    private void makeContentForTocNcxAndContentOpf() {
        int index = 1;

        // cover
        navPointList = navPointList.concat(String.format(Constant.FORMAT_NAV_POINT, index, index, Constant.COVER_TITLE, "cover.xhtml"));
        referenceList = referenceList.concat(String.format(Constant.FORMAT_REFERENCE, "cover", "cover.xhtml", Constant.COVER_TITLE));
        tocItemList = tocItemList.concat(String.format(Constant.FORMAT_TOC_ITEM, "cover.xhtml", Constant.COVER_TITLE));
        index++;

        // TOC
        navPointList = navPointList.concat(String.format(Constant.FORMAT_NAV_POINT, index, index, Constant.TOC_TITLE, "toc.xhtml"));
        referenceList = referenceList.concat(String.format(Constant.FORMAT_REFERENCE, "toc", "toc.xhtml", Constant.TOC_TITLE));
        tocItemList = tocItemList.concat(String.format(Constant.FORMAT_TOC_ITEM, "toc.xhtml", Constant.TOC_TITLE));
        index++;

        int i = 0;
        for (String chapterTitle: book.getHtmlFileMap().keySet()) {
            if (0 == i && !Constant.FRONT_MATTER_TITLE.equals(chapterTitle)) {
                // the chapter just before the 1st chapter
                i++;
                continue;
            }

            FileInfo file = book.getHtmlFileMap().get(chapterTitle);
            String fileName = file.getName();

            navPointList = navPointList.concat(String.format(Constant.FORMAT_NAV_POINT, index, index, chapterTitle, fileName));
            itemList = itemList.concat(String.format(Constant.FORMAT_ITEM, fileName, i));
            itemrefList = itemrefList.concat(String.format(Constant.FORMAT_ITEMREF, i));
            referenceList = referenceList.concat(String.format(Constant.FORMAT_REFERENCE, "text", fileName, chapterTitle));
            tocItemList = tocItemList.concat(String.format(Constant.FORMAT_TOC_ITEM, fileName, chapterTitle));

            index++;
            i++;
        }

    }

}
