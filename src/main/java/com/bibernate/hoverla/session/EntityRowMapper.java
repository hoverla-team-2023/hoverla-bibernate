package com.bibernate.hoverla.session;

import com.bibernate.hoverla.metamodel.EntityMapping;
import com.bibernate.hoverla.utils.EntityUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EntityRowMapper {

  private final SessionImplementor sessionImplementor;

  public <T> T createEntityFromRow(Object[] row, Class<T> entityClass) {
    EntityMapping entityMapping = sessionImplementor.getEntityMapping(entityClass);

    var entity = EntityUtils.newInstanceOf(entityClass);
    int i = 0;
    for (var fieldMapping : entityMapping.getFieldMappingMap().values()) {
      Object columnValue = row[i++];
      if (columnValue == null) {
        EntityUtils.setFieldValue(fieldMapping.getFieldName(), entity, null);
        continue;
      }

      if (fieldMapping.isManyToOne()) {
        Object manyToOneReference = sessionImplementor.getReference(fieldMapping.getFieldType(), columnValue);
        EntityUtils.setFieldValue(fieldMapping.getFieldName(), entity, manyToOneReference);
        continue;
      }

      EntityUtils.setFieldValue(fieldMapping.getFieldName(), entity, columnValue);

    }
    return entity;
  }

}
