package io.github.chhch.vertxChat;

import io.github.chhch.vertxChat.verticles.chat.ChatVerticle;
import io.github.chhch.vertxChat.verticles.website.WebsiteVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.embeddedmongo.EmbeddedMongoVerticle;

/**
 * Created by ch on 31.08.2015.
 */
public class ChatApp {
    private static final boolean USE_EMBEDDED_MONGO_DB = false;
    private static final int EMBEDDED_MONGO_DB_PORT = 27017;

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        if (USE_EMBEDDED_MONGO_DB) {
            startEmbeddedMongoDB(vertx);
        } else {
            deployVerticles(vertx);
        }
    }

    private static void startEmbeddedMongoDB(final Vertx vertx) {
        // The MongoDB download need some time. After that, any further start will be much faster
        VertxOptions vertxOptions = new VertxOptions().setMaxWorkerExecuteTime(30 * 60 * 1000);
        final Vertx vertxOpt = Vertx.vertx(vertxOptions);

        // a port number is needed by EmbeddedMongoVerticle
        DeploymentOptions mongoOptions = new DeploymentOptions().setWorker(true)
                .setConfig(new JsonObject().put("port", EMBEDDED_MONGO_DB_PORT));
        vertx.deployVerticle(new EmbeddedMongoVerticle(), mongoOptions, res -> {
            if (res.succeeded()) {
                System.out.println("Embedded MongoDB Verticle successfully deployed");
                deployVerticles(vertx);
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
                System.out.println("WebsiteVerticle successfully deployed," +
                        " visit: http://localhost:8080");
            } else {
                System.out.println("ERROR: WebsiteVerticle deployment failed!");
            }
        });
    }
}
