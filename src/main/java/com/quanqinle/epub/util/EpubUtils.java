package com.quanqinle.epub.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author quanqinle
 */
public class EpubUtils {

    public static final Logger logger = LoggerFactory.getLogger(EpubUtils.class);

    public static void main(String[] args) {
        Path epubSrcFolderPath = Paths.get("D:", "book-template");
        Path epubFilePath = Paths.get("D:", "book-library", "my-book.epub");
        try {
            zipEpub(epubSrcFolderPath, epubFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * zip folder to epub.
     *
     * $ zip -0Xq  my-book.epub mimetype
     * $ zip -Xr9Dq my-book.epub *
     *
     * @param epubSrcFolderPath - the source of epub folder
     * @param epubFilePath - the epub file
     * @throws IOException -
     */
    public static void zipEpub(Path epubSrcFolderPath, Path epubFilePath) throws IOException {
        logger.debug("source folder = {}", epubSrcFolderPath);
        logger.debug("target epub = {}", epubFilePath);

        FileOutputStream fos = new FileOutputStream(String.valueOf(epubFilePath));
        ZipOutputStream zos = new ZipOutputStream(fos);

        Files.walk(epubSrcFolderPath)
                .forEach(
                        path ->
                        {
                            try {
                                if (!Files.isDirectory(path)) {

                                    Path relativePath = epubSrcFolderPath.relativize(path);
                                    String relativePathName = relativePath.toString();

//                                    logger.debug("File Added = {}", path);
//                                    logger.debug("zip entry = {}", relativePathName);

                                    zos.putNextEntry(new ZipEntry(relativePathName));

                                    FileInputStream in = new FileInputStream(String.valueOf(path));
                                    int len;
                                    byte[] buffer = new byte[2046];
                                    while ((len = in.read(buffer)) > 0) {
                                        zos.write(buffer, 0, len);
                                    }

                                    in.close();
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                );

        zos.closeEntry();
        zos.close();
    }
}
