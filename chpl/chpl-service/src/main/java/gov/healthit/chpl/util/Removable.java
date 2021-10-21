package gov.healthit.chpl.util;

import lombok.Getter;

@Getter
public class Removable<T> {
    private Boolean removed;
    private T item;

    public Removable(T item, Boolean removed) {
        this.item = item;
        this.removed = removed;
    }
}
