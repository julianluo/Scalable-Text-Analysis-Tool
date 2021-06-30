import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {

  public static void main(String[] args) {
//    int numThreads = 128;
//    String inputFile = "C:/Users/zhiyu/Desktop/CS6650/sample.txt";
//    String inputFile = "https://raw.githubusercontent.com/gortonator/bsds-6650/master/assignments-2021/bsds-summer-2021-testdata.txt";
//    String inputFile = "https://raw.githubusercontent.com/gortonator/bsds-6650/master/assignments-2021/bsds-summer-2021-testdata-assignment2.txt";

    ParamValidater paramValidater = new ParamValidater();
    if (!paramValidater.validate(args)){
      return;
    }
    int numThreads = paramValidater.getNumThreads();
    String inputFile = paramValidater.getInputFile();

    BlockingQueue<String> listOfLines = new LinkedBlockingQueue<String>();

    CountDownLatch latch = new CountDownLatch(numThreads);
    Statistics resultsPost = new Statistics();
    Statistics resultsGet = new Statistics();

    try {
      WallTime wallTime = runThreads(latch, resultsPost, resultsGet, numThreads, listOfLines, inputFile);

      System.out.println("Number of threads: " + numThreads);
      printStatistics(resultsPost, wallTime.getWallTimePost(), "POST");
      printStatistics(resultsGet, wallTime.getWallTimeGet(), "GET");
//      writeFile(resultsPost);
//      writeFile(resultsGet);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static WallTime runThreads(CountDownLatch latch, Statistics resultsPost, Statistics resultsGet,
                                     int numThreads, BlockingQueue<String> listOfLines, String inputFile) throws InterruptedException {
    Timestamp startTimePost = new Timestamp(System.currentTimeMillis());
    for(int i=0; i < numThreads; i++) {
      Consumer consumer = new Consumer(latch, resultsPost, listOfLines);
      consumer.start();
    }
    Producer producer = new Producer(numThreads, inputFile, listOfLines);
    producer.start();

    List<Thread> threads = new ArrayList<>();
    Timestamp startTimeGet = new Timestamp(System.currentTimeMillis());
    for (int i = 0; i < 1; i++) {
      Runnable runnable = new GetThread(resultsGet);
      Thread thread = new Thread(runnable);
      threads.add(thread);
      thread.start();
    }
    producer.join();
    Globals.setRunVariable(false);

    for (int i = 0; i < threads.size(); i++) {
      threads.get(i).join();
    }
    Timestamp endTimeGet = new Timestamp(System.currentTimeMillis());

    latch.await();

    Timestamp endTimePost = new Timestamp(System.currentTimeMillis());
    return new WallTime(endTimePost.getTime() - startTimePost.getTime(), endTimeGet.getTime() - startTimeGet.getTime());
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

  private static void printStatistics(Statistics results, long wallTime, String function) {
    MetricsProcessor metricsProcessor = new MetricsProcessor(getResponseTimes(results.getFileLines()),
      wallTime / 1000, results.getSuccessfulPosts());
    System.out.println("=================================");
    System.out.println("Number of successful " + function + ": " + results.getSuccessfulPosts());
    System.out.println("Number of failed " + function + ": " + results.getFailedPosts());
    if (function == "POST") {
      System.out.println("Wall time: " + (wallTime / 1000) + "secs, " + (wallTime) + "ms");
      System.out.println("Throughput: " + metricsProcessor.getThroughput() + " requests/sec");
      System.out.println("Average response time: " + metricsProcessor.getMean() + " ms");
      System.out.println("Median response time: " + metricsProcessor.getMedian() + " ms");
      System.out.println("99th percentile: " + metricsProcessor.getNintyninthPercentile() + " ms");
      System.out.println("Max response time: " + metricsProcessor.getMaxResponse() + " ms");
    } else if (function == "GET") {
      System.out.println("Average response time: " + metricsProcessor.getMean() + " ms");
      System.out.println("Median response time: " + metricsProcessor.getMedian() + " ms");
      System.out.println("Max response time: " + metricsProcessor.getMaxResponse() + " ms");
    }
  }

  private static void writeFile(Statistics results) throws IOException {
    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("results.csv"));
    bufferedWriter.write("start_time,request_type,latency,response_code\n");
    for (String result : results.getFileLines()) {
      bufferedWriter.write(result);
    }
    bufferedWriter.close();
  }
}



