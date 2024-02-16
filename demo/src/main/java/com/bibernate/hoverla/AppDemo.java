package com.bibernate.hoverla;

import java.util.List;

import com.bibernate.hoverla.configuration.Configuration;
import com.bibernate.hoverla.configuration.config.CommonConfig;
import com.bibernate.hoverla.model.Genre;
import com.bibernate.hoverla.model.ItemComment;
import com.bibernate.hoverla.model.MusicRecord;
import com.bibernate.hoverla.model.StoreItem;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppDemo {

  public static void main(String[] args) {
    Configuration configuration = Configuration.builder()
      .packageName(AppDemo.class.getPackageName()) // scan all entities under the current package
      .properties(CommonConfig.of("config.yml"))
      .build();

    var sessionFactory = configuration.getSessionFactory();

    // dirty check mechanism
    sessionFactory.inTransaction(session -> {
      var musicRecord = session.find(MusicRecord.class, 1L);
      log.info("The retrieved record: {}", musicRecord);
      musicRecord.setArtist("The Beatles");
    });

    sessionFactory.inSession(session -> {
      var musicRecord = session.find(MusicRecord.class, 1L);
      log.info("The record artist was updated: {}", musicRecord);
    });

    // selecting a collection
    sessionFactory.inSession(session -> {
      List<MusicRecord> records = session.createQuery("WHERE genre IN :genres", MusicRecord.class)
        .setParameter("genres", List.of(Genre.POP, Genre.RAP))
        .getResult();
      log.info("The retrieved records: {}", records);

      List<StoreItem> items = session.createQuery("WHERE price > :price", StoreItem.class)
        .setParameter("price", 100)
        .getResult();
      log.info("The retrieved items: {}", items);
    });

    // selecting and updating entities with many-to-one relationship
    sessionFactory.inTransaction(session -> {
      ItemComment comment = session.find(ItemComment.class, 1L);
      log.info("The retrieved comment: {}", comment);

      StoreItem item = comment.getStoreItem();
      log.info("The retrieved item: {}", item);

      comment.setComment("This is a new comment");
      item.setPrice(200);

      StoreItem storeItem = session.getReference(StoreItem.class, 2L);
      List<ItemComment> comments = session.createQuery("WHERE comment = :commentValue", ItemComment.class)
        .setParameter("commentValue", "Do not recommend")
        .getResult();
      comments.forEach(c -> c.setStoreItem(storeItem));
    });

    sessionFactory.inSession(session -> {
      ItemComment comment = session.find(ItemComment.class, 1L);
      log.info("The comment was updated in the previous session: {}", comment);

      StoreItem item = comment.getStoreItem();
      log.info("The item was updated. Note that version field was updated, too: {}", item);
    });

    // saving a new entity
    MusicRecord savedRecord = sessionFactory.fromTransaction(session -> {
      var musicRecord = new MusicRecord();
      musicRecord.setArtist("The Beatles");
      musicRecord.setTitle("Yesterday");
      musicRecord.setGenre(Genre.POP);

      session.persist(musicRecord);
      return musicRecord;
    });
    log.info("The saved record: {}. We are out of session now", savedRecord);
    savedRecord.setTitle("apart of me");

    // merging the entity state
    sessionFactory.inTransaction(session -> session.merge(savedRecord));

    // removing the entity
    sessionFactory.inTransaction(session -> {
      ItemComment comment = new ItemComment("New comment", session.find(StoreItem.class, 2L));

      session.persist(comment);
      session.remove(comment);
    });
  }

}
