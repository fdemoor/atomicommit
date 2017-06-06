package atomicommit.util.misc;

public class Counter {

  private int n;

  public Counter() {
    n = 0;
  }

  public Counter(int d) {
    n = d;
  }

  /** Increments counter value */
  public void incr() {
    n++;
  }

  /** Decrements counter value */
  public void decr() {
    n--;
  }

  /** Resets counter value to 0 */
  public void reset() {
    n = 0;
  }

  /** Returns counter value
   * @return  counter value
   */
  public int get() {
    return n;
  }

}
