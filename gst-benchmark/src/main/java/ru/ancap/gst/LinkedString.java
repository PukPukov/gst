package ru.ancap.gst;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.Nullable;

public class LinkedString {
    
    public @Nullable Node base;
    
    public LinkedString(String base) {
        char[] chars = base.toCharArray();
        if (chars.length != 0) {
            this.base = new Node(new StringBuilder(1).append(chars[0]), null, null);
            Node previous = this.base;
            for (int i = 1; i < chars.length; i++) {
                Node next = new Node(new StringBuilder(1).append(chars[i]), previous, null);
                previous.next = next;
                previous = next;
            }
        }
    }
    
    public String toString() {
        int length = 0;
        LinkedString.Node next0 = this.base;
        while (next0 != null) {
            LinkedString.Node this_ = next0;
            length+=this_.content.length();
            next0 = this_.next;
        }
        
        StringBuilder stringBuilder = new StringBuilder(length);
        LinkedString.Node next1 = this.base;
        while (next1 != null) {
            LinkedString.Node this_ = next1;
            stringBuilder.append(this_.content);
            next1 = this_.next;
        }
        
        return stringBuilder.toString();
    }
    
    @AllArgsConstructor
    public static class Node {
        
        public CharSequence content;
        public @Nullable Node previous;
        public @Nullable Node next;
        
        public @Nullable Node next(int i) {
            @Nullable Node this_ = this;
            for (int j = 0; j <= i; j++) {
                if (this_ == null) return null;
                this_ = this_.next;
            }
            return this_;
        }
        
    }
    
}