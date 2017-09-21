package com.meituan.robust.change.comparator;

/**
 * Created by hedingxu on 17/8/26.
 */


public interface Comparator<T> {
    boolean areEqual(T first, T second);
}
