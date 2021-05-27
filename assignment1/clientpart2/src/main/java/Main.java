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

    int numThreads = 512;
    String inputFile = "https://raw.githubusercontent.com/gortonator/bsds-6650/master/assignments-2021/bsds-summer-2021-testdata.txt";
//    String inputFile = "https://www.w3.org/TR/PNG/iso_8859-1.txt";

    Producer textReader = new Producer(numThreads, inputFile);
    BlockingQueue<String> listOfLines = textReader.run();
//    System.out.println(listOfLines);

    int numPostRequests = listOfLines.size();

    CountDownLatch latch = new CountDownLatch(numThreads);
    ReqStatistics results = new ReqStatistics();

    try {
      long wallTime = runConsumers(latch, numPostRequests, results, numThreads, listOfLines);

      printStatistics(results, wallTime);
      writeFile(results);
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
    System.out.println("Wall time: " + (wallTime / 1000) + "secs");
    System.out.println("Average response time: " + dataProcessor.getMean() + " ms");
    System.out.println("Median response time: " + dataProcessor.getMedian() + " ms");
    System.out.println("Throughput: " + dataProcessor.getThroughput() + " requests/sec");
    System.out.println("99th percentile: " + dataProcessor.getNintyninthPercentile() + " ms");
    System.out.println("Max response time: " + dataProcessor.getMaxResponse() + " ms");
  }

  private static void writeFile(ReqStatistics results) throws IOException {
    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("results.csv"));
    bufferedWriter.write("start_time,request_type,latency,response_code\n");
    for (String result : results.getFileLines()) {
      bufferedWriter.write(result);
    }
    bufferedWriter.close();
  }

}



