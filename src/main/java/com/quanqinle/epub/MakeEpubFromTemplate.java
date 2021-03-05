package com.quanqinle.epub;

public class MakeEpubFromTemplate {




    /**
     * 拷贝epub模板
     */
    public void copyTemplate() {

    }

    public void getBodyHtmls() {

    }

    /** toc.ncx
     ${ISBN}

     [BOOK'S TITLE]

     [navPoint LIST]--COVER--Front Matter--TOC--BOOK BODY
     <navPoint id="navPoint-1" playOrder="1">
     <navLabel>
     <text>Chapter 1</text>
     </navLabel>
     <content src="Text/ch01.xhtml"/>
     </navPoint>
     */
    public void modifyTocNcx() {

    }

    /** content.opf
     [BOOK'S TITLE]
     [ISBN]
     [NAME LASTNAME]--CREATOR
     [LASTNAME, NAME]
     [LANGUAGE]--en--zh
     [DATE]--2012-01-01

     [manifest item list]
     <item href="Text/chapter01.xhtml" id="chapter-1" media-type="application/xhtml+xml" />

     [spine itemref list]
     <itemref idref="chapter-1" />

     [guide reference list]
     <reference href="Text/frontmatter.xhtml" type="copyright-page" />
     <reference href="Text/foreword.xhtml" type="foreword" />
     <reference href="Text/chapter01.xhtml" type="text" />
     */
    public void modifyContentOpf() {

    }


}
