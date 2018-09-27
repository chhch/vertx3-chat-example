package io.github.chhch.vertxChat.verticles.chat;

import io.github.chhch.vertxChat.persistence.DbOperation;
import io.github.chhch.vertxChat.verticles.enums.EventBusAddresses;
import io.github.chhch.vertxChat.verticles.enums.JsonKeys;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;

/**
 * Created by ch on 02.09.2015.
 */
public class ChatVerticle extends AbstractVerticle {

    static JsonObject getStatusMessage(String status, String message) {
        return new JsonObject().put(JsonKeys.STATUS.get(), status).put(JsonKeys.STATUS_MESSAGE.get(), message);
    }

    @Override
    public void start() {
        EventBus eventBus = vertx.eventBus();
        MongoClient mongoClient = MongoClient.createShared(vertx, new JsonObject());
        DbOperation dbOperation = new DbOperation(mongoClient);

        eventBus.consumer(EventBusAddresses.CHAT_ADD_CONTACT.get(), new ContactAddHandler(dbOperation, eventBus));
        eventBus.consumer(EventBusAddresses.CHAT_GET_CONTACT_MAP.get(), new ContactListLoadHandler(dbOperation, eventBus));
        eventBus.consumer(EventBusAddresses.CHAT_GET_CONVERSATION.get(), new MessagesLoadHandler(dbOperation, eventBus));
        eventBus.consumer(EventBusAddresses.CHAT_SEND_READ_NOTIFICATION.get(), new MessageReadHandler(dbOperation, eventBus));
        eventBus.consumer(EventBusAddresses.CHAT_SEND_MESSAGE.get(), new MessageSendHandler(dbOperation, eventBus));
    }

}
