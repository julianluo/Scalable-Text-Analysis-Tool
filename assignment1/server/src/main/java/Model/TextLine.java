package Model;

/**
 * The type TextLine.
 */
public class TextLine {
  private String message;

  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Instantiates a new TextLine.
   *
   * @param message the text string
   */
  public TextLine(String message) {
    this.message = message;
  }

  public TextLine() {
    this.message = "";
  }
  /**
   * Gets text.
   *
   * @return the text
   */
  public String getText() {
    return message;
  }

  @Override
  public String toString() {
    return message;
  }
}
