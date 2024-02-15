package com.bibernate.hoverla.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bibernate.hoverla.session.EntityDaoService;
import com.bibernate.hoverla.session.SessionImplementor;
import com.bibernate.hoverla.session.cache.CollectionKey;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersistenceLazyListTest {

  @Mock
  private SessionImplementor session;

  @Mock
  private EntityDaoService entityDaoService;

  @Mock
  private List<String> loadedList;

  private CollectionKey<String> collectionKey;

  private PersistenceLazyList<String> lazyList;

  @BeforeEach
  public void setUp() {
    doReturn(entityDaoService).when(session).getEntityDaoService();
    doReturn(loadedList).when(entityDaoService).loadCollection(any());
    collectionKey = new CollectionKey<>(String.class, "testId", "testCollection");

    lazyList = new PersistenceLazyList<>(collectionKey, session);
  }

  @Test
  public void testSize() {
    assertEquals(0, lazyList.size());
    verify(entityDaoService).loadCollection(collectionKey);
  }

  @Test
  public void testAdd() {
    lazyList.add("Test");
    verify(loadedList).add("Test");
  }

  @Test
  public void testGet() {
    when(loadedList.get(0)).thenReturn("Test");
    assertEquals("Test", lazyList.get(0));
    verify(loadedList).get(0);
  }

  @Test
  public void testClear() {
    lazyList.clear();
    verify(loadedList).clear();
  }

  @Test
  public void testIterator() {
    when(loadedList.iterator()).thenReturn(Arrays.asList("Test").iterator());
    assertTrue(lazyList.iterator().hasNext());
    assertEquals("Test", lazyList.iterator().next());
  }

  @Test
  public void testContains() {
    when(loadedList.contains("Test")).thenReturn(true);
    assertTrue(lazyList.contains("Test"));
  }

  @Test
  public void testRemove() {
    when(loadedList.remove("Test")).thenReturn(true);
    assertTrue(lazyList.remove("Test"));
  }

  @Test
  public void testSet() {
    when(loadedList.set(0, "NewValue")).thenReturn("OldValue");
    assertEquals("OldValue", lazyList.set(0, "NewValue"));
  }

  @Test
  public void testContainsAll() {
    Collection<String> collection = Arrays.asList("Test1", "Test2");
    when(loadedList.containsAll(collection)).thenReturn(true);
    assertTrue(lazyList.containsAll(collection));
  }

  @Test
  public void testAddAll() {
    Collection<String> collection = Arrays.asList("Test1", "Test2");
    when(loadedList.addAll(collection)).thenReturn(true);
    assertTrue(lazyList.addAll(collection));
  }

  @Test
  public void testAddAllAtIndex() {
    Collection<String> collection = Arrays.asList("Test1", "Test2");
    when(loadedList.addAll(1, collection)).thenReturn(true);
    assertTrue(lazyList.addAll(1, collection));
  }

  @Test
  public void testRemoveAll() {
    Collection<String> collection = Arrays.asList("Test1", "Test2");
    when(loadedList.removeAll(collection)).thenReturn(true);
    assertTrue(lazyList.removeAll(collection));
  }

  @Test
  public void testRetainAll() {
    Collection<String> collection = Arrays.asList("Test1", "Test2");
    when(loadedList.retainAll(collection)).thenReturn(true);
    assertTrue(lazyList.retainAll(collection));
  }

  @Test
  public void testReplaceAll() {
    UnaryOperator<String> operator = String::toUpperCase;
    lazyList.replaceAll(operator);
    verify(loadedList).replaceAll(operator);
  }

  @Test
  public void testSort() {
    Comparator<String> comparator = String::compareTo;
    lazyList.sort(comparator);
    verify(loadedList).sort(comparator);
  }

  @Test
  public void testAddAtIndex() {
    lazyList.add(0, "Test");
    verify(loadedList).add(0, "Test");
  }

  @Test
  public void testRemoveAtIndex() {
    when(loadedList.remove(0)).thenReturn("Test");
    assertEquals("Test", lazyList.remove(0));
  }

  @Test
  public void testIndexOf() {
    when(loadedList.indexOf("Test")).thenReturn(1);
    assertEquals(1, lazyList.indexOf("Test"));
  }

  @Test
  public void testLastIndexOf() {
    when(loadedList.lastIndexOf("Test")).thenReturn(1);
    assertEquals(1, lazyList.lastIndexOf("Test"));
  }

  @Test
  public void testListIterator() {
    ListIterator<String> listIterator = mock(ListIterator.class);
    when(loadedList.listIterator()).thenReturn(listIterator);
    assertEquals(listIterator, lazyList.listIterator());
  }

  @Test
  public void testListIteratorWithIndex() {
    ListIterator<String> listIterator = mock(ListIterator.class);
    when(loadedList.listIterator(2)).thenReturn(listIterator);
    assertEquals(listIterator, lazyList.listIterator(2));
  }

  @Test
  public void testSubList() {
    List<String> subList = mock(List.class);
    when(loadedList.subList(1, 3)).thenReturn(subList);
    assertEquals(subList, lazyList.subList(1, 3));
  }

  @Test
  public void testSpliterator() {
    Spliterator<String> spliterator = mock(Spliterator.class);
    when(loadedList.spliterator()).thenReturn(spliterator);
    assertEquals(spliterator, lazyList.spliterator());
  }

  @Test
  public void testStream() {
    Stream<String> stream = mock(Stream.class);
    when(loadedList.stream()).thenReturn(stream);
    assertEquals(stream, lazyList.stream());
  }

  @Test
  public void testParallelStream() {
    Stream<String> parallelStream = mock(Stream.class);
    when(loadedList.parallelStream()).thenReturn(parallelStream);
    assertEquals(parallelStream, lazyList.parallelStream());
  }

  @Test
  public void testForEach() {
    Consumer<String> action = System.out::println;
    lazyList.forEach(action);
    verify(loadedList).forEach(action);
  }

}