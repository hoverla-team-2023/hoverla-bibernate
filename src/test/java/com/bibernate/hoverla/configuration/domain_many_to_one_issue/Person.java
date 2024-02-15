package com.bibernate.hoverla.configuration.domain_many_to_one_issue;

import java.util.List;

import com.bibernate.hoverla.annotations.Id;
import com.bibernate.hoverla.annotations.OneToMany;

import lombok.Data;

@Data
public class Person {

  @Id
  private Long id;
  @OneToMany(mappedBy = "person")
  private List<Note> notes;

}
