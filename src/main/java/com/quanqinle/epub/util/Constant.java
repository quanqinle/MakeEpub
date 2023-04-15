package com.quanqinle.epub.util;

/**
 * global constant variables
 *
 * @author quanqinle
 */
public final class Constant {
  /** the folder name in resource of this project, saving epub template */
  public static final String TEMPLATE_FOLDER = "template";

  public static final String FORMAT_NAV_POINT =
      """
      <navPoint id="navPoint-%s" playOrder="%s">
        <navLabel><text>%s</text></navLabel>
        <content src="Text/%s"/>
      </navPoint>
      """; // no need end-with \n
  public static final String FORMAT_ITEM =
      "<item href='Text/%s' id='%s' media-type='application/xhtml+xml' />\n";
  public static final String FORMAT_ITEMREF = "<itemref idref='%s' />\n";
  public static final String FORMAT_REFERENCE =
      "<reference type='%s' href='Text/%s' title='%s'/>\n";
  public static final String FORMAT_TOC_ITEM = "<p><a href='../Text/%s'>%s</a></p>\n";
}
