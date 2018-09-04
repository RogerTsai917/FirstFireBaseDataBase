package com.roger.firstfirebasedatabase;

public class Article {
    public String article_title;
    public String article_content;
    public String article_tag;
    public Author author;
    public String created_time;

    public Article() {

    }

    public Article(String title, String content, String tag, Author author, String time) {
        article_title = title;
        article_content = content;
        article_tag = tag;
        this.author = author;
        created_time = time;
    }
}
