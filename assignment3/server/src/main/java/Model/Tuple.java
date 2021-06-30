package Model;

public class Tuple<String, Long> {
  public final String key;
  public Long count;

  public Tuple(String key, Long count) {
    this.key = key;
    this.count = count;
  }

  public String getKey() {
    return this.key;
  }

  public Long getCount() {
    return this.count;
  }

  @Override
  public java.lang.String toString() {
    return "{" + key +
      ", " + count +
      '}';
  }
}
