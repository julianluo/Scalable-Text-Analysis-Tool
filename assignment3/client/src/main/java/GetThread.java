import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

public class GetThread implements Runnable {

  private Statistics results;
//  private final String SERVER_PATH = "http://localhost:8080/servlet_war_exploded/servlet/wordcount/";
//  private final String SERVER_PATH = "http://35.169.82.198:8080/servlet_war_a3_7_archive/servlet/wordcount/";
  private final String SERVER_PATH = "http://NLB-2-34640b8ada856395.elb.us-east-1.amazonaws.com:8080/servlet_war_a3_7_archive/servlet/wordcount/";
  private final String WORD = "the";

  public GetThread(Statistics results) {
    this.results = results;
  }

  @Override
  public void run() {
    int successfulGets = 0;
    int failedGets = 0;
    List<String> fileData = new ArrayList<>();

    while (Globals.isRunVariable()) {
      Timestamp startTime;
      Timestamp endTime;
      int responseCode;

      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      for (int i=0;i<10;i++){
        Client client = ClientBuilder.newClient();

        startTime = new Timestamp(System.currentTimeMillis());
        try {
          WebTarget target = client.target(SERVER_PATH + "{id}").resolveTemplate("id", WORD);
          target.request(MediaType.TEXT_PLAIN).get().readEntity(String.class);

          endTime = new Timestamp(System.currentTimeMillis());
          successfulGets++;
        } catch (Exception e) {
          System.err.println("Exception when calling WebTarget");
          endTime = new Timestamp(System.currentTimeMillis());
          failedGets++;
          e.printStackTrace();
        }
        long latency = endTime.getTime() - startTime.getTime();
        String fileLine = startTime.toString() + ",GET," + latency + "," + "200" + "\n";
        fileData.add(fileLine);
      }
    }
    this.results.incrementSuccessfulPost(successfulGets);
    this.results.incrementSuccessfulPost(failedGets);
    this.results.addNewResults(fileData);
  }
}
