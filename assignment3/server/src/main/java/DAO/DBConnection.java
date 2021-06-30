package DAO;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

import org.bson.Document;

// Singleton Class
class DBConnection {

  private static MongoDatabase db;
  private static MongoCollection<Document> wordRecordsCollection;
  private static UpdateOptions upsert;

//  30
  private static final int CONNECTION_POOL_SIZE = 50;
//  private static String MONGODB_HOST = "localhost";
  private static final String MONGODB_HOST = "100.25.190.73";

  static MongoDatabase getDB() {
    if (db == null) {
      MongoClientOptions.Builder builder = new MongoClientOptions.Builder();
      MongoClientOptions options = builder.connectionsPerHost(CONNECTION_POOL_SIZE).build();

      MongoClient client = new MongoClient(new ServerAddress(MONGODB_HOST, 27017), options);
      db = client.getDatabase("dummyDB");
    }
    return db;
  }

  static MongoCollection<Document> getCollectionWordRecords() {
    if (wordRecordsCollection == null) {
      wordRecordsCollection = getDB().getCollection("dummyCollection");
    }
    return wordRecordsCollection;
  }

  public static UpdateOptions getUpsert() {
    if (upsert == null) {
      upsert = new UpdateOptions().upsert(true);
    }
    return upsert;
  }

}
