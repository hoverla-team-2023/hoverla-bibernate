package com.bibernate.hoverla.configuration.domain;

import java.util.List;

import com.bibernate.hoverla.annotations.Entity;
import com.bibernate.hoverla.annotations.Id;
import com.bibernate.hoverla.annotations.OneToMany;

import lombok.Data;

@Data
@Entity
public class Person {

  @Id
  private Long id;
  @OneToMany(mappedBy = "person")
  private List<Note> notes;

}
