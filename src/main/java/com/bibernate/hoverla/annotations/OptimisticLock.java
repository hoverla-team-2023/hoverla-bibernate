package com.bibernate.hoverla.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation that marks a field as an optimistic lock, and it should be used as the version counter.
 * <p>
 * An entity may have only one optimistic lock. Only <code>int</code> and <code>long</code> (and their wrapper types) fields can be marked as optimistic locks.
 * <p>
 * The optimistic lock is a mechanism that allows the database to detect concurrent updates to the same entity.
 * When an entity is updated, the database increments a version counter.
 * When another user tries to update the same entity, the database compares the current version counter with the one in the database.
 * If the versions match, the update is allowed. If they don't match, the update is rejected, indicating a concurrent modification.
 * <p>
 * Example:
 * <pre>
 * &#64;OptimisticLock
 * private int version;
 * </pre>
 * <p>
 * In this example, the "version" field is used as the optimistic lock.
 * When the entity is updated, the version field is incremented.
 * <p>
 * Note that the optimistic lock annotation is not a substitute for a proper concurrency control mechanism, such as pessimistic locking or transaction isolation levels.
 * It is used to prevent some types of concurrency conflicts, but it cannot guarantee that concurrent updates will never occur.
 * <p>
 * In summary, the optimistic lock annotation is a way to implement optimistic concurrency control in entities.
 * It allows the database to detect concurrent updates and reject them, preventing some types of concurrency conflicts.
 * However, it is not a complete solution for concurrency control.
 * <p>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OptimisticLock {
}
