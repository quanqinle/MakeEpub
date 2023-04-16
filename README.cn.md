简体中文 | [English](./README.md)

[TOC]

# 这个项目是什么？

有些电子书是 txt 格式，读起来很不方便，我更喜欢 epub 格式的电子书。
所以，我写了这个项目，这个项目会有一些电子书处理功能，但它第一要务将始终是更好的将一个 txt 转换成 epub。

# 使用场景

## 将一个 `.txt` 格式电子书转制成 `.epub`

已有纯文本格式的电子书，如`xx.txt`，将其按章节分成多个`chapter-???.xhtml`文件，并根据这些章节文件生成目录文件`toc.xhtml`，再使用工程中的模板`template`，生成 epub。

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
见 `MakeEpubFromTemplateTest.java`

推荐通过 `bookinfo.yaml` 文件配置参数，然后完成书籍转换。
```Java
class MakeEpubFromTemplateTest {
  @Test
  void makeByBookInfoInYaml() throws IOException {
    // 书的配置文件
    ObjectMapper mapper = new YAMLMapper();
    BookInfo book = mapper.readValue(Files.newInputStream(Path.of("bookinfo.yaml")), BookInfo.class);
    
    MakeEpubFromTemplate makeEpub = new MakeEpubFromTemplate(book);
    makeEpub.make();
  }
}
```

或者，给对象设置参数。
```Java
class MakeEpubFromTemplateTest {

  void make() {
    BookInfo book = new BookInfo();
    // 源文件
    book.setSrcTxtPath(Paths.get("D:", "红楼梦.txt"));
    // 目标文件
    book.setOutputDir(Paths.get("D:", "demo"));
    book.setHasManyBooks(true);

    /* 以下属性非必须 */
    book.setBookTitle("红楼梦");
    book.setAuthor("曹雪芹");
    // 设置 jpg 格式的封面图
    book.setCoverJpgFullPath(Paths.get("D:", "book.jpg"));

    // 建议设置上面3个

    book.setUuid(UUID.randomUUID().toString());
    book.setLanguage("zh");
    book.setCreateDate("2023-04-07");

    // make srcFilePath to a .epub book
    MakeEpubFromTemplate makeEpub = new MakeEpubFromTemplate(book);
    try {
      makeEpub.make();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
}
```

## 将电子书文件夹压制成 `.epub`

如果你只是想把文件夹里的内容压制成 epub，参考下面例子。
> 前提条件：
> 文件夹中的内容符合 epub 规范。

见 `EpubUtilsTest.java`

```java
class EpubUtilsTest {

  @Test
  void zipEpub() {
    Path epubSrcFolderPath = Paths.get("D:", "book-template");
    Path epubFilePath = Paths.get("D:", "my-book.epub");

    try {
      EpubUtils.zipEpub(epubSrcFolderPath, epubFilePath);
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
