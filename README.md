[简体中文](./README.cn.md) | English

> To update to support 'epub3'

# What is this?

I LOVE EPUB.

Sometimes I have some e-books to read, which are in plain text format. 
Files in txt format are not convenient to read, I prefer books in epub format. 
So I write this project, its main purpose is to convert a `.txt` file to a `.epub` file.

# When to use?

## convert a `.txt` ebook to a `.epub`

### Prerequisites
> Note:
> 
> The default regular expression for parsing chapter names is `^第.{1,10}章[^完]`, 
> if it does not meet your requirements, you can modify `Constant.REGEX_CHAPTER_TITLE` or `ConvertPlainTxtToHtmlFiles.setRegexChapterTitle()`.

The plain text file `xx.txt` needs to meet the following format. For specific requirements, please read the contents of the file below:
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

Many chapter xhtml will be created, such as `chapter-001.xhtml`:
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

### Usage

See `MakeEpubFromTemplateTest.java`

[Recommend] Use `bookinfo.yaml` to set parameters, then complete to make the book。
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

Or, set parameters by book object.
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


## Zip a folder into `.epub`
If you want to zip a folder which contains book contents into a `.epub`. 

> Prerequisites: 
> The folder needs to contain some right epub resources.

See `EpubUtilsTest.java`

```Java
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

## Validate `.epub`
Validate `.epub` against the EPUB specifications, the errors are printed on stderr stream.
```java
public class Demo() {
    public static void main(String[] args) {
        validateEpubFile(epubFilePath.toFile());
    }
}
```

# Others
## A recommended framework
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
+ Try [`epublib`](https://github.com/psiegman/epublib) : articles on the Internet say it is a good java library for epub. Try it later.

## Reference
+ A epub template: refer to [epub-boilerplate](https://github.com/javierarce/epub-boilerplate.git)
+ epubcheck: refer to [w3c epub check tool](https://github.com/w3c/epubcheck)
