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
    System.out.println("Number of threads: " + numThreads);
    String inputFile = paramValidater.getInputFile();

    BlockingQueue<String> listOfLines = new LinkedBlockingQueue<String>();

    CountDownLatch latch = new CountDownLatch(numThreads);
    PostStatistics results = new PostStatistics();

    try {
      long wallTime = runConsumers(latch, results, numThreads, listOfLines, inputFile);

      printStatistics(results, wallTime);
      writeFile(results);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static long runConsumers(CountDownLatch latch, PostStatistics results,
                                   int numThreads, BlockingQueue<String> listOfLines, String inputFile) throws InterruptedException {
    Timestamp startTime = new Timestamp(System.currentTimeMillis());
    for(int i=0; i < numThreads; i++) {
      Consumer consumer = new Consumer(latch, results, listOfLines);
      consumer.start();
    }

    Producer producer = new Producer(numThreads, inputFile, listOfLines);
    producer.start();

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

  private static void printStatistics(PostStatistics results, long wallTime) {
    ResultAnalyzer dataProcessor = new ResultAnalyzer(getResponseTimes(results.getFileLines()),
      wallTime / 1000, results.getSuccessfulPosts());
    System.out.println("Number of successful posts: " + results.getSuccessfulPosts());
    System.out.println("Number of failed posts: " + results.getFailedPosts());
    System.out.println("Wall time: " + (wallTime / 1000) + "secs, " + (wallTime) + "ms");
    System.out.println("Throughput: " + dataProcessor.getThroughput() + " requests/sec");
    System.out.println("Average response time: " + dataProcessor.getMean() + " ms");
    System.out.println("Median response time: " + dataProcessor.getMedian() + " ms");
    System.out.println("99th percentile: " + dataProcessor.getNintyninthPercentile() + " ms");
    System.out.println("Max response time: " + dataProcessor.getMaxResponse() + " ms");
  }

  private static void writeFile(PostStatistics results) throws IOException {
    BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter("results.csv"));
    bufferedWriter.write("start_time,request_type,latency,response_code\n");
    for (String result : results.getFileLines()) {
      bufferedWriter.write(result);
    }
    bufferedWriter.close();
  }
}



