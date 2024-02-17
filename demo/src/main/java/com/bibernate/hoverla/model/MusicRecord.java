package com.bibernate.hoverla.model;

import com.bibernate.hoverla.annotations.Entity;
import com.bibernate.hoverla.annotations.Id;
import com.bibernate.hoverla.annotations.IdentityGeneratedValue;
import com.bibernate.hoverla.annotations.JdbcType;
import com.bibernate.hoverla.annotations.Table;
import com.bibernate.hoverla.jdbc.types.PostgreSqlJdbcEnumType;

import lombok.Data;

@Entity
@Data
@Table("music_record")
public class MusicRecord {

  @Id
  @IdentityGeneratedValue
  private Long id;
  private String title;
  private String artist;
  @JdbcType(PostgreSqlJdbcEnumType.class)
  private Genre genre;

}
