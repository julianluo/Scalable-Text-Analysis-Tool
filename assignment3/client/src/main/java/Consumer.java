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
  private final String SERVER_PATH = "http://NLB-2-34640b8ada856395.elb.us-east-1.amazonaws.com:8080/servlet_war_a3_7_archive/servlet/";
//  private final String SERVER_PATH = "http://NLB-2-34640b8ada856395.elb.us-east-1.amazonaws.com:8080/servlet_war_a2_persistent/servlet/";
//  private final String SERVER_PATH = "http://35.169.82.198:8080/servlet_war_a3_7_archive/servlet/";
//  private final String SERVER_PATH = "http://35.169.82.198:8080/servlet_war_a2_persistent/servlet/";

  private CountDownLatch latch;
  private Statistics results;
  private BlockingQueue<String> listOfLines;

  public Consumer(CountDownLatch latch, Statistics results, BlockingQueue<String> listOfLines) {
    this.latch = latch;
    this.results = results;
    this.listOfLines = listOfLines;
  }

  @Override
  public void run() {
    int successfulPosts = 0;
    int failedPosts = 0;
    List<String> fileData = new ArrayList<>();
    TextbodyApi apiInstance = new TextbodyApi();

    ApiClient client = apiInstance.getApiClient();
    client.setBasePath(SERVER_PATH);

    while (true) {
      Timestamp startTime;
      Timestamp endTime;
      int responseCode;

      TextLine body = new TextLine();
      try {
        body.setMessage(listOfLines.take());
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

        String function = "POST";

        startTime = new Timestamp(System.currentTimeMillis());
      try {
        ApiResponse<ResultVal> result = apiInstance.analyzeNewLineWithHttpInfo(body, function);
//        System.out.println(listOfLines.size());
        endTime = new Timestamp(System.currentTimeMillis());
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

        return;
      }
    }
  }
}



