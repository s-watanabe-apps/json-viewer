package com.swapps.jsonviewer.comparator;

import com.swapps.jsonviewer.Item;
import java.util.Comparator;

/**
 * ディレクトリ順に並べ替え
 */
public class DirectoryComparator implements Comparator {
    public int compare(Object object1, Object object2) {
        Item item1 = (Item) object1;
        Item item2 = (Item) object2;

        return item1.getPath().compareTo(item2.getPath());
    }
}
