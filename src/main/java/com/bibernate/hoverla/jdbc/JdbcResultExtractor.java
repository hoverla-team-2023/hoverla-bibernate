package com.bibernate.hoverla.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface JdbcResultExtractor<T> {
    T extractData(ResultSet resultSet, int index) throws SQLException;
}
