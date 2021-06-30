import com.google.gson.Gson;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import DAO.FrequencyDao;
import Model.Tuple;

public class Consumer {

  private static ConnectionFactory factory;
  private static Connection connection;
  private static final String QUEUE_NAME = "queue";
  private static final String USERNAME = System.getenv("RABBIT_USERNAME");
  private static final String PASSWORD = System.getenv("RABBIT_PASSWORD");
  private static final String HOST = System.getenv("RABBIT_HOST");
  private static final boolean isLocal = false;
  private static final boolean DURABLE = false;
  private static final Map<String, AtomicLong> frequency = new ConcurrentHashMap<>();
  private static FrequencyDao frequencyDao;

  public static void main(String[] args) {

    CommandLineParser commandLineParser = new CommandLineParser();
    if (!commandLineParser.validate(args)) {
      return;
    }

    int numThreads = commandLineParser.getNumThreads();
//    System.out.println("Number of threads: " + numThreads);

    factory = new ConnectionFactory();
    if (isLocal) {
      factory.setHost("localhost");
    } else {
      factory.setUsername("admin");
      factory.setPassword("admin");
      factory.setHost("54.204.219.31");
    }

    try {
      connection = factory.newConnection();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }

    System.out.println("Consumer Started.");

    for (int i = 0; i < numThreads; i++) {
      int ThreadIndex = i;
      Runnable runnable = new Runnable() {
        @Override
        public void run() {
          try {
            Channel channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, DURABLE, false, false, null);

            channel.basicQos(1);
//            System.out.println("Thread " + ThreadIndex + " waiting for messages.");

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
              String message = new String(delivery.getBody(), "UTF-8");
//              System.out.println("Callback Thread " + ThreadIndex + " Received " + message);

              Gson gson = new Gson();
              Tuple tuple = gson.fromJson(message, Tuple.class);

              String key = (String) tuple.getKey();
              double count = (double) tuple.getCount();

//              Assignment 2
//              frequency.putIfAbsent(key, new AtomicLong(0));
//              frequency.get(key).addAndGet((long) count);
//              System.out.println("frequency " + frequency);

//              Assignment 3
              frequencyDao = FrequencyDao.getFrequencyDao();

//              frequencyDao.postCountReadHeavy(key, count);
              frequencyDao.postCountWriteHeavy(key, count);
            };

            channel.basicConsume(QUEUE_NAME, true, deliverCallback, consumerTag -> { });
          } catch (IOException ex) {
            Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, ex);
          } catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger(Consumer.class.getName()).log(Level.SEVERE, null, e);
          }
        }
      };
      new Thread(runnable).start();
    }
  }
}
