package com.quanqinle.epub.util;

/**
 * global constant variables
 *
 * @author quanqinle
 */
public final class Constant {

    /**
     * the folder name in resource of this project, saving epub template
     */
    public static final String templateName = "template";

    /**
     * the chapter title of front matter, the chapter just before the 1st chapter
     */
    public static final String frontMatterTitle = "引言";

    public static final String navPointFormat = ""
            + "<navPoint id=\"navPoint-%s\" playOrder=\"%s\">"
            + "<navLabel><text>%s</text></navLabel>"
            + "<content src=\"Text/%s\"/>"
            + "</navPoint>\n";
    public static final String itemFormat = ""
            + "<item href=\"Text/%s\" id=\"chapter-%s\" media-type=\"application/xhtml+xml\" />\n";
    public static final String itemrefFormat = "<itemref idref=\"chapter-%s\" />\n";
    public static final String referenceFormat = "<reference type=\"%s\" href=\"Text/%s\" title=\"%s\"/>\n";
    public static final String tocItemFormat = "<p><a href=\"../Text/%s\">%s</a></p>";
}
