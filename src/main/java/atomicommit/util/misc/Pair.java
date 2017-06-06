package atomicommit.util.misc;

public class Pair<V,T> {

  private final V v;
  private final T t;

  public Pair(V vv, T tt) {
    v = vv;
    t = tt;
  }

  public V getFirst() {
    return v;
  }

  public T getVote() {
    return t;
  }

}
