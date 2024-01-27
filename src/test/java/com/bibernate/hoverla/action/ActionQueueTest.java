package com.bibernate.hoverla.action;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ActionQueueTest {

  private ActionQueue actionQueue;

  @BeforeEach
  void setUp() {
    actionQueue = new ActionQueue();
  }

  @Test
  void whenActionsWithDifferentPrioritiesExecuted_thenActionsExecuteInCorrectOrder() {
    EntityAction action1 = mock(EntityAction.class);
    EntityAction action2 = mock(EntityAction.class);
    EntityAction action3 = mock(EntityAction.class);

    doReturn(10).when(action1).priority();
    doReturn(20).when(action2).priority();
    doReturn(10).when(action3).priority();

    actionQueue.addAction(action1);
    actionQueue.addAction(action2);
    actionQueue.addAction(action3);

    actionQueue.executeActions();

    // Verify that actions were executed based on priority and insertion order
    InOrder inOrder = inOrder(action1, action3, action2);
    inOrder.verify(action1, times(1)).execute(); // Priority 10, executed second
    inOrder.verify(action3, times(1)).execute(); // Priority 10, executed last
    inOrder.verify(action2, times(1)).execute(); // Priority 20, executed first
  }

  @Test
  void whenIdentityInsertActionExecuted_thenActionIsScheduledImmediately() {
    IdentityInsertAction identityInsertAction = mock(IdentityInsertAction.class);
    EntityAction action1 = mock(EntityAction.class);

    actionQueue.addAction(identityInsertAction);
    actionQueue.addAction(action1);

    actionQueue.executeActions();

    // Verify that IdentityInsertAction is executed immediately
    verify(identityInsertAction, times(1)).execute();

    // Verify that the second action is executed afterward
    verify(action1, times(1)).execute();
  }

}


