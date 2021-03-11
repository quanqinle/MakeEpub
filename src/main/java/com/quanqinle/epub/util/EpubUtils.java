package com.quanqinle.epub.util;

import com.adobe.epubcheck.api.EpubCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    public static void main(String[] args) {
        Path epubSrcFolderPath = Paths.get("D:", "book-template");
        Path epubFilePath = Paths.get("D:", "book-library", "my-book.epub");

        try {
            zipEpub(epubSrcFolderPath, epubFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        validateEpubFile(epubFilePath.toFile());
    }

    /**
     * Zip folder to epub.
     *
     * A demo of command line:
     * $ zip -0Xq  my-book.epub mimetype
     * $ zip -Xr9Dq my-book.epub *
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
             ZipOutputStream zos = new ZipOutputStream(fos);
             ) {

            addMimetypeToEpub(zos);

            Files.walk(epubSrcFolderPath)
                    .filter(Files::isRegularFile)
                    .filter(path -> !mimetypeFileName.equals(path.getFileName().toString()))
                    .forEach(
                            path ->
                            {
                                Path relativePath = epubSrcFolderPath.relativize(path);
                                // IMPORTANT!! epub need / sign, not \ sign in entry name.
                                String entryName = relativePath.toString().replace("\\", "/");

                                try {
                                    addFileToZip(entryName, path, zos);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                    );
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
        zos.closeEntry();
    }

    /**
     * Write file to zip output stream
     *
     * @param entryName use in `new ZipEntry(entryName)`
     * @param file the file which will be witten into zip
     * @param zos zip output stream
     * @throws IOException -
     */
    private static void addFileToZip(String entryName, Path file, ZipOutputStream zos) throws IOException {

        if (Files.isDirectory(file) || Files.isHidden(file)) {
            return;
        }

        try (FileInputStream in = new FileInputStream(file.toFile().getPath())) {
            zos.putNextEntry(new ZipEntry(entryName));

            byte[] buffer = new byte[1024];
            int len;
            long size = 0L;
            while ((len = in.read(buffer)) > 0) {
                zos.write(buffer, 0, len);
                size += len;
            }
//            logger.debug("Stored {} bytes to {}", size, path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            // closeEntry() should be called after putNextEntry() each time
            zos.closeEntry();
        }

    }

    /**
     * Validate .epub file using w3c epubcheck tool.
     * If false, errors are printed on stderr stream.
     *
     * @param epubFile .epub file
     * @return -
     */
    public static boolean validateEpubFile(File epubFile) {
        // simple constructor; errors are printed on stderr stream
        EpubCheck epubcheck = new EpubCheck(epubFile);

        // validate() returns true if no errors or warnings are found
        return epubcheck.validate();
    }

}
