package Model.Message;

public abstract class AbstractMessage {
  private int message;

  public AbstractMessage(int message) {
    this.message = message;
  }

  public int getMessage() {
    return message;
  }
}
