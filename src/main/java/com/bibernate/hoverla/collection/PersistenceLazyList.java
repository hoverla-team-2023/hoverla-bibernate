package com.bibernate.hoverla.collection;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import com.bibernate.hoverla.exceptions.LazyLoadingException;
import com.bibernate.hoverla.session.SessionImplementor;
import com.bibernate.hoverla.session.cache.CollectionKey;

import lombok.extern.slf4j.Slf4j;

/**
 * Represents a lazy-loading list for persistence purposes.
 *
 * @param <T> the type of elements in this list
 */
@Slf4j
public class PersistenceLazyList<T> implements List<T> {

  private final CollectionKey<?> collectionKey;

  private SessionImplementor session;

  private List<T> nestedList;

  public PersistenceLazyList(CollectionKey<?> collectionKey, SessionImplementor session) {
    this.collectionKey = collectionKey;
    this.session = session;
  }

  public void unlinkSession() {
    this.session = null;
  }

  public List<T> getOrLoad() {
    if (nestedList == null) {
      log.trace("Lazy loading list with key: {}", collectionKey);

      if (session == null) {
        throw new LazyLoadingException("Failed to load list with key: %s session is null.".formatted(collectionKey));
      }
      nestedList = session.getEntityDaoService().loadCollection(collectionKey);
    }

    return nestedList;
  }

  @Override
  public int size() {
    return getOrLoad().size();
  }

  @Override
  public boolean isEmpty() {
    return getOrLoad().isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return getOrLoad().contains(o);
  }

  @Override
  public Iterator<T> iterator() {
    return getOrLoad().iterator();
  }

  @Override
  public Object[] toArray() {
    return getOrLoad().toArray();
  }

  @Override
  public <T1> T1[] toArray(T1[] a) {
    return getOrLoad().toArray(a);
  }

  @Override
  public boolean add(T t) {
    return getOrLoad().add(t);
  }

  @Override
  public boolean remove(Object o) {
    return getOrLoad().remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return getOrLoad().containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    return getOrLoad().addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends T> c) {
    return getOrLoad().addAll(index, c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return getOrLoad().removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return getOrLoad().retainAll(c);
  }

  @Override
  public void replaceAll(UnaryOperator<T> operator) {
    getOrLoad().replaceAll(operator);
  }

  @Override
  public void sort(Comparator<? super T> c) {
    getOrLoad().sort(c);
  }

  @Override
  public void clear() {
    getOrLoad().clear();
  }

  @Override
  public boolean equals(Object o) {
    return getOrLoad().equals(o);
  }

  @Override
  public int hashCode() {
    return getOrLoad().hashCode();
  }

  @Override
  public T get(int index) {
    return getOrLoad().get(index);
  }

  @Override
  public T set(int index, T element) {
    return getOrLoad().set(index, element);
  }

  @Override
  public void add(int index, T element) {
    getOrLoad().add(index, element);
  }

  @Override
  public T remove(int index) {
    return getOrLoad().remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return getOrLoad().indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return getOrLoad().lastIndexOf(o);
  }

  @Override
  public ListIterator<T> listIterator() {
    return getOrLoad().listIterator();
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    return getOrLoad().listIterator(index);
  }

  @Override
  public List<T> subList(int fromIndex, int toIndex) {
    return getOrLoad().subList(fromIndex, toIndex);
  }

  @Override
  public Spliterator<T> spliterator() {
    return getOrLoad().spliterator();
  }

  @Override
  public <T1> T1[] toArray(IntFunction<T1[]> generator) {
    return getOrLoad().toArray(generator);
  }

  @Override
  public boolean removeIf(Predicate<? super T> filter) {
    return getOrLoad().removeIf(filter);
  }

  @Override
  public Stream<T> stream() {
    return getOrLoad().stream();
  }

  @Override
  public Stream<T> parallelStream() {
    return getOrLoad().parallelStream();
  }

  @Override
  public void forEach(Consumer<? super T> action) {
    getOrLoad().forEach(action);
  }

}