import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import Model.ResultVal;
import Model.Tuple;
import io.swagger.client.model.ErrMessage;
import io.swagger.client.model.TextLine;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.commons.pool2.impl.GenericObjectPool;

@WebServlet(name = "Servlet")
public class Servlet extends HttpServlet {

  private ConnectionFactory factory;
  private Connection conn;
  private ChannelFactory channelFactory = new ChannelFactory();
  private Channel dummychannel;
  private ObjectPool<Channel> channelPool;
  private final String USERNAME = System.getProperty("RABBIT_USERNAME");
  private final String PASSWORD = System.getProperty("RABBIT_PASSWORD");
  private final String HOST = System.getProperty("RABBIT_HOST");
  private final boolean isLocal = false;
  private final boolean DURABLE = false;
  private static final String QUEUE_NAME = "queue";
//  Value: non-persistent (1) or persistent (2)
  private final int PERSISTENT = 2;

  public class ChannelFactory extends BasePooledObjectFactory<Channel> {

    @Override
    public Channel create() throws Exception {
      return conn.createChannel();
    }

    @Override
    public PooledObject<Channel> wrap(Channel channel) {
      return new DefaultPooledObject<Channel>(channel);
    }
  }

  public void init() {

    factory = new ConnectionFactory();
    if (isLocal) {
      factory.setHost("localhost");
    } else {
      factory.setUsername("admin");
      factory.setPassword("admin");
      factory.setHost("54.204.219.31");
    }

    dummychannel = null;
    try {
      conn = factory.newConnection();
      channelPool = new GenericObjectPool<>(channelFactory);
      dummychannel = channelPool.borrowObject();
      dummychannel.queueDeclare(QUEUE_NAME, DURABLE, false, false, null);
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }

  public void destroy() {
    if (channelPool != null) {
      channelPool.close();
    }
    if (dummychannel != null) {
      try {
        dummychannel.close();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  protected void doPost(HttpServletRequest req, HttpServletResponse res)
    throws IOException {

    res.setContentType("application/json");
    String urlPath = req.getPathInfo();
    res.setCharacterEncoding("UTF-8");

    if (urlPath == null || urlPath.isEmpty()) {
      ErrMessage errMessage = new ErrMessage();
      errMessage.setMessage("invalid URL path");
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write(new Gson().toJson(errMessage));
    }


    try {
          Gson gson = new Gson();
          TextLine body = gson.fromJson(req.getReader(), TextLine.class);
          String reqBody = body.getMessage();

    if (checkNull(reqBody, res, HttpServletResponse.SC_BAD_REQUEST, "Missing requestBody")) {
      return;
    }

    Stream<String> stream = Stream.of(reqBody.split("\\s+")).parallel();
    Map<String, Long> wordFreq = stream
      .collect(Collectors.groupingBy(String::toString,Collectors.counting()));

    Channel channel = null;
    try {
      int uniqueCount = countUniqueWords(reqBody);

      channel = channelPool.borrowObject();
      // System.out.println(("ACTIVE: " + channelPool.getNumActive() + " IDLE: " + channelPool.getNumIdle()));

      for (Map.Entry<String, Long> entry : wordFreq.entrySet()) {
        String key = entry.getKey();
        Long count = entry.getValue();
        Tuple tuple = new Tuple(key, count);

        String item = gson.toJson(tuple);

        AMQP.BasicProperties props = new AMQP.BasicProperties
          .Builder()
          .deliveryMode(PERSISTENT)
          .build();

//        System.out.println(item);
        channel.basicPublish("", QUEUE_NAME,  props, item.getBytes(StandardCharsets.UTF_8));
      }

      res.setStatus(HttpServletResponse.SC_CREATED);

      res.getWriter().write(new Gson().toJson(new ResultVal(uniqueCount)));

    }
    catch (Exception e) {
      e.printStackTrace();
      res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
      res.getWriter().write("Unable to publish to rabbitmq queue");
    } finally {
      if (channel != null) {
        try {
          channelPool.returnObject(channel);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
    } catch (IllegalStateException | JsonSyntaxException e) {
      ErrMessage errMessage = new ErrMessage();
      errMessage.setMessage("invalid post body");
      res.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      res.getWriter().write(new Gson().toJson(errMessage));
    }
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse res)
    throws IOException {
    res.setContentType("application/json");
    String urlPath = req.getPathInfo();

    res.setCharacterEncoding("UTF-8");

    if (urlPath == null || urlPath.isEmpty()) {
      ErrMessage errMessage = new ErrMessage();
      errMessage.setMessage("invalid post path");
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write(new Gson().toJson(errMessage));
    }

    res.setStatus(HttpServletResponse.SC_OK);
    res.getWriter().write("It works!");

  }

  private boolean checkNull(String content, HttpServletResponse res, int resCode, String message)
      throws IOException {
    if ((content == null || content.isEmpty())) {
      res.setStatus(resCode);
      res.getWriter().write(message);
      return true;
    }
    return false;
  }

  private int countUniqueWords(String text) {
    Scanner scanner = new Scanner(text);
    Set<String> uniqueWords = new HashSet<String>();

    while (scanner.hasNext()) {
      uniqueWords.add(scanner.next());
    }
    scanner.close();
    return uniqueWords.size();
  }
}
