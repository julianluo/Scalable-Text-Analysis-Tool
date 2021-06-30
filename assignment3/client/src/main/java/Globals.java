public class Globals {
  public static boolean runVariable = true;

  public Globals() {
  }

  public static boolean isRunVariable() {
    return runVariable;
  }

  public static boolean setRunVariable(boolean runVariable) {
    Globals.runVariable = runVariable;
    return runVariable;
  }
}
