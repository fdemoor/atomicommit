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

  public T getSecond() {
    return t;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<");
    sb.append(v.toString());
    sb.append(",");
    sb.append(t.toString());
    sb.append(">");
    return sb.toString();
  }

}
