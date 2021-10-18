package com.example.cyberaware2;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * custom arrayAdapter for EditFeedActivity
 */
public class CheckAdapter extends ArrayAdapter<String> {
    private Context context;                // current context
    private ArrayList<Boolean> checkedList; // list of values present in users filter
    private String[] keywordList;           // list of keywords that users can choose from


    /**
     * constructor
     * @param context current context
     * @param checkedList list of values present in users filter
     * @param keywordList list of keywords that users can choose from
     */
    public CheckAdapter(Context context, ArrayList<Boolean> checkedList, String[] keywordList) {
        super(context, 0, keywordList);
        this.context = context;
        this.keywordList = keywordList;
        this.checkedList = checkedList;
    }


    /**
     * sets the views checkboxes and textview with appropriate content
     * @param position current index in listview
     * @param convertView current view
     * @param parent parent view
     * @return current view updated
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item = convertView;
        if(item == null) {
            item = LayoutInflater.from(context).inflate(R.layout.checked_view, parent, false);
        }
        ImageView imageView = item.findViewById(R.id.myCheckBox);
        if(checkedList.get(position)){
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.INVISIBLE);
        }
        TextView textView = item.findViewById(R.id.filterNameView);
        textView.setText(keywordList[position]);
        return item;
    }
}
