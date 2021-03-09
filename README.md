[简体中文](./README.cn.md) | English

# What is this?
Sometimes I have some e-books to read, which are in plain text format. However, I prefer to read books in epub format.
So I write this project, it can convert a `.txt` file to a `.epub` file.

# When to use?

## convert a `.txt` ebook to a `.epub`

### Prerequisites

Only for chinese book now.

### Usage
> `MakeEpubFromTemplate.java`

```java
public class Demo() {
    public static void main(String[] args) throws IOException, URISyntaxException {
        // source .txt file
        Path srcFilePath = Paths.get("D:", "book-library", "demo.txt");

        // target .epub file 
        BookInfo book = new BookInfo();
        book.setOutputDir(Paths.get("D:", "epub"));
        /* The following attributes are not required */
        book.setBookTitle("红楼梦");
        book.setAuthor("曹雪芹");
        book.setCoverJpgFullPath(Paths.get("D:", "book.jpg"));
        // Suggestion: Set the three above
        book.setUUID(UUID.randomUUID().toString());
        book.setLanguage("zh");
        book.setCreateDate("2021-03-06");
        
        // making srcFilePath to a .epub book
        MakeEpubFromTemplate makeEpub = new MakeEpubFromTemplate(srcFilePath, book);
        makeEpub.make();
    }
}
```

# Reference
+ A epub template: refer to [epub-boilerplate](https://github.com/javierarce/epub-boilerplate.git)