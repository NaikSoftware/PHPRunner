package ua.naiksoftware.phprunner;

import java.util.*;

public class AlphabeticComparator implements Comparator<SortItem> {

    public int compare(SortItem p1, SortItem p2) {
        return p1.getSortField().compareToIgnoreCase(p2.getSortField());
    }
}
