import java.io.*;
import java.util.concurrent.BlockingQueue;

public class Producer extends Thread {

  private String inputFile;
  private int numThreads;
  private BlockingQueue<String> listOfLines;

  public Producer(int numThreads, String inputFile, BlockingQueue<String> listOfLines) {
    this.numThreads = numThreads;
    this.inputFile = inputFile;
    this.listOfLines = listOfLines;
  }

  public void run() {

    FileInputStream fstream = null;
    try {
      fstream = new FileInputStream(inputFile);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }

    DataInputStream in = new DataInputStream(fstream);
    BufferedReader br = new BufferedReader(new InputStreamReader(in));

    try {
      String textline;
      while ((textline = br.readLine()) != null) {
        if (textline.length() > 0) {
          listOfLines.add(textline);
//          System.out.println("producer" + listOfLines);
        }
      }
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    for (int i = 0; i < (this.numThreads); i++) {
      String ending = "/End of File/";
      listOfLines.add(ending);
    }
  }
}
