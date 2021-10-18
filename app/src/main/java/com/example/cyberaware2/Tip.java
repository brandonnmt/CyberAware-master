package com.example.cyberaware2;

import java.io.Serializable;

public class Tip implements Serializable {
    private String Title;
    private String Content;

    public Tip(String title, String content) {
        Title = title;
        Content = content;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }

    @Override
    public String toString() {
        return Title;
    }
}
