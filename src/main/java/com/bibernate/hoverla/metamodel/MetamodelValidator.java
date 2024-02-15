package com.bibernate.hoverla.metamodel;

/**
 * Interface for validating a metamodel.
 * This interface defines a method to perform validation on a given metamodel.
 */
public interface MetamodelValidator {

  /**
   * Validates the provided metamodel.
   *
   * @param metamodel The metamodel to validate.
   */
  void validate(Metamodel metamodel);

}
