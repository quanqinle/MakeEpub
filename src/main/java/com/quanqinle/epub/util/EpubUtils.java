package com.quanqinle.epub.util;

import com.adobe.epubcheck.api.EpubCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * epub utils
 *
 * @author quanqinle
 */
public class EpubUtils {

  public static final Logger logger = LoggerFactory.getLogger(EpubUtils.class);

  /**
   * Zip folder to epub.
   *
   * <p>A demo of command line:
   *
   * <p>$ zip -0Xq my-book.epub mimetype
   *
   * <p>$ zip -Xr9Dq my-book.epub *
   *
   * @param epubSrcFolderPath the source of epub folder
   * @param epubFilePath the epub file
   * @throws IOException -
   */
  public static void zipEpub(Path epubSrcFolderPath, Path epubFilePath) throws IOException {
    logger.debug("source folder = {}", epubSrcFolderPath);
    logger.debug("target epub = {}", epubFilePath);

    String mimetypeFileName = "mimetype";

    try (FileOutputStream fos = new FileOutputStream(String.valueOf(epubFilePath));
        ZipOutputStream zos = new ZipOutputStream(fos)) {

      addMimetypeToEpub(zos);

      Files.walk(epubSrcFolderPath)
          .filter(Files::isRegularFile)
          .filter(path -> !mimetypeFileName.equals(path.getFileName().toString()))
          .forEach(
              path -> {
                Path relativePath = epubSrcFolderPath.relativize(path);
                // IMPORTANT!! epub need / sign in entry name (NOT \ sign)
                String entryName = relativePath.toString().replace("\\", "/");

                try (FileInputStream fin = new FileInputStream(path.toFile().getPath())) {
                  if (Files.isSymbolicLink(path) || Files.isHidden(path)) {
                    // TODO
                    assert true : "I am not sure if need to check these";
                  } else {
                    zos.putNextEntry(new ZipEntry(entryName));
                    fin.transferTo(zos);
                    zos.closeEntry();
                  }
                } catch (IOException e) {
                  e.printStackTrace();
                }
              });
    }
  }

  /**
   * Add mimetype file into epub, only store, do not compress.
   *
   * @param zos zip output stream
   * @throws IOException -
   */
  private static void addMimetypeToEpub(ZipOutputStream zos) throws IOException {
    String mimetypeFileName = "mimetype";
    String mimetypeFileContent = "application/epub+zip";

    ZipEntry entry = new ZipEntry(mimetypeFileName);

    // IMPORTANT!! uncompressed entry
    entry.setMethod(ZipEntry.STORED);

    byte[] mimetypeBytes = mimetypeFileContent.getBytes();
    entry.setSize(mimetypeBytes.length);

    CRC32 crc = new CRC32();
    crc.update(mimetypeBytes);
    long value = crc.getValue();
    entry.setCrc(value);

    zos.putNextEntry(entry);
    zos.write(mimetypeBytes);
    // closeEntry() should be called after putNextEntry() each time
    zos.closeEntry();
  }

  /**
   * Validate .epub file using w3c epubcheck tool.
   *
   * @param epubFile .epub file
   * @return If false, errors are printed on stderr stream.
   */
  public static boolean validateEpubFile(File epubFile) {
    // simple constructor; errors are printed on stderr stream
    EpubCheck epubcheck = new EpubCheck(epubFile);

    // validate() returns true if no errors or warnings are found
    return epubcheck.validate();
  }

  /**
   * Write all lines of a chapter in a html file
   *
   * @author quanqinle
   * @param chapterName chapter title, such as "Chapter ONE A Dance with Dragons"
   * @param bodyLines all lines of this chapter
   * @param htmlPath full name of the output file
   */
  public static void writeHtmlFile(String chapterName, List<String> bodyLines, Path htmlPath) {
    List<String> chpLines = new ArrayList<>();

    String topPart =
            """
            <?xml version="1.0" encoding="utf-8"?>
            <!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN"
              "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
            <html xmlns="http://www.w3.org/1999/xhtml">
            <head>
              <title>$TITLE</title>
              <link href="../Styles/main.css" type="text/css" rel="stylesheet"/>
            </head>
            <body>
            """.replace("$TITLE", chapterName);
    String bottomPart = "</body>\r\n</html>";

    chpLines.add(topPart);
    chpLines.addAll(bodyLines);
    chpLines.add(bottomPart);

    try {
      Files.write(htmlPath, chpLines);
      logger.debug("Saved file: {}, chapter: {}, first line: {}", htmlPath.getFileName(), chapterName, bodyLines.get(0));
    } catch (Exception e) {
      logger.error("Fail to save: {}", chapterName);
      e.printStackTrace();
    }
  }

}
