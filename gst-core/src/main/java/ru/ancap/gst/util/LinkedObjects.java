package ru.ancap.gst.util;

import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

@Accessors(fluent = true) @Getter @Setter
@ToString @EqualsAndHashCode
public class LinkedObjects<T> implements Iterable<Node<T>> {
    
                               private @Nullable Node<T> graphStart;
    @EqualsAndHashCode.Exclude private @Nullable Node<T> graphEnd;
    
    public void add(T t) {
        Node<T> node = new Node<>(this, t);
        //noinspection IfStatementWithIdenticalBranches because resolution too cognitivly complex
        if (this.graphStart == null) { this.graphStart = node;
                                       this.graphEnd   = node;   }
        else                         { node.prev(this.graphEnd);
                                       //noinspection DataFlowIssue because if graphStart is not null graphEnd should also be not null
                                       this.graphEnd.next(node);
                                       this.graphEnd = node;     }
    }
    
    public LinkedObjects<T> copy() {
        LinkedObjects<T> copy = new LinkedObjects<>();
        for (var node : this) copy.add(node.contents());
        return copy;
    }
    
    @Override
    public @NotNull Iterator<Node<T>> iterator() {
        Node<T> start = this.graphStart();
        return new Iterator<>() {
            
            private Node<T> next = start;
            
            @Override
            public boolean hasNext() {
                return this.next != null;
            }
            
            @Override
            public Node<T> next() {
                Node<T> returned = this.next;
                this.next = this.next.next();
                return returned;
            }
            
        };
    }
    
}