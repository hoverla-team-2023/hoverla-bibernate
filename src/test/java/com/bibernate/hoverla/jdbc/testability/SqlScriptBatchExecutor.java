package com.bibernate.hoverla.jdbc.testability;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SqlScriptBatchExecutor {

  private static final Pattern COMMENT_PATTERN = Pattern.compile("--.*|/\\*(.|[\\r\\n])*?\\*/");

  public static void executeBatchedSQL(String scriptFilePath, Connection connection, int batchSize) throws IOException, SQLException {
    log.info("Running sql script: {}",scriptFilePath);
    List<String> sqlStatements = parseSQLScript(scriptFilePath);
    executeSQLBatches(connection, sqlStatements, batchSize);
  }

  private static List<String> parseSQLScript(String scriptFilePath) throws IOException {
    List<String> sqlStatements = new ArrayList<>();
    ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    InputStream resourceAsStream = classLoader.getResourceAsStream(scriptFilePath);

    try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceAsStream))) {
      StringBuilder currentStatement = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        Matcher commentMatcher = COMMENT_PATTERN.matcher(line);
        line = commentMatcher.replaceAll("");
        line = line.trim();

        if (line.isEmpty()) {
          continue;
        }

        currentStatement.append(line).append(" ");

        if (line.endsWith(";")) {
          sqlStatements.add(currentStatement.toString());
          log.info(currentStatement.toString());
          currentStatement.setLength(0);
        }
      }
    } catch (IOException e) {
      throw e;
    }
    return sqlStatements;
  }

  private static void executeSQLBatches(Connection connection, List<String> sqlStatements, int batchSize)
    throws SQLException {
    int count = 0;
    connection.setAutoCommit(false);
    Statement statement = connection.createStatement();

    for (String sql : sqlStatements) {
      statement.addBatch(sql);
      count++;

      if (count % batchSize == 0) {
        log.info("Executing batch");
        statement.executeBatch();
        statement.clearBatch();
      }
    }
    // Execute any remaining statements
    if (count % batchSize != 0) {
      statement.executeBatch();
    }
    connection.commit();
  }

}
