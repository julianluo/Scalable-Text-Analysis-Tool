package Model;

public class TextLine {
  private String message;

  public void setMessage(String message) {
    this.message = message;
  }

  public TextLine(String message) {
    this.message = message;
  }

  public TextLine() {
    this.message = "";
  }

  public String getText() {
    return message;
  }

  @Override
  public String toString() {
    return message;
  }
}
