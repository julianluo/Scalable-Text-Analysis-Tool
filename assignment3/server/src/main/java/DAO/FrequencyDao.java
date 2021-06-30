package DAO;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.UpdateOptions;

import org.bson.Document;

import java.util.*;

import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;

public class FrequencyDao {
  private static FrequencyDao frequencyDao;
  private static MongoCollection<Document> dbCollection;

  private FrequencyDao() {
    dbCollection = DBConnection.getCollectionWordRecords();
  }

  public static FrequencyDao getFrequencyDao() {
    if (frequencyDao == null) {
      frequencyDao = new FrequencyDao();
    }
    return frequencyDao;
  }

  public Document create(Document wordRecord) {
    dbCollection.insertOne(wordRecord);
    return wordRecord;
  }

  public void createMany(List<Document> wordRecords) {
    dbCollection.insertMany(wordRecords);
  }

  public Document getCountReadHeavy(String key) {
    Document document = dbCollection
      .find(new BasicDBObject("_id", key))
      .projection(fields(include("count"), excludeId())).first();
    return document;
  }

  public void postCountReadHeavy(String key, double count) {
    UpdateOptions options = new UpdateOptions().upsert(true);
    dbCollection.updateOne(new Document("_id", key),
        new Document("$inc", new Document("count", count)), options);
  }

  public double getCountWriteHeavy(String key) {
    FindIterable<Document> findIterable = dbCollection
      .find(new BasicDBObject("key", key))
      .projection(fields(include("count"), excludeId()));

    double sum = 0;
    MongoCursor<Document> cursor = findIterable.iterator();
    while (cursor.hasNext()) {
      Document document = cursor.next();
      sum += (double) document.get("count");
//      System.out.println(sum);
    }
    return sum;
  }

  public void postCountWriteHeavy(String key, double count) {
    Document document = new Document();
    document.append("key", key);
    document.append("count", count);

    dbCollection.insertOne(document);
  }
}

