package ru.ancap.gst.util;

import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;
@Accessors(fluent = true) @Getter @Setter
@ToString @EqualsAndHashCode
public class Node<T> {
    
    @EqualsAndHashCode.Exclude @ToString.Exclude private final LinkedObjects<T> owner;
    private T contents;
    
    @EqualsAndHashCode.Exclude @ToString.Exclude private @Nullable Node<T> prev;
    private @Nullable Node<T> next;
    
    public Node(LinkedObjects<T> owner, T contents) {
        this.owner = owner;
        this.contents = contents;
    }
    
    @Deprecated
    public Node(T contents, LinkedObjects<T> owner) {
        this(owner, contents);
    }
    
    public void excludeFromGraph() {
        if (this.owner.graphStart() == this) this.owner.graphStart (this.next);
        if (this.owner.graphEnd()   == this) this.owner.graphEnd   (this.prev);
        if (this.next != null) this.next.prev = this.prev;
        if (this.prev != null) this.prev.next = this.next;
    }
    
}