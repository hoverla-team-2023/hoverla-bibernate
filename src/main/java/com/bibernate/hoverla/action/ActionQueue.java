package com.bibernate.hoverla.action;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.extern.slf4j.Slf4j;

/**
 * ActionQueue is responsible for maintaining a queue of {@link EntityAction}s.
 * <ul>
 *   <li><em>Priority-Based Execution:</em> Actions are scheduled for execution based on their assigned priorities {@link EntityAction#priority()}. Lower-priority actions are given precedence over higher-priority ones.</li>
 *   <li><em>Order Preservation:</em> Actions sharing the same priority are executed in the order they were added to the queue. This ensures that actions maintain a consistent first-in-first-out (FIFO) order within their priority group.</li>
 * </ul>
 * The purpose:
 * <ul>
 *   <li><em>Write-Behind Cache Optimization:</em> ActionQueue excels in supporting the implementation of a write-behind cache. A write-behind cache is a caching mechanism that optimizes write operations by deferring them until necessary. </li>
 * </ul>
 */
@Slf4j
public class ActionQueue {

  /**
   * The priority queue used to store actions with preserved order.
   */
  private final PriorityQueue<ActionWithPreservedOrder> priorityQueue;

  /**
   * Initializes the ActionQueue with a priority queue sorted by order and insertion order.
   */
  public ActionQueue() {
    this.priorityQueue = new PriorityQueue<>(Comparator.comparingInt((ActionWithPreservedOrder a) -> a.action().priority())
                                               .thenComparingInt(ActionWithPreservedOrder::order));
  }

  /**
   * Atomic counter for generating insertion orders.
   */
  private final AtomicInteger orderCounter = new AtomicInteger(0);

  /**
   * Adds an action to the queue.
   *
   * @param action The action to add.
   */
  public void addAction(EntityAction action) {
    if (action instanceof IdentityInsertAction) {
      log.debug("Scheduled execution for IdentityInsertAction");
      action.execute();
      return;
    }

    int order = orderCounter.getAndIncrement();
    priorityQueue.add(new ActionWithPreservedOrder(order, action));
  }

  /**
   * Executes all actions in the queue in their preserved order:
   * <ul>
   *     <li>Actions are executed based on their priority, with lower priority actions executed first.</li>
   *     <li>If actions have the same priority, their execution order is preserved as they were added to the queue.</li>
   * </ul>
   */
  public void executeActions() {
    int numberOfActions = priorityQueue.size();
    log.info("Execution {} scheduled action(s)", numberOfActions);

    while (!priorityQueue.isEmpty()) {
      ActionWithPreservedOrder actionWithPreservedOrder = priorityQueue.poll();
      EntityAction entityAction = actionWithPreservedOrder.action();

      log.debug("Executing scheduled action with priority: {} and order {}", entityAction.priority(), actionWithPreservedOrder.order);

      entityAction.execute();

      log.debug("Completed execution of scheduled action: {} and order {}", entityAction.priority(), actionWithPreservedOrder.order);
    }
    log.info("Completed execution {} scheduled action(s)", numberOfActions);
  }

  /**
   * Record class to represent an action with preserved order.
   */
  record ActionWithPreservedOrder(int order, EntityAction action) {}

}
