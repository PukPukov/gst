package ru.ancap.gst.util;

import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

@RequiredArgsConstructor
@Accessors(fluent = true) @Getter @Setter
@ToString @EqualsAndHashCode
public class Node<T> {

    private final T contents;
    @EqualsAndHashCode.Exclude @ToString.Exclude private final LinkedObjects<T> owner;

    @EqualsAndHashCode.Exclude @ToString.Exclude private @Nullable Node<T> prev;
    private @Nullable Node<T> next;

    public void excludeFromGraph() {
        if (this.owner.graphStart() == this) this.owner.graphStart (this.next);
        if (this.owner.graphEnd()   == this) this.owner.graphEnd   (this.prev);
        if (this.next != null) this.next.prev = this.prev;
        if (this.prev != null) this.prev.next = this.next;
    }

}