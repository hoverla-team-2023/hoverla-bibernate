package com.bibernate.hoverla.metamodel;

import com.bibernate.hoverla.exceptions.InvalidEntityDeclarationException;

/**
 * Validator for post-processing the metamodel to ensure correct OneToMany and ManyToOne mappings.
 */
public class MetamodelPostValidatorImpl implements MetamodelValidator {

  /**
   * Validates OneToMany and ManyToOne mappings in the provided metamodel.
   *
   * @param metamodel The metamodel containing entity mappings to validate.
   *
   * @throws InvalidEntityDeclarationException if any mapping is invalid.
   */
  public void validate(Metamodel metamodel) {
    for (EntityMapping entityMapping : metamodel.getEntityMappingMap().values()) {
      for (FieldMapping<?> fieldMapping : entityMapping.getFieldNameMappingMap().values()) {
        validateFieldMapping(metamodel, entityMapping, fieldMapping);
      }
    }
  }

  /**
   * Validates a field mapping.
   *
   * @param metamodel     The metamodel containing entity mappings.
   * @param entityMapping The entity mapping associated with the field.
   * @param fieldMapping  The field mapping to validate.
   *
   * @throws InvalidEntityDeclarationException if the mapping is invalid.
   */
  private void validateFieldMapping(Metamodel metamodel, EntityMapping entityMapping, FieldMapping<?> fieldMapping) {
    if (fieldMapping.isOneToMany()) {
      validateOneToMany(metamodel, entityMapping, fieldMapping);
    }
    if (fieldMapping.isManyToOne()) {
      validateManyToOne(metamodel, entityMapping, fieldMapping);
    }
  }

  /**
   * Validates a ManyToOne mapping.
   *
   * @param metamodel     The metamodel containing entity mappings.
   * @param entityMapping The entity mapping associated with the field.
   * @param fieldMapping  The field mapping to validate.
   *
   * @throws InvalidEntityDeclarationException if the mapping is invalid.
   */
  private void validateManyToOne(Metamodel metamodel, EntityMapping entityMapping, FieldMapping<?> fieldMapping) {
    Class<?> fieldType = fieldMapping.getFieldType();
    if (!metamodel.getEntityMappingMap().containsKey(fieldType)) {
      throw new InvalidEntityDeclarationException(
        "Invalid ManyToOne mapping for field: %s, fieldType: %s is not registered as entity"
          .formatted(fieldMapping.getFieldName(), fieldType.getName()));
    }
  }

  /**
   * Validates a OneToMany mapping.
   *
   * @param metamodel     The metamodel containing entity mappings.
   * @param entityMapping The entity mapping associated with the field.
   * @param fieldMapping  The field mapping to validate.
   *
   * @throws InvalidEntityDeclarationException if the mapping is invalid.
   */
  private void validateOneToMany(Metamodel metamodel, EntityMapping entityMapping, FieldMapping<?> fieldMapping) {
    OneToManyMapping oneToManyMapping = fieldMapping.getOneToManyMapping();
    String mappedBy = oneToManyMapping.getMappedBy();
    Class<?> collectionType = oneToManyMapping.getCollectionType();
    EntityMapping collectionTypeMapping = metamodel.getEntityMappingMap().get(collectionType);

    if (collectionTypeMapping == null) {
      throw new InvalidEntityDeclarationException(
        "Collection type not registered as an entity: %s for field: %s, entity: %s".formatted(collectionType, fieldMapping.getFieldName(),
                                                                                              entityMapping.getEntityClass()));
    }

    if (mappedBy == null || isNotValidMappedBy(collectionTypeMapping, mappedBy)) {
      throw new InvalidEntityDeclarationException("Invalid OneToMany mappedBy attribute: %s for field: %s, entity: %s"
                                                    .formatted(mappedBy, fieldMapping.getFieldName(), entityMapping.getEntityClass()));
    }

  }

  /**
   * Checks if the mappedBy attribute is valid.
   *
   * @param entityMapping The entity mapping containing the field.
   * @param mappedBy      The mappedBy attribute value.
   *
   * @return True if mappedBy is not valid, false otherwise.
   */
  private boolean isNotValidMappedBy(EntityMapping entityMapping, String mappedBy) {
    return !entityMapping.getFieldNameMappingMap().containsKey(mappedBy);
  }

}
