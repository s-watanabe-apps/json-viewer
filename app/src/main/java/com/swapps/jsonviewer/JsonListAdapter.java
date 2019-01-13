package com.swapps.jsonviewer;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.List;

public class JsonListAdapter extends ArrayAdapter<Item> {
    private Context context;
    private int resource;
    private List<Item> items;
    private LayoutInflater inflater;
    private SharedPreferences preferences;

    public JsonListAdapter(Context context, int resource, List<Item> items) {
        super(context, resource, items);

        this.context = context;
        this.resource = resource;
        this.items = items;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        preferences = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView != null) {
            view = convertView;
        } else {
            view = inflater.inflate(resource, null);
        }

        int background = preferences.getInt("background", 0);
        int backgroundColor;
        int filenameColor;
        int normalTextColor;
        int sortTextColor;
        if(background == 0) {
            backgroundColor = Color.parseColor(context.getString(R.string.list_color_white_background));
            filenameColor = Color.parseColor(context.getString(R.string.list_color_white_filename));
            normalTextColor = Color.parseColor(context.getString(R.string.list_color_white_normal_text));
            sortTextColor = Color.parseColor(context.getString(R.string.list_color_white_sort_text));
        } else{
            backgroundColor = Color.parseColor(context.getString(R.string.list_color_black_background));
            filenameColor = Color.parseColor(context.getString(R.string.list_color_black_filename));
            normalTextColor = Color.parseColor(context.getString(R.string.list_color_black_normal_text));
            sortTextColor = Color.parseColor(context.getString(R.string.list_color_black_sort_text));
        }

        int sort = preferences.getInt("sort", 0);

        LinearLayout layoutList = view.findViewById(R.id.layout_list);
        layoutList.setBackgroundColor(backgroundColor);

        // リストビューに表示する要素を取得
        Item item = items.get(position);

        // ファイル名
        TextView jsonName = view.findViewById(R.id.json_name);
        jsonName.setText(item.getName());
        jsonName.setTextColor(filenameColor);

        // サイズ
        TextView jsonSize = view.findViewById(R.id.json_size);
        jsonSize.setText(item.getSize() + " byte");
        if(sort == 1) {
            jsonSize.setTextColor(sortTextColor);
        } else{
            jsonSize.setTextColor(normalTextColor);
        }

        // 最終更新
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        TextView jsonLastModified = view.findViewById(R.id.json_last_modified);
        jsonLastModified.setText(format.format(item.getLastModified()));
        if(sort == 2) {
            jsonLastModified.setTextColor(sortTextColor);
        } else{
            jsonLastModified.setTextColor(normalTextColor);
        }

        // パス名
        TextView jsonPath = view.findViewById(R.id.json_path);
        jsonPath.setText(item.getPath());
        if(sort == 0) {
            jsonPath.setTextColor(sortTextColor);
        } else{
            jsonPath.setTextColor(normalTextColor);
        }

        return view;
    }
}
