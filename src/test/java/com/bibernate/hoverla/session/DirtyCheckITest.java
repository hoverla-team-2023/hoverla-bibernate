package com.bibernate.hoverla.session;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.RegisterExtension;

import com.bibernate.hoverla.annotations.Column;
import com.bibernate.hoverla.annotations.Entity;
import com.bibernate.hoverla.annotations.Id;
import com.bibernate.hoverla.annotations.IdentityGeneratedValue;
import com.bibernate.hoverla.annotations.JdbcType;
import com.bibernate.hoverla.annotations.ManyToOne;
import com.bibernate.hoverla.annotations.Table;
import com.bibernate.hoverla.jdbc.PostgresSqlTestExtension;
import com.bibernate.hoverla.jdbc.types.PostgreSqlJdbcEnumType;
import com.bibernate.hoverla.jdbc.types.provider.JdbcTypeProviderImpl;
import com.bibernate.hoverla.metamodel.Metamodel;
import com.bibernate.hoverla.metamodel.scan.MetamodelScanner;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.ToString;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DirtyCheckITest {

  @RegisterExtension
  static PostgresSqlTestExtension DB = new PostgresSqlTestExtension("session-dirty-check/init.sql", "session-dirty-check/clear.sql");

  @Test
  @Order(10)
  @SneakyThrows
  void entityUpdatedOutOfSession_doNothing() {
    final long primaryKey = 3L;

    final String oldTitle = "mude";
    final String oldArtist = "cliffe";
    final Genre oldGenre = Genre.LOFI;

    final String newTitle = "Ghost";
    final String newArtist = "Roderick Porter";
    final Genre newGenre = Genre.RAP;

    var record = getSessionFactory().fromTransaction(session -> {
      var musicRecord = session.find(MusicRecord.class, primaryKey);
      assertNotNull(musicRecord);

      // asert the entity has old field values
      assertEquals(oldTitle, musicRecord.title);
      assertEquals(oldArtist, musicRecord.artist);
      assertEquals(oldGenre, musicRecord.genre);

      return musicRecord;
    });

    // update the entity outside session
    record.setTitle(newTitle);
    record.setArtist(newArtist);
    record.setGenre(newGenre);

    // verify the entity was not updated in the database
    try (var connection = DB.getDataSource().getConnection()) {
      MusicRecord selectedRecord = selectMusicRecord(connection, primaryKey);
      assertNotNull(selectedRecord);
      assertEquals(oldTitle, selectedRecord.title);
      assertEquals(oldArtist, selectedRecord.artist);
      assertEquals(oldGenre, selectedRecord.genre);
    }
  }

  @Test
  @Order(20)
  @SneakyThrows
  void entityUpdatedTwiceInSession_updateDbOnce() {
    final long primaryKey = 2L;
    final String newTitle = "Lover Boy";
    final String newArtist = "Phum Viphurit";
    final Genre newGenre = Genre.POP;

    getSessionFactory().inTransaction(session -> {
      var musicRecord = session.find(MusicRecord.class, primaryKey);
      assertNotNull(musicRecord);

      // update the entity
      musicRecord.setTitle("nedTitle");
      musicRecord.setArtist("newArtist");
      musicRecord.setGenre(Genre.LOFI);

      // update the entity second time
      musicRecord.setTitle(newTitle);
      musicRecord.setArtist(newArtist);
      musicRecord.setGenre(newGenre);
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
  @Order(30)
  @SneakyThrows
  void entitiesUpdatedInSession_updateRecordsInDb() {
    final long firstPrimaryKey = 1L;
    final long secondPrimaryKey = 2L;

    getSessionFactory().inTransaction(session -> {
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
      first.setTitle("No Blueberries");

      second.setTitle("Like you do");
      second.setArtist("Joji");
      second.setGenre(Genre.POP);
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
  @Order(40)
  @SneakyThrows
  void entityUpdatedInTwoSessions_updateDbTwice() {
    final long primaryKey = 3L;

    final String oldTitle = "mude";
    final String oldArtist = "cliffe";
    final Genre oldGenre = Genre.LOFI;

    final String newTitle = "Ghost";
    final String newArtist = "Roderick Porter";
    final Genre newGenre = Genre.RAP;

    getSessionFactory().inTransaction(session -> {
      var musicRecord = session.find(MusicRecord.class, primaryKey);
      assertNotNull(musicRecord);

      // asert the entity has old field values
      assertEquals(oldTitle, musicRecord.title);
      assertEquals(oldArtist, musicRecord.artist);
      assertEquals(oldGenre, musicRecord.genre);

      // update the entity
      musicRecord.setTitle(newTitle);
      musicRecord.setArtist(newArtist);
      musicRecord.setGenre(newGenre);
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

    getSessionFactory().inTransaction(session -> {
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
      musicRecord.setTitle(oldTitle);
      musicRecord.setArtist(oldArtist);
      musicRecord.setGenre(oldGenre);
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
  @Order(50)
  @SneakyThrows
  void entityUpdatedInSession_updateDb() {
    final long primaryKey = 3L;
    final String newTitle = "Ghost";
    final String newArtist = "Roderick Porter";
    final Genre newGenre = Genre.RAP;

    getSessionFactory().inTransaction(session -> {
      var musicRecord = session.find(MusicRecord.class, primaryKey);
      assertNotNull(musicRecord);

      // update the entity
      musicRecord.setTitle(newTitle);
      musicRecord.setArtist(newArtist);
      musicRecord.setGenre(newGenre);
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
  @Order(60)
  @SneakyThrows
  void entityWithRelationsUpdated_updateDb() {
    final long primaryKey = 2L;
    final int newPrice = 2000;
    final String newComment = "New comment";

    getSessionFactory().inTransaction(session -> {
      var comment = session.find(ItemComment.class, primaryKey);
      assertNotNull(comment);

      // update the entity
      comment.setComment(newComment);
      StoreItem storeItem = comment.getStoreItem();
      storeItem.setPrice(newPrice);

      session.flush();
    });

    // verify the entity was updated in the database
    try (var connection = DB.getDataSource().getConnection()) {
      ItemComment selectedRecord = selectItemComment(connection, primaryKey);
      assertNotNull(selectedRecord);
      assertEquals(newComment, selectedRecord.getComment());
      assertEquals(newPrice, selectedRecord.getStoreItem().getPrice());
    }
  }

  @Test
  @Order(70)
  @SneakyThrows
  void proxyEntityWithRelationsUpdated_updateDb() {
    final long primaryKey = 2L;
    final int newPrice = 2000;
    final String newComment = "New comment";

    getSessionFactory().inTransaction(session -> {
      var comment = session.getReference(ItemComment.class, primaryKey);
      assertNotNull(comment);

      // update the entity
      comment.setComment(newComment);
      StoreItem storeItem = comment.getStoreItem();
      storeItem.setPrice(newPrice);

      session.flush();
    });

    // verify the entity was updated in the database
    try (var connection = DB.getDataSource().getConnection()) {
      ItemComment selectedRecord = selectItemComment(connection, primaryKey);
      assertNotNull(selectedRecord);
      assertEquals(newComment, selectedRecord.getComment());
      assertEquals(newPrice, selectedRecord.getStoreItem().getPrice());
    }
  }

  @Test
  @Order(80)
  @SneakyThrows
  void entityWithoutRelationFieldUpdatedFromNull_updateDb() {
    final long primaryKey = 1L;
    final int newPrice = 100;

    getSessionFactory().inTransaction(session -> {
      var item = session.find(StoreItem.class, primaryKey);
      assertNotNull(item);

      // update the entity
      item.setPrice(newPrice);
      session.flush();
    });

    // verify the entity was updated in the database
    try (var connection = DB.getDataSource().getConnection()) {
      StoreItem selectedRecord = selectStoreItem(connection, primaryKey);
      assertNotNull(selectedRecord);
      assertEquals(newPrice, selectedRecord.getPrice());
    }
  }

  @Test
  @Order(100)
  @SneakyThrows
  void entityWithRelationFieldUpdatedFromNull_updateDb() {
    final long itemPrimaryKey = 3L;
    final long commentPrimaryKey = 5L;

    getSessionFactory().inTransaction(session -> {
      var storeItem = session.getReference(StoreItem.class, itemPrimaryKey);

      var comment = session.getReference(ItemComment.class, commentPrimaryKey);
      assertNotNull(comment);
      assertNull(comment.getStoreItem());

      // update the entity
      comment.setStoreItem(storeItem);
      session.flush();
    });

    // verify the entity was updated in the database
    try (var connection = DB.getDataSource().getConnection()) {
      ItemComment selectedComment = selectItemComment(connection, commentPrimaryKey);
      assertNotNull(selectedComment);
      assertEquals(itemPrimaryKey, selectedComment.getStoreItem().getId());
    }
  }

  @Test
  @Order(110)
  @SneakyThrows
  void entityWithRelationFieldUpdatedToNull_updateDb() {
    final long primaryKey = 3L;
    final String newComment = "New comment";

    getSessionFactory().inTransaction(session -> {
      var comment = session.getReference(ItemComment.class, primaryKey);
      assertNotNull(comment);

      // update the entity
      comment.setComment(newComment);
      comment.setStoreItem(null);

      session.flush();
    });

    // verify the entity was updated in the database
    try (var connection = DB.getDataSource().getConnection()) {
      ItemComment selectedRecord = selectItemComment(connection, primaryKey);
      assertNotNull(selectedRecord);
      assertEquals(newComment, selectedRecord.getComment());
      assertNull(selectedRecord.getStoreItem());
    }
  }

  private SessionFactoryImpl getSessionFactory() {
    MetamodelScanner metamodelScanner = new MetamodelScanner(new JdbcTypeProviderImpl());
    Metamodel metamodel = metamodelScanner.scanEntities(MusicRecord.class, StoreItem.class, ItemComment.class);
    return new SessionFactoryImpl(DB.getDataSource(), metamodel);
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

  private ItemComment selectItemComment(Connection connection, long primaryKey) throws SQLException {
    var statement = connection.prepareStatement("SELECT * FROM item_comment WHERE id=?;");
    statement.setLong(1, primaryKey);

    var resultSet = statement.executeQuery();

    if (resultSet.next()) {
      long id = resultSet.getLong("id");
      String comment = resultSet.getString("comment");
      var storeItem = selectStoreItem(connection, resultSet.getLong("item_id"));

      return new ItemComment(id, comment, storeItem);
    }
    return null;
  }

  private StoreItem selectStoreItem(Connection connection, long primaryKey) throws SQLException {
    var statement = connection.prepareStatement("SELECT * FROM store_item WHERE id=?;");
    statement.setLong(1, primaryKey);

    var resultSet = statement.executeQuery();

    if (resultSet.next()) {
      long id = resultSet.getLong("id");
      String name = resultSet.getString("name");
      int price = resultSet.getInt("price");

      return new StoreItem(id, name, price);
    }
    return null;
  }

  @Entity
  @AllArgsConstructor
  @NoArgsConstructor
  @ToString
  @Setter
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

  @Entity
  @AllArgsConstructor
  @NoArgsConstructor
  @ToString
  @Setter
  @Getter
  @Table("store_item")
  public static class StoreItem {

    @Id
    @IdentityGeneratedValue
    private Long id;
    private String name;
    // todo: does not work for primitive int. need to fix it
    private Integer price;

  }

  @Entity
  @AllArgsConstructor
  @NoArgsConstructor
  @ToString
  @Setter
  @Getter
  public static class ItemComment {

    @Id
    @IdentityGeneratedValue
    private Long id;
    private String comment;
    @ManyToOne
    @Column(name = "item_id")
    private StoreItem storeItem;

  }

}
