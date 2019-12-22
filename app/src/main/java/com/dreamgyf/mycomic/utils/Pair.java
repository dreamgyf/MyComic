package com.dreamgyf.mycomic.utils;

import java.io.Serializable;

import androidx.annotation.Nullable;

public class Pair<F, S> implements Serializable {
    public final @Nullable F first;
    public final @Nullable S second;

    /**
     * Constructor for a Pair.
     *
     * @param first the first object in the Pair
     * @param second the second object in the pair
     */
    public Pair(@Nullable F first, @Nullable S second) {
        this.first = first;
        this.second = second;
    }
}
