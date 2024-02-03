package com.bibernate.hoverla.query;

public interface ResultMapper<T> {
    T map(Object[] row);

}
