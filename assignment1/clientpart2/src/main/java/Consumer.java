import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.ApiResponse;
import io.swagger.client.model.*;
import io.swagger.client.api.TextbodyApi;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

public class Consumer extends Thread {
//  private final String SERVER_PATH = "http://localhost:8080/servlet_war_exploded/servlet/";
  private final String SERVER_PATH = "http://35.169.82.198:8080/servlet_war/servlet/";

  private CountDownLatch latch;
  private PostStatistics results;
  private int numThreads;
  private int numPostRequests;
  private BlockingQueue<String> listOfLines;
  int counter = 0;

  public Consumer(CountDownLatch latch, int numPostRequests, PostStatistics results, int numThreads, BlockingQueue<String> listOfLines) {
    this.latch = latch;
    this.numPostRequests = numPostRequests;
    this.results = results;
    this.numThreads = numThreads;
    this.listOfLines = listOfLines;
  }

  @Override
  public void run() {
    while (true) {
      Timestamp startTime;
      Timestamp endTime;
      int responseCode;
      int successfulPosts = 0;
      int failedPosts = 0;

      List<String> fileData = new ArrayList<>();
      TextbodyApi apiInstance = new TextbodyApi();
      ApiClient client = apiInstance.getApiClient();
      client.setBasePath(SERVER_PATH);

      for (int i = 0; i < this.numPostRequests; i++) {

        TextLine body = new TextLine(); // TextLine | text string to analyze
        body.setMessage(listOfLines.poll());
//        System.out.println(body);
//        System.out.println(listOfLines);
        String function = "POST"; // String | the operation to perform on the text

        startTime = new Timestamp(System.currentTimeMillis());
        try {
          ApiResponse<ResultVal> result = apiInstance.analyzeNewLineWithHttpInfo(body, function);
          endTime = new Timestamp(System.currentTimeMillis());
          counter++;
//          System.out.println(counter);
          successfulPosts++;
          responseCode = result.getStatusCode();
        } catch (ApiException e) {
          System.err.println("Exception when calling TextbodyApi#analyzeNewLine");
          endTime = new Timestamp(System.currentTimeMillis());
          responseCode = e.getCode();
          failedPosts++;
          e.printStackTrace();
        }
        long latency = endTime.getTime() - startTime.getTime();
        String fileLine = startTime.toString() + ",POST," + latency + "," + responseCode + "\n";
        fileData.add(fileLine);

        if(listOfLines.peek() == "/End of File/") {
          this.results.incrementSuccessfulPost(successfulPosts);
          this.results.incrementFailedPost(failedPosts);
          this.results.addNewResults(fileData);

          try {
            this.latch.countDown();
          } catch (Exception e) {
            e.printStackTrace();
          }

//          System.out.println("peek");
//          System.out.println(listOfLines.peek());
          return;
        }
      }

    }
  }


}
