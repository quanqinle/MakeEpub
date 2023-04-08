package com.quanqinle.epub.util;

import java.util.List;

/**
 * global constant variables
 *
 * @author quanqinle
 */
public final class Constant {

  /** Note: modify this regex if the chapter title is not match in your book. */
  public static final List<String> CHAPTER_TITLE_REGEX_LIST =
      List.of("^第.{1,10}章[^完]", "^第.{1,10}节");

  /** the folder name in resource of this project, saving epub template */
  public static final String TEMPLATE_NAME = "template";

  /** the chapter title of front matter, the chapter just before the 1st chapter */
  public static final String FRONT_MATTER_TITLE = "引言";
  /**
   * the chapter title of cover, used in places like these: <title><h1>, etc.
   */
  public static final String COVER_TITLE = "封面";
  /**
   * the chapter title of TOC, used in places like these: <title><h1>, etc.
   */
  public static final String TOC_TITLE = "目录";

  public static final String FORMAT_NAV_POINT =
      """
      <navPoint id="navPoint-%s" playOrder="%s">
        <navLabel><text>%s</text></navLabel>
        <content src="Text/%s"/>
      </navPoint>
      """;
  public static final String FORMAT_ITEM =
      "<item href='Text/%s' id='chapter-%s' media-type='application/xhtml+xml />\n";
  public static final String FORMAT_ITEMREF = "<itemref idref='chapter-%s' />\n";
  public static final String FORMAT_REFERENCE =
      "<reference type='%s' href='Text/%s' title='%s'/>\n";
  public static final String FORMAT_TOC_ITEM = "<p><a href='../Text/%s'>%s</a></p>\n";
}
