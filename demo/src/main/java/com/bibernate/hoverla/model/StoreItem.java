package com.bibernate.hoverla.model;

import com.bibernate.hoverla.annotations.Column;
import com.bibernate.hoverla.annotations.Entity;
import com.bibernate.hoverla.annotations.Id;
import com.bibernate.hoverla.annotations.OptimisticLock;
import com.bibernate.hoverla.annotations.SequenceGeneratedValue;
import com.bibernate.hoverla.annotations.Table;

import lombok.Data;

@Data
@Entity
@Table("store_item")
public class StoreItem {

  @Id
  @SequenceGeneratedValue(sequenceName = "store_item_id_seq")
  private Long id;
  @Column(name = "name", updatable = false)
  private String name;
  @Column(name = "price")
  private Integer price;
  @OptimisticLock
  private int version;

}
