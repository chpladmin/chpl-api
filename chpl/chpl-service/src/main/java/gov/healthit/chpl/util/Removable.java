package gov.healthit.chpl.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Removable<T> {
    private Boolean removed;
    private T item;

    public Removable(T item, Boolean removed) {
        this.item = item;
        this.removed = removed;
    }
}
