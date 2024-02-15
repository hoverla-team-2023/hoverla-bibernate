package com.bibernate.hoverla.configuration.domain_many_to_one_issue;

import com.bibernate.hoverla.annotations.Column;
import com.bibernate.hoverla.annotations.Entity;
import com.bibernate.hoverla.annotations.Id;
import com.bibernate.hoverla.annotations.ManyToOne;

import lombok.Data;

@Data
@Entity
public class Note {

  @Id
  private Long id;

  @ManyToOne
  @Column(name = "person_id")
  private Person person;

}
