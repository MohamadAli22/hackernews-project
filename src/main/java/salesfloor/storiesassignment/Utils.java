/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package salesfloor.storiesassignment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author mohamadAli
 */
public class Utils {
    
    public ArrayList<Map.Entry<String, Integer>> SortHashtable(Hashtable input) {
            TreeMap<String, Integer> sorted
                        = new TreeMap(input);
            ArrayList<Map.Entry<String, Integer>> temp = new ArrayList(sorted.entrySet());
            Collections.sort(temp, new Comparator<Map.Entry<String, Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                    return -o1.getValue().compareTo(o2.getValue());
                }
            });
            return temp;
        }
    
}
