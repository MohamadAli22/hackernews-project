/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package salesfloor.storiesassignment;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author mohamadAli
 */
public class UtilsTest {
    
    public UtilsTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of SortHashtable method, of class Utils.
     */
    @Test
    public void testSortHashtable() {
        System.out.println("SortHashtable");
        Hashtable<String, Integer> input = new Hashtable<>();
        input.put("commenter1", 500);
        input.put("commenter2", 10);
        input.put("commenter3", 50);
        input.put("commenter4", 1);
        
        Utils instance = new Utils();
        ArrayList<Map.Entry<String, Integer>> result = instance.SortHashtable(input);
        assertEquals(500, result.get(0).getValue());
        assertEquals(50, result.get(1).getValue());
        assertEquals(10, result.get(2).getValue());
        assertEquals(1, result.get(3).getValue());
    }
    
}
