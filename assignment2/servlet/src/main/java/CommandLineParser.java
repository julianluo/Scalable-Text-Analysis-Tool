public class CommandLineParser {
  private final int COMMAND_LINE_LENGTH = 1;
  private final int MAX_THREADS = 1000;
  private int numThreads;

  public CommandLineParser() {
    this.numThreads = 0;
  }

  public Integer getNumThreads() {
    return numThreads;
  }

  public boolean validate(String[] args) {
    if (args.length != COMMAND_LINE_LENGTH) {
      System.out.println("No argument is detected");
      return false;
    }
    try {
      this.numThreads = Integer.parseInt(args[0]);
      if (this.numThreads > MAX_THREADS) {
        System.out.println("Number of threads is too high");
        return false;
      }
      return true;
    } catch (Exception ex) {
      System.out.println("Enter number of threads.");
      return false;
    }
  }
}

