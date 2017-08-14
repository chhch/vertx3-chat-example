package io.github.chhch.vertxChat;

import io.github.chhch.vertxChat.verticles.chat.ChatVerticle;
import io.github.chhch.vertxChat.verticles.website.WebsiteVerticle;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.embeddedmongo.EmbeddedMongoVerticle;

/**
 * Created by ch on 31.08.2015.
 */
public class ChatApp {
    private static final boolean USE_EMBEDDED_MONGO_DB = false;
    private static final int EMBEDDED_MONGO_DB_PORT = 27017;
    private static Vertx vertx;

    public static void main(String[] args) {
        vertx = Vertx.vertx();

        Future<String> embeddedMongo = Future.future();
        Future<String> chat = Future.future();
        Future<String> web = Future.future();

        if (USE_EMBEDDED_MONGO_DB) {
            deployEmbeddedMongoDB(embeddedMongo);
        } else {
            embeddedMongo.complete("use another one");
        }

        embeddedMongo.setHandler(result -> {
            if (result.succeeded()) {
                String message = USE_EMBEDDED_MONGO_DB ? "Embedded MongoDB Verticle deployed"
                                                       : "Embedded MongoDB Verticle not deployed (use another one)";
                System.out.println(message);
                deployVerticles(chat, web);
            } else {
                System.out.println("ERROR: Embedded MongoDB Verticle deployment failed!");
            }
        });

        CompositeFuture.join(chat, web).setHandler(result -> {
            if (chat.succeeded()) {
                System.out.println("ChatVerticle successfully deployed");
            } else {
                System.out.println("ERROR: ChatVerticle deployment failed!");
            }

            if (web.succeeded()) {
                System.out.println("WebsiteVerticle successfully deployed");
            } else {
                System.out.println("ERROR: WebsiteVerticle deployment failed!");
            }

            if (chat.succeeded() && web.succeeded()) {
                System.out.println("Visit: http://localhost:8080");
            }
        });
    }

    private static void deployEmbeddedMongoDB(Future<String> embeddedMongo) {
        // The MongoDB download need some time. After that, any further start will be much faster
        VertxOptions vertxOptions = new VertxOptions().setMaxWorkerExecuteTime(30 * 60 * 1000);
        Vertx.vertx(vertxOptions);

        // a port number is needed by EmbeddedMongoVerticle
        DeploymentOptions mongoOptions = new DeploymentOptions().setWorker(true)
                .setConfig(new JsonObject().put("port", EMBEDDED_MONGO_DB_PORT));
        vertx.deployVerticle(new EmbeddedMongoVerticle(), mongoOptions, embeddedMongo);
    }

    private static void deployVerticles(Future<String> chat, Future<String> web) {
        vertx.deployVerticle(new ChatVerticle(), chat.completer());
        vertx.deployVerticle(new WebsiteVerticle(), web.completer());
    }
}
