package Model.Message;

/**
 * The type Abstract message.
 */
public abstract class AbstractMessage {
  private int message;

  /**
   * Instantiates a new Abstract message.
   *
   * @param message the message
   */
  public AbstractMessage(int message) {
    this.message = message;
  }

  /**
   * Gets message.
   *
   * @return the message
   */
  public int getMessage() {
    return message;
  }
}
