package ayaya.core.utils;

import java.util.Comparator;

/**
 * A comparator to compare names. The names in this case are strings.
 */
public class NameComparator implements Comparator<String> {

    @Override
    public int compare(String o1, String o2) {
        return o1.compareToIgnoreCase(o2);
    }

}