简体中文 | [English](./README.md)

[TOC]

# 这个项目是什么？

有些电子书是 txt 格式，读起来很不方便，我更喜欢 epub 格式的电子书。
所以，我写了这个项目，这个项目会有一些电子书处理功能，但它第一要务将始终是更好的将一个 txt 转换成 epub。

# 使用场景

## 将一个 `.txt` 格式电子书转制成 `.epub`

已有纯文本格式的电子书，如`xx.txt`，将其按章节分成多个`chapter-???.xhtml`文件，
并根据这些章节文件生成目录文件`toc.xhtml`，再使用工程中的模板`template`，生成 epub。
 
### 前提条件

> 注意：
> 
> 默认的解析章节名的正则表达式是`^第.{1,10}章[^完]`，
> 如果不符合你的要求，可以修改`Constant.REGEX_CHAPTER_TITLE`或`ConvertPlainTxtToHtmlFiles.setRegexChapterTitle()`。

纯文本文件`xx.txt`需要满足如下格式，具体要求请阅读如下文件中的内容：
```txt
文件头处允许有一些文字。
如果有，则从第一行到“第一章 xxx”之前的内容都保存在同一个`.xhtml`文件中，形成标题叫“引言”的章节。
如果没有，则不会有任何影响。

第一章 章节名1
每行首尾的空白符都会被删除。
行首是"^第.{1,10}章[^完]"之间的内容，分割为一个`.xhtml`文件

  第二章 章节名2
行首的空格不影响章节分割的判断。
第二章完 <-- 左侧这种情况不会被当成一个新章节

第三章 我是章节名

   上面的空行会被自动删除，新生成的文件不会有无用的空行。   
```

将生成一些章节文件，例如`chapter-001.xhtml`：
```html
<?xml version="1.0" encoding="utf-8" standalone="no"?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN"
        "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <title>第一章 章节名1</title>
    <link href="../Styles/main.css" rel="stylesheet" type="text/css" />
</head>
<body>
<h1>第一章 章节名1</h1>
<p>每行首尾的空白符都会被删除。</p>
<p>行首是"^第.{1,10}章[^完]"之间的内容，分割为一个`.xhtml`文件</p>
</body>
</html>
```

### 使用方法
> `MakeEpubFromTemplate.java`

```java
public class Demo() {
    public static void main(String[] args) throws IOException {
        // 源文件
        Path srcFilePath = Paths.get("D:", "book-library", "demo.txt");

        // 目标文件
        BookInfo book = new BookInfo();
        book.setOutputDir(Paths.get("D:", "epub"));
        /* 以下属性非必须 */
        book.setBookTitle("红楼梦");
        book.setAuthor("曹雪芹");
        book.setCoverJpgFullPath(Paths.get("D:", "book.jpg")); // 设置jpg格式的封面图
        // 建议设置上面3个
        book.setUUID(UUID.randomUUID().toString());
        book.setLanguage("zh");
        book.setCreateDate("2021-03-06");
        
        // make srcFilePath to a .epub book
        MakeEpubFromTemplate makeEpub = new MakeEpubFromTemplate(srcFilePath, book);
        makeEpub.make();
    }
}
```

## 将电子书文件夹压制成 `.epub`

### 前提条件
文件夹中的内容符合 epub 规范。

### 使用方法
> `MakeEpubFromTemplate.java`

```java
public class Demo() {
    public static void main(String[] args) {
        Path epubSrcFolderPath = Paths.get("D:", "epub", "book-folder");
        Path epubFilePath = Paths.get("D:", "my-book.epub");
        try {
            zipEpub(epubSrcFolderPath, epubFilePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
```

## 校验 `.epub` 是否符合规范
如果校验不通过，错误信息将打进在控制台。
```java
public class Demo() {
    public static void main(String[] args) {
        validateEpubFile(epubFilePath.toFile());
    }
}
```

# 其他
## 一个推荐的 epub 目录结构
```
├── mimetype
├── META-INF
│    └── container.xml 
└── OEBPS 
    ├── content.opf
    ├── toc.ncx
    ├── css
    |    └── main.css
    ├── images
    |    └── cover.jpg
    └── xhtml
         ├── cover.xhtml
         ├── toc.xhtml
         ├── chapter-001.xhtml
         └── chapter-002.xhtml
```

## TODO list
+ 试用[`epublib`](https://github.com/psiegman/epublib) ：搜索到这个库，据说好用，以后试试

## 参考
+ epub 模板：详见 [epub-boilerplate](https://github.com/javierarce/epub-boilerplate.git)
+ epubcheck：详见 [w3c epub check tool](https://github.com/w3c/epubcheck)
