package com.swapps.jsonviewer.comparator;

import com.swapps.jsonviewer.Item;

import java.util.Comparator;

/**
 * 日付の大きい順に並べ替え
 */
public class DateComparator implements Comparator {
    public int compare(Object object1, Object object2) {
        Item item1 = (Item) object1;
        Item item2 = (Item) object2;

        if(item1.getLastModified().before(item2.getLastModified())){
            return 1;
        }else if(item2.getLastModified().before(item1.getLastModified())){
            return -1;
        }else{
            return 0;
        }
    }
}
