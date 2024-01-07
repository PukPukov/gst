package ru.ancap.gst;

import org.junit.jupiter.api.Test;
import ru.ancap.gst.util.LinkedObjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SuppressWarnings("DataFlowIssue") // it's test, all runtime error will become compile-time
public class LinkedObjectsTest {
    
    @Test
    public void copy() {
        var objects = new LinkedObjects<Integer>();
        objects.add(1);
        objects.add(2);
        objects.add(3);
        
        var copy1 = objects.copy();
        var copy2 = objects.copy();
        
        assertEquals(objects, copy1);
        
        copy1.add(4);
        assertNotEquals(objects, copy1);
        assertEquals(objects, copy2);
    }
    
    @Test
    public void add() {
        var objects = new LinkedObjects<Integer>();
        objects.add(1);
        objects.add(2);
        objects.add(3);
        assertEquals(
            "LinkedObjects(graphStart=Node(contents=1, next=Node(contents=2, next=Node(contents=3, next=null))), graphEnd=Node(contents=3, next=null))", 
            objects.toString()
        );
    }
    
    @Test
    public void remove() {
        var objects = new LinkedObjects<Integer>();
        objects.add(0);
        objects.add(1);
        objects.add(2);
        objects.add(3);
        objects.add(4);
        objects.add(5);
        objects.add(6);
        objects.add(7);
        
        var expected = new LinkedObjects<Integer>();
        expected.add(1);
        expected.add(3);
        expected.add(5);

        var zero = objects.graphStart();
        var two = objects.graphStart().next().next();
        var four = two.next().next();
        var six = objects.graphEnd().prev();
        var seven = objects.graphEnd();
        
        zero.excludeFromGraph();
        two.excludeFromGraph();
        four.excludeFromGraph();
        six.excludeFromGraph();
        seven.excludeFromGraph();
        
        assertEquals(expected, objects);
        
    }
    
}
