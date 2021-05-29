public class ParamValidater {
  private final int COMMAND_LINE_LENGTH = 2;
  private final int MAX_THREADS = 10000;
  private int numThreads;
  private String inputFile;

  public ParamValidater() {
    this.numThreads = 0;
    this.inputFile = null;
  }

  public String getInputFile() {
    return inputFile;
  }

  public Integer getNumThreads() {
    return numThreads;
  }

  public boolean validate(String[] args) {
    if (args.length != COMMAND_LINE_LENGTH) {
      System.out.println("Not enough argument is detected");
      return false;
    }
    try {
      this.numThreads = Integer.parseInt(args[0]);
      this.inputFile = args[1];
      if (this.numThreads > MAX_THREADS) {
        System.out.println("Number of threads is too high");
        return false;
      }
      return true;
    } catch (Exception ex) {
      System.out.println("Enter number of threads and input file.");
      return false;
    }

  }

}
