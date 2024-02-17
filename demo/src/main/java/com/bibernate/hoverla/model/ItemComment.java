package com.bibernate.hoverla.model;

import com.bibernate.hoverla.annotations.Column;
import com.bibernate.hoverla.annotations.Entity;
import com.bibernate.hoverla.annotations.Id;
import com.bibernate.hoverla.annotations.IdentityGeneratedValue;
import com.bibernate.hoverla.annotations.ManyToOne;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class ItemComment {

  @Id
  @IdentityGeneratedValue
  private Long id;
  private String comment;

  @ManyToOne
  @Column(name = "item_id")
  private StoreItem storeItem;

  public ItemComment(String comment, StoreItem storeItem) {
    this.comment = comment;
    this.storeItem = storeItem;
  }

}
