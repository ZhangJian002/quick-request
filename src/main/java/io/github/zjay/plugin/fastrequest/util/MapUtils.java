package io.github.zjay.plugin.fastrequest.util;

import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class MapUtils {

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map, boolean isDesc) {
        Map<K, V> result = new LinkedHashMap<>();
        Comparator<Map.Entry<K, V>> entryComparator = Map.Entry.comparingByValue();
        if(isDesc){
            entryComparator = entryComparator.reversed();
        }
        map.entrySet().stream().sorted(entryComparator).forEachOrdered(e -> result.put(e.getKey(), e.getValue()));
        return result;
    }

    public static <T> T getFirstNoneNull(Iterator<T> iterator) {
        if (null != iterator) {
            while(iterator.hasNext()){
                return iterator.next();
            }
        }
        return null;
    }
}
