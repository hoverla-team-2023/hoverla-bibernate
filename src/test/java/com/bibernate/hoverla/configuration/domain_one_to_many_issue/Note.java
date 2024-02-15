package com.bibernate.hoverla.configuration.domain_one_to_many_issue;

import com.bibernate.hoverla.annotations.Column;
import com.bibernate.hoverla.annotations.Id;
import com.bibernate.hoverla.annotations.ManyToOne;

import lombok.Data;

@Data
public class Note {

  @Id
  private Long id;

  @ManyToOne
  @Column(name = "person_id")
  private Person person;

}
