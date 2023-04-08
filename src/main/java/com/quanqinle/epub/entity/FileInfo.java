package com.quanqinle.epub.entity;

import java.nio.file.Path;

/**
 * File info, such as .xhtml
 *
 * @author quanqinle
 */
public class FileInfo {
  /** the index in the same type files */
  int order;
  /** file name with suffix */
  String name;
  /** the suffix of file name, including . sign */
  String suffix;
  /** file name without suffix */
  String shortName;
  /**
   * such as `<title>` or `<h1>` in HTML
   */
  String describe;
  /** the full path */
  Path fullPath;

  public FileInfo() {}

  public FileInfo(String name) {
    this.name = name;
  }

  public FileInfo(int order, String name, String describe) {
    this.order = order;
    this.name = name;
    this.describe = describe;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(String suffix) {
    this.suffix = suffix;
  }

  public String getShortName() {
    return shortName;
  }

  public void setShortName(String shortName) {
    this.shortName = shortName;
  }

  public String getDescribe() {
    return describe;
  }

  public void setDescribe(String describe) {
    this.describe = describe;
  }

  public Path getFullPath() {
    return fullPath;
  }

  public void setFullPath(Path fullPath) {
    this.fullPath = fullPath;
  }
}
