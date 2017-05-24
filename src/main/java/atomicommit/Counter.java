package atomicommit;

public class Counter {

  private int n;

  Counter() {
    n = 0;
  }

  Counter(int d) {
    n = d;
  }

  void incr() {
    n++;
  }

  void decr() {
    n--;
  }

  void reset() {
    n = 0;
  }

  int get() {
    return n;
  }

}
