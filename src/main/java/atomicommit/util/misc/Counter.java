package atomicommit.util.misc;

public class Counter {

  private int n;

  public Counter() {
    n = 0;
  }

  public Counter(int d) {
    n = d;
  }

  public void incr() {
    n++;
  }

  public void decr() {
    n--;
  }

  public void reset() {
    n = 0;
  }

  public int get() {
    return n;
  }

}
