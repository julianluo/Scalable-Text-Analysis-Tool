import java.net.*;
import java.io.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Producer {

  private String inputFile;
  private int numThreads;

  public Producer(int numThreads, String inputFile) {
    this.numThreads = numThreads;
    this.inputFile = inputFile;
  }

  public BlockingQueue<String> run() throws IOException {
    URL fileLink;
    fileLink = new URL(inputFile);
    BufferedReader br = new BufferedReader(new InputStreamReader(fileLink.openStream()));
    BlockingQueue<String> listOfLines = new LinkedBlockingQueue<String>();

    try {
      String textline;
      while ((textline = br.readLine()) != null) {
        if (textline.length() > 0) {
          listOfLines.add(textline);
//          System.out.println(listOfLines);
        }
      }
      br.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    for (int i = 0; i < (this.numThreads); i++) {
      String ending = "/End of File/";
      listOfLines.add(ending);
//      System.out.println(listOfLines);
    }

    return listOfLines;
  }
}

