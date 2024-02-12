package com.bibernate.hoverla.session;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.bibernate.hoverla.annotations.Entity;
import com.bibernate.hoverla.annotations.Id;
import com.bibernate.hoverla.annotations.JdbcType;
import com.bibernate.hoverla.jdbc.PostgresSqlTestExtension;
import com.bibernate.hoverla.jdbc.types.PostgreSqlJdbcEnumType;
import com.bibernate.hoverla.jdbc.types.provider.JdbcTypeProviderImpl;
import com.bibernate.hoverla.metamodel.Metamodel;
import com.bibernate.hoverla.metamodel.scan.MetamodelScanner;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.ToString;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DirtyCheckITest {

  @RegisterExtension
  static PostgresSqlTestExtension DB = new PostgresSqlTestExtension("session-dirty-check/init.sql", "session-dirty-check/clear.sql");

  @Test
  @SneakyThrows
  void selectedRecordsUpdatedInSession_updateEntities() {
    final long firstPrimaryKey = 1L;
    final long secondPrimaryKey = 2L;

    inSession(session -> {
      List<MusicRecord> result = session.createQuery(
          "WHERE id IN :ids",
          MusicRecord.class)
        .setParameter("ids", List.of(firstPrimaryKey, secondPrimaryKey))
        .getResult();

      assertNotNull(result);
      assertEquals(2, result.size());

      MusicRecord first = result.getFirst();
      MusicRecord second = result.get(1);

      // make sure the entities with the needed ids were updated
      assertEquals(firstPrimaryKey, first.id);
      assertEquals(secondPrimaryKey, second.id);

      // update the entities
      first.title = "No Blueberries";

      second.title = "Like you do";
      second.artist = "Joji";
      second.genre = Genre.POP;
      session.flush();
    });

    // verify the entities were updated in the database
    try (var connection = DB.getDataSource().getConnection()) {
      MusicRecord selectedFirstRecord = selectMusicRecord(connection, firstPrimaryKey);
      assertNotNull(selectedFirstRecord);
      assertEquals("No Blueberries", selectedFirstRecord.title);

      MusicRecord selectedSecondRecord = selectMusicRecord(connection, secondPrimaryKey);
      assertNotNull(selectedSecondRecord);
      assertEquals("Like you do", selectedSecondRecord.title);
      assertEquals("Joji", selectedSecondRecord.artist);
      assertEquals(Genre.POP, selectedSecondRecord.genre);
    }
  }

  @Test
  @SneakyThrows
  void foundRecordUpdatedInSession_updateEntity() {
    final long primaryKey = 3L;
    final String newTitle = "Ghost";
    final String newArtist = "Roderick Porter";
    final Genre newGenre = Genre.RAP;

    inSession(session -> {
      var musicRecord = session.find(MusicRecord.class, primaryKey);
      assertNotNull(musicRecord);

      // update the entity
      musicRecord.title = newTitle;
      musicRecord.artist = newArtist;
      musicRecord.genre = newGenre;
      session.flush();
    });

    // verify the entity was updated in the database
    try (var connection = DB.getDataSource().getConnection()) {
      MusicRecord selectedRecord = selectMusicRecord(connection, primaryKey);
      assertNotNull(selectedRecord);
      assertEquals(newTitle, selectedRecord.title);
      assertEquals(newArtist, selectedRecord.artist);
      assertEquals(newGenre, selectedRecord.genre);
    }
  }

  @Test
  @SneakyThrows
  void foundRecordUpdatedTwiceInSession_updateEntityOnce() {
    final long primaryKey = 2L;
    final String newTitle = "Lover Boy";
    final String newArtist = "Phum Viphurit";
    final Genre newGenre = Genre.POP;

    inSession(session -> {
      var musicRecord = session.find(MusicRecord.class, primaryKey);
      assertNotNull(musicRecord);

      // update the entity
      musicRecord.title = "nedTitle";
      musicRecord.artist = "newArtist";
      musicRecord.genre = Genre.LOFI;

      // update the entity second time
      musicRecord.title = newTitle;
      musicRecord.artist = newArtist;
      musicRecord.genre = newGenre;
      session.flush();
    });

    // verify the entity was updated in the database
    try (var connection = DB.getDataSource().getConnection()) {
      MusicRecord selectedRecord = selectMusicRecord(connection, primaryKey);
      assertNotNull(selectedRecord);
      assertEquals(newTitle, selectedRecord.title);
      assertEquals(newArtist, selectedRecord.artist);
      assertEquals(newGenre, selectedRecord.genre);
    }
  }

  @Test
  @SneakyThrows
  void foundRecordUpdatedInTwoSessions_updateEntityTwice() {
    final long primaryKey = 3L;

    final String oldTitle = "mude";
    final String oldArtist = "cliffe";
    final Genre oldGenre = Genre.LOFI;

    final String newTitle = "Ghost";
    final String newArtist = "Roderick Porter";
    final Genre newGenre = Genre.RAP;

    inSession(session -> {
      var musicRecord = session.find(MusicRecord.class, primaryKey);
      assertNotNull(musicRecord);

      // asert the entity has old field values
      assertEquals(oldTitle, musicRecord.title);
      assertEquals(oldArtist, musicRecord.artist);
      assertEquals(oldGenre, musicRecord.genre);

      // update the entity
      musicRecord.title = newTitle;
      musicRecord.artist = newArtist;
      musicRecord.genre = newGenre;
      session.flush();
    });

    // verify the entity was updated in the database
    try (var connection = DB.getDataSource().getConnection()) {
      MusicRecord selectedRecord = selectMusicRecord(connection, primaryKey);
      assertNotNull(selectedRecord);
      assertEquals(newTitle, selectedRecord.title);
      assertEquals(newArtist, selectedRecord.artist);
      assertEquals(newGenre, selectedRecord.genre);
    }

    inSession(session -> {
      var musicRecords = session.createQuery(
          "WHERE id = :id",
          MusicRecord.class)
        .setParameter("id", primaryKey)
        .getResult();
      assertEquals(1, musicRecords.size());
      var musicRecord = musicRecords.getFirst();

      assertNotNull(musicRecords);

      // asert the entity has new field values
      assertEquals(newTitle, musicRecord.title);
      assertEquals(newArtist, musicRecord.artist);
      assertEquals(newGenre, musicRecord.genre);

      // update the entity
      musicRecord.title = oldTitle;
      musicRecord.artist = oldArtist;
      musicRecord.genre = oldGenre;
      session.flush();
    });

    // verify the entity was updated in the database the second time
    try (var connection = DB.getDataSource().getConnection()) {
      MusicRecord selectedRecord = selectMusicRecord(connection, primaryKey);
      assertNotNull(selectedRecord);
      assertEquals(oldTitle, selectedRecord.title);
      assertEquals(oldArtist, selectedRecord.artist);
      assertEquals(oldGenre, selectedRecord.genre);
    }
  }

  @Test
  @SneakyThrows
  void foundRecordUpdatedOutOfSession_doNothing() {
    final long primaryKey = 3L;

    final String oldTitle = "mude";
    final String oldArtist = "cliffe";
    final Genre oldGenre = Genre.LOFI;

    final String newTitle = "Ghost";
    final String newArtist = "Roderick Porter";
    final Genre newGenre = Genre.RAP;

    var record = withSession(session -> {
      var musicRecord = session.find(MusicRecord.class, primaryKey);
      assertNotNull(musicRecord);

      // asert the entity has old field values
      assertEquals(oldTitle, musicRecord.title);
      assertEquals(oldArtist, musicRecord.artist);
      assertEquals(oldGenre, musicRecord.genre);

      return musicRecord;
    });

    // update the entity outside session
    record.title = newTitle;
    record.artist = newArtist;
    record.genre = newGenre;

    // verify the entity was not updated in the database
    try (var connection = DB.getDataSource().getConnection()) {
      MusicRecord selectedRecord = selectMusicRecord(connection, primaryKey);
      assertNotNull(selectedRecord);
      assertEquals(oldTitle, selectedRecord.title);
      assertEquals(oldArtist, selectedRecord.artist);
      assertEquals(oldGenre, selectedRecord.genre);
    }
  }

  private void inSession(Consumer<Session> testInSession) {
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(MusicRecord.class);
    SessionFactoryImpl sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);

    try (Session session = sessionFactory.openSession()) {
      testInSession.accept(session);
    }
  }

  private <T> T withSession(Function<Session, T> testInSession) {
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(MusicRecord.class);
    SessionFactoryImpl sessionFactory = new SessionFactoryImpl(DB.getDataSource(), metamodel);

    try (Session session = sessionFactory.openSession()) {
      return testInSession.apply(session);
    }
  }

  private MusicRecord selectMusicRecord(Connection connection, long primaryKey) throws SQLException {
    var statement = connection.prepareStatement("SELECT * FROM music_record WHERE id=?;");
    statement.setLong(1, primaryKey);

    var resultSet = statement.executeQuery();

    if (resultSet.next()) {
      long id = resultSet.getLong("id");
      String title = resultSet.getString("title");
      String artist = resultSet.getString("artist");
      Genre genre = Genre.valueOf(resultSet.getString("genre"));

      return new MusicRecord(id, title, artist, genre);
    }
    return null;
  }

  @Entity
  @AllArgsConstructor
  @NoArgsConstructor
  @ToString
  @Getter
  public static class MusicRecord {

    @Id
    private Long id;
    private String title;
    private String artist;
    @JdbcType(PostgreSqlJdbcEnumType.class)
    private Genre genre;

  }

  public enum Genre {
    POP,
    RAP,
    LOFI
  }

}
