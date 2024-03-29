import java.util.Collections;
import java.util.List;

public class ResultAnalyzer {
  private double mean;
  private double median;
  private int throughput;
  private int maxResponse;
  private int nintyninthPercentile;

  public ResultAnalyzer(List<Integer> responseTimes, float wallTime, long successfulPosts) {
    this.mean = calculateMean(responseTimes);
    this.median = calculateMedian(responseTimes);
    this.throughput =  (int)(successfulPosts / wallTime);
    this.maxResponse = responseTimes.get(responseTimes.size() - 1);
    this.nintyninthPercentile = responseTimes.get((int) Math.ceil(responseTimes.size() * 0.99) - 1);
  }

  private double calculateMean(List<Integer> responseTimes) {
    double sum = 0;
    for (Integer response : responseTimes) {
      sum += response;
    }
    return sum / responseTimes.size();
  }

  private double calculateMedian(List<Integer> responseTimes) {
    Collections.sort(responseTimes);
    double median;
    int numElements = responseTimes.size();
    if (numElements % 2 == 0) {
      int sumOfMiddleElements =
        responseTimes.get(numElements / 2) + responseTimes.get(numElements / 2 - 1);
      median = ((double) sumOfMiddleElements) / 2;
    } else {
      median = (double) responseTimes.get(numElements / 2);
    }
    return median;
  }

  public double getMean() {
    return mean;
  }

  public double getMedian() {
    return median;
  }

  public int getThroughput() {
    return throughput;
  }

  public Integer getMaxResponse() {
    return maxResponse;
  }

  public Integer getNintyninthPercentile() {
    return nintyninthPercentile;
  }
}

