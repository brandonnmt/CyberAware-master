package com.example.cyberaware2;

import java.io.Serializable;

/**
 * Created by Vhl2 on 6/14/2019.
 * Object containing article information
 */
public class Article implements Serializable {
    private String content;      // article description
    private String articleTitle; // articles title
    private String url;          // article url
    private String image;        // url for article image

    /**
     * url getter
     * @return url
     */
    public String getUrl() {
        return url;
    }

    /**
     * constructor
     * @param name title
     * @param url url
     * @param image imageurl
     * @param content description
     */
    public Article (String name, String url, String image, String content){
        articleTitle = name;
        this.url = url;
        this.image = image;
        this.content = content;
    }


    /**
     * image getter
     * @return imageurl
     */
    public String getImage() {
        return image;
    }



    /**
     * getter for content
     * @return String containing content
     */
    public String getContent() {
        return content;
    }

    /**
     * setter for content
     * @param content updated content
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * getter for Title
     * @return the articles title
     */
    public String getArticleTitle() {
        return articleTitle;
    }

    /**
     * to string for arrayadapter
     * @return title
     */
    @Override
    public String toString() {
        return articleTitle;
    }
}