package com.bibernate.hoverla.configuration.domain_one_to_many_mappedby_issue;

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
  @OneToMany(mappedBy = "persona")
  private List<Note> notes;

}
