package com.meituan.robust.change.comparator;

/**
 * Created by hedingxu on 17/8/26.
 */

import com.android.annotations.Nullable;

public interface Comparator<T> {
    boolean areEqual(@Nullable T first, @Nullable T second);
}
