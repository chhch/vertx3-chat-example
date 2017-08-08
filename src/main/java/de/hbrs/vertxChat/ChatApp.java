package de.hbrs.vertxChat;

import de.hbrs.vertxChat.verticles.chat.ChatVerticle;
import de.hbrs.vertxChat.verticles.website.WebsiteVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.embeddedmongo.EmbeddedMongoVerticle;
import io.vertx.ext.mongo.MongoClient;

/**
 * Created by ch on 31.08.2015.
 */
public class ChatApp {
    private final static boolean USE_EMBEDDED_MONGO_DB = false;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        // To stop the MongoDB-Server after terminating the program, type in the console/terminal:
        // 1) mongo
        // 2) use admin
        // 3) db.shutdownServer()
        if (USE_EMBEDDED_MONGO_DB) {
            startEmbeddedMongoDB(vertx);
        } else {
            deployVerticles(vertx);
//            testMongoDb(vertx);
        }
    }

    private static void startEmbeddedMongoDB(final Vertx vertx) {
        // The MongoDB download need some time. After that, any further start will be much faster
        VertxOptions vertxOptions = new VertxOptions().setMaxWorkerExecuteTime(30 * 60 * 1000);
        final Vertx vertxOpt = Vertx.vertx(vertxOptions);

        // a port number is needed by EmbeddedMongoVerticle
        DeploymentOptions mongoOptions = new DeploymentOptions().setWorker(true).setConfig(new JsonObject().put("port", 27017));
        vertx.deployVerticle(new EmbeddedMongoVerticle(), mongoOptions, res -> {
            if (res.succeeded()) {
                System.out.println("Embedded MongoDB Verticle successfully deployed");
                deployVerticles(vertx);
                testMongoDb(vertxOpt);
            } else {
                System.out.println("ERROR: Embedded MongoDB Verticle deployment failed!");
            }
        });
    }

    private static void deployVerticles(Vertx vertx) {
        vertx.deployVerticle(new ChatVerticle(), completionHandler -> {
            if (completionHandler.succeeded()) {
                System.out.println("ChatVerticle successfully deployed");
            } else {
                System.out.println("ERROR: ChatVerticle deployment failed!");
            }
        });

        vertx.deployVerticle(new WebsiteVerticle(), completionHandler -> {
            if (completionHandler.succeeded()) {
                System.out.println("WebsiteVerticle successfully deployed");
            } else {
                System.out.println("ERROR: WebsiteVerticle deployment failed!");
            }
        });
    }

    private static void testMongoDb(Vertx vertx) {
        MongoClient mongoService = MongoClient.createNonShared(vertx, new JsonObject());
        JsonObject document = new JsonObject().put("_key", "_value");

        mongoService.save("__test", document, result -> {
            if (result.succeeded()) {
                System.out.println("MongoDB is running");
            } else {
                System.out.println("MongoDB isn't running");
                result.cause().printStackTrace();
            }
        });
    }
}
