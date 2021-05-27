import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.ArrayList;
import java.util.List;

public class Main {

  public static void main(String[] args) throws IOException {

//    int numThreads = 256;
    String inputFile = "https://raw.githubusercontent.com/gortonator/bsds-6650/master/assignments-2021/bsds-summer-2021-testdata.txt";
//    String inputFile = "https://www.w3.org/TR/PNG/iso_8859-1.txt";

    ParamValidater paramValidater = new ParamValidater();
    if (!paramValidater.validate(args)){
      return;
    }
    int numThreads = paramValidater.getNumThreads();
    System.out.println("Number of threads: " + numThreads);

    Producer textReader = new Producer(numThreads, inputFile);
    BlockingQueue<String> listOfLines = textReader.run();
//    System.out.println(listOfLines);

    int numPostRequests = listOfLines.size();

    CountDownLatch latch = new CountDownLatch(numThreads);
    ReqStatistics results = new ReqStatistics();

    try {
      long wallTime = runConsumers(latch, numPostRequests, results, numThreads, listOfLines);

      printStatistics(results, wallTime);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  private static long runConsumers(CountDownLatch latch, int numPostRequests, ReqStatistics results,
                                   int numThreads, BlockingQueue<String> listOfLines) throws InterruptedException {
    Timestamp startTime = new Timestamp(System.currentTimeMillis());
    for(int i=0; i < numThreads; i++) {
//      System.out.println("Thread#:" + i);
      Consumer consumer = new Consumer(latch, numPostRequests, results, numThreads, listOfLines);
      consumer.start();
//      System.out.println("Started.");
    }
    latch.await();

    Timestamp endTime = new Timestamp(System.currentTimeMillis());
    return endTime.getTime() - startTime.getTime();
  }

  private static List<Integer> getResponseTimes(List<String> results) {
    List<Integer> responseTimes = new ArrayList<>();
    for (String result : results) {
      String[] parsedResults = result.split(",");
      Integer responseTime = Integer.parseInt(parsedResults[2]);
      responseTimes.add(responseTime);
    }
    return responseTimes;
  }

  private static void printStatistics(ReqStatistics results, long wallTime) {
    ResultAnalyzer dataProcessor = new ResultAnalyzer(getResponseTimes(results.getFileLines()),
      wallTime / 1000, results.getSuccessfulPosts());
    System.out.println("Number of successful posts: " + results.getSuccessfulPosts());
    System.out.println("Number of failed posts: " + results.getFailedPosts());
    System.out.println("Wall time: " + (wallTime / 1000) + "secs, " + (wallTime) + "ms");
    System.out.println("Throughput: " + dataProcessor.getThroughput() + " requests/sec");
  }

}



