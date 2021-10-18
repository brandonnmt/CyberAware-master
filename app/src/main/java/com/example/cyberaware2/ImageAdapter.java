package com.example.cyberaware2;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;


/**
 * custom arraadapter that displays an articles icon, title, and description.
 */
public class ImageAdapter extends ArrayAdapter<Article> {
    private Context context; // current context
    private List<Article> articleList; // list of articles
    private List<Drawable> drawableList; // list of drawables
    private final String TAG = "ImageAdapter";

    /**
     * constructor
     * @param context current context
     * @param articleList article list
     * @param drawableList list of respective images
     */
    public ImageAdapter (Context context, ArrayList<Article> articleList, ArrayList<Drawable> drawableList){
        super(context,0, articleList);
        this.context = context;
        this.articleList = articleList;
        this.drawableList = drawableList;

    }

    /**
     * sets the views imageview and textviews with appropriate content
     * @param position current index in listview
     * @param convertView current view
     * @param parent parent view
     * @return current view updated
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        if(item == null) {
            item = LayoutInflater.from(context).inflate(R.layout.display_view, parent, false);
        }
        if(position < articleList.size()) { // checks for index out of bounds
            Article currentArticle = articleList.get(position);
            TextView name = (TextView) item.findViewById(R.id.name);
            name.setText(currentArticle.getArticleTitle());
            TextView description = item.findViewById(R.id.description);
            description.setText(currentArticle.getContent());
        }

        if(position < drawableList.size()) { // checks for index out of bounds
            ImageView icon = item.findViewById(R.id.icon);
            icon.setImageDrawable(drawableList.get(position));

        }
        return item;
    }




}



