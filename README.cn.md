简体中文 | [English](./README.md)

# 这个项目是什么？
Sometimes I have some e-books to read, which are in plain text format. However, I prefer to read books in epub format.
So I write this project, it can convert a `.txt` file to a `.epub` file.

# 使用场景

## 将一个 `.txt` 格式电子书转制成 `.epub`

已有纯文本格式的电子书，如`xx.txt`，将其按章节分成多个`chapter-???.xhtml`文件，再使用工程中的模板`template`，生成epub。
 
### 前提条件
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

### 使用方法
> `MakeEpubFromTemplate.java`

```java
public class Demo() {
    public static void main(String[] args) throws IOException, URISyntaxException {
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
        
        // making srcFilePath to a .epub book
        MakeEpubFromTemplate makeEpub = new MakeEpubFromTemplate(srcFilePath, book);
        makeEpub.make();
    }
}
```

# 参考
+ epub模板：详见 [epub-boilerplate](https://github.com/javierarce/epub-boilerplate.git)
