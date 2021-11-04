package com.example.cyberaware2;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import android.content.Context;

/**
 * class for user objects
 * Created by Vhl2 on 6/19/2019.
 */

public class User implements Serializable {
    private String userName;
    private ArrayList<String> userFilter; // personal filter
    private ArrayList<Article> favoriteList; // list of favorites
    private ArrayList<String> searchHistory; // search history
    private ArrayList<String> whitelist; // wifi whitelist
    private int level;
    private int levelCount;
    private final String TAG = "User"; // log tag
    private boolean usage;
    private boolean location;
    private boolean notifications;
    private String state = "";
    private int numApps;

    public int getNumApps() {
        return numApps;
    }

    public void setNumApps(int numApps) {
        this.numApps = numApps;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    /**
     * getter for favorites
     * @return favoriteList
     */
    public ArrayList<Article> getFavoriteList() {
        return favoriteList;
    }

    public ArrayList<String> getWhitelist() {
        return whitelist;
    }

    /**
     * getter for level
     * @return level
     */
    public int getLevel(){
        return level;
    }

    /**
     * adds a single item to search history
     * @param keyword keyword that user just used
     */
   public void addSearchHistory(String keyword){
        if (!searchHistory.contains(keyword)) {
            Log.e(TAG, "new keyword: " + keyword);
            searchHistory.add(keyword);
        } else {
            Log.e(TAG, "already in list");
        }
    }


    /**
     * adds articles to favorites
     * @param article new favorite article
     */
    public boolean addFavorite(Article article) {
        int x=0;
        for (int i = 0; i < favoriteList.size(); i++) {
            if (favoriteList.get(i).getArticleTitle().equals(article.getArticleTitle())) {
                x = 1;
            }
        }
        if(x==0)
            favoriteList.add(article);
        else {
            Log.e(TAG, "already in list");
             return false;
        }
        return true;
    }

    /**
     * increments level count;
     * @return true if the user leveled up
     */
    public boolean incrementLevelCount(){
        levelCount++;
        if(levelCount == 20){
            level++;
            return true;
        } else if (levelCount == 40) {
            level++;
            return true;
        } else if (levelCount == 60) {
            level++;
            return true;
        }
        return false;

    }

    /**
     * resets users level
     * @param level new level
     */
    public void resetLevel(int level){
        this.level = level;
        levelCount = (level - 1) * 20;
    }


    /**
     * constructor
     * @param name new username
     * @param role integer representing skill level
     */
    public User(String name, int role) { // todo fix strings
        userName = name;
        favoriteList = new ArrayList<>();
        userFilter = new ArrayList<>();
        searchHistory = new ArrayList<>();
        whitelist = new ArrayList<>();
        usage = true;
        level = role;
        levelCount = (role - 1) * 20;
        numApps = 0;
    }

    /**
     * getter for usage
     * @return true if usage is on
     */
    public boolean isUsage() {
        return usage;
    }

    /**
     * setter for usage
     * @param usage boolean for if the user wants their search history used
     */
    public void setUsage(boolean usage) {
        this.usage = usage;
    }

    /**
     * getter for location
     * @return true if location is on
     */
    public boolean isLocation() {
        return location;
    }

    /**
     * setter for location
     * @param location boolean for if the user wants their location used
     */
    public void setLocation(boolean location) { this.location = location; }

    /**
     * getter for notifications
     * @return true if notifications is on
     */
    public boolean isNotifications() { return notifications; }

    /**
     * setter for notifications
     * @param notifications boolean for if the user wants notifications used
     */
    public void setNotifications(boolean notifications) { this.notifications = notifications; }

    /**
     * This constructor simply takes in a name.
     * It acts as a backup constructor in case user data is not retrieved.
     * @param name username
     */
    public User(String name) {
        this.userName = name;
        userFilter = new ArrayList<>();
        favoriteList = new ArrayList<>();
        searchHistory = new ArrayList<>();
        whitelist = new ArrayList<>();
        usage = true;
        level = 1;
        levelCount = 0;
        numApps = 0;
    }

    /**
     * updates the user filter
     * @param tempFilter new filter
     */
    public void updateFilter(ArrayList<String> tempFilter){
        userFilter.clear();
        userFilter.addAll(tempFilter);
    }

    public void addToFilter(String location) {
        this.userFilter.add(location);
    }

    public void addToWhitelist(String ssid) {
        this.whitelist.add(ssid);
    }

    /**
     * formats userFilter into a single keyword
     * @return string representing the filter
     */
    public String getStringFilter(){
        String urlFilter = "";
        for (int i = 0; i < userFilter.size(); i++) {
            urlFilter = urlFilter + userFilter.get(i);
            if ((i + 1) < userFilter.size()) { // adds or if not the end of the filter
                urlFilter = urlFilter + " OR ";
            }
        }
        if(urlFilter.equals("")){
            return "cybersecurity"; // return all if filter is blank
        }
        return urlFilter;
    }

    /**
     * formats userFilter into a single keyword
     * @return string representing the filter
     */
    public String getFilterQuery(){
        String urlFilter = "";

        for (int i = 0; i < userFilter.size(); i++) {
            urlFilter = urlFilter + userFilter.get(i);
            if ((i + 1) < userFilter.size() || searchHistory.size() > 0) { // adds or if not the end of the filter
                urlFilter = urlFilter + " OR ";
            }
        }

        for(int i = 0; i < searchHistory.size(); i++) {
            urlFilter = urlFilter + searchHistory.get(i);
            if ((i + 1) < searchHistory.size()) { // adds or if not the end of the filter
                urlFilter = urlFilter + " OR ";
            }
        }

        if(urlFilter.equals("")){
            return "cybersecurity"; // return all if filter is blank
        }
        return urlFilter;
    }

    /**
     * getter for username
     * @return username
     */
    public String getUserName() {
        return userName;
    }

    /**
     * setter for username
     * @param userName new name
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * getter for userFilter
     * @return userFilter
     */
    public ArrayList<String> getUserFilter() {
        return userFilter;
    }

    public void eraseHistory(){
        searchHistory.clear();
    }

    /**
     * toString
     * @return string containing user data
     */
    @Override
    public String toString() {
        String temp = "userName: " + userName + " filter: ";
        for(int x = 0; x < userFilter.size(); x ++){
            temp = temp + userFilter.get(x) + " ";
        }
        return temp;
    }
}
