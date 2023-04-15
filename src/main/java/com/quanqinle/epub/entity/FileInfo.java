package com.quanqinle.epub.entity;

import java.nio.file.Path;
import java.util.List;

/**
 * file info, mainly for .html file
 */
public class FileInfo {
  /** file name without suffix */
  String name;
  /** the suffix of file name, including . sign */
  String suffix = ".xhtml";
  /** file name with suffix */
  String fullName;
  /** the full path */
  Path fullPath;
  /** such as text in &lt;title> or &lt;h1> in HTML */
  String describe;
  /** content */
  List<String> lines;

  public FileInfo(String name, String suffix, String fullName, Path fullPath, String describe, List<String> lines) {
    this.name = name;
    this.suffix = suffix;
    this.fullName = fullName;
    this.fullPath = fullPath;
    this.describe = describe;
    this.lines = lines;
  }

  /**
   * Constructor.
   * <p>meanwhile set fullName with default suffix
   * @param name
   * @param describe
   */
  public FileInfo(String name, String describe) {
    this.name = name;
    this.describe = describe;
    this.fullName = name + this.suffix;
  }

  /**
   * Constructor.
   * <p>meanwhile set fullName with default suffix
   *
   * @param name
   * @param describe
   * @param lines
   */
  public FileInfo(String name, String describe, List<String> lines) {
    this.name = name;
    this.describe = describe;
    // this.suffix default
    this.fullName = name + this.suffix;
    this.lines = lines;
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

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public Path getFullPath() {
    return fullPath;
  }

  public void setFullPath(Path fullPath) {
    this.fullPath = fullPath;
  }

  public String getDescribe() {
    return describe;
  }

  public void setDescribe(String describe) {
    this.describe = describe;
  }

  public List<String> getLines() {
    return lines;
  }

  public void setLines(List<String> lines) {
    this.lines = lines;
  }
}
