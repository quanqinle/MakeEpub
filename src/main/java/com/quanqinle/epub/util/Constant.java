package com.quanqinle.epub.util;

import java.util.List;

/**
 * global constant variables
 *
 * @author quanqinle
 */
public final class Constant {

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
