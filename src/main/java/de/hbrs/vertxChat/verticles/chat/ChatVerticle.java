package de.hbrs.vertxChat.verticles.chat;

import de.hbrs.vertxChat.persistence.DbOperation;
import de.hbrs.vertxChat.verticles.enums.EventBusAddresses;
import de.hbrs.vertxChat.verticles.enums.JsonKeys;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

import java.util.Arrays;

/**
 * Created by ch on 02.09.2015.
 */
public class ChatVerticle extends AbstractVerticle {

    public static JsonObject getStatusMessage(String status, String message) {
        return new JsonObject().put(JsonKeys.STATUS.get(), status).put(JsonKeys.STATUS_MESSAGE.get(), message);
    }

    public static JsonArray getAsJsonArray(String... strings) {
        return new JsonArray(Arrays.asList(strings));
    }

    @Override
    public void start() throws Exception {
        EventBus eventBus = vertx.eventBus();
        MongoClient mongoClient = MongoClient.createShared(vertx, new JsonObject());
        DbOperation dbOperation = new DbOperation(mongoClient);

        eventBus.consumer(EventBusAddresses.CHAT_ADD_CONTACT.get(), new ContactAddHandler(dbOperation, eventBus));
        eventBus.consumer(EventBusAddresses.CHAT_GET_CONTACT_MAP.get(), new ContactListLoadHandler(dbOperation, eventBus));
        eventBus.consumer(EventBusAddresses.CHAT_GET_CONVERSATION.get(), new ConversationLoadHandler(dbOperation, eventBus));
        eventBus.consumer(EventBusAddresses.CHAT_SEND_READ_NOTIFICATION.get(), new MessageReadHandler(dbOperation, eventBus));
        eventBus.consumer(EventBusAddresses.CHAT_SEND_MESSAGE.get(), new MessageSendHandler(dbOperation, eventBus));
    }

}
