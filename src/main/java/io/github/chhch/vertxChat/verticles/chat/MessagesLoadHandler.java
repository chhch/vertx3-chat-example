package io.github.chhch.vertxChat.verticles.chat;

import io.github.chhch.vertxChat.persistence.DbOperation;
import io.github.chhch.vertxChat.util.I18n;
import io.github.chhch.vertxChat.verticles.enums.EventBusAddresses;
import io.github.chhch.vertxChat.verticles.enums.JsonKeys;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
/**
 * Created by ch on 07.09.2015.
 */
class MessagesLoadHandler implements Handler<Message<JsonObject>> {

    private final DbOperation dbOperation;
    private final EventBus eventBus;
    private String source;
    private String contact;

    MessagesLoadHandler(DbOperation dbOperation, EventBus eventBus) {
        this.dbOperation = dbOperation;
        this.eventBus = eventBus;
    }

    @Override
    public void handle(Message<JsonObject> message) {
        markMessagesAsReadAndSendThem(message);
    }

    private void markMessagesAsReadAndSendThem(Message<JsonObject> message) {
        source = message.headers().get(JsonKeys.SOURCE.get());
        contact = message.body().getString(JsonKeys.CONTACT.get());
        boolean findOnlyUnreadMessages = message.body().getBoolean(JsonKeys.ONLY_UNREAD.get());

        if (findOnlyUnreadMessages) {
            dbOperation.findUnreadMessagesFromContact(source, contact, event -> handleResult(message, event));
        } else {
            dbOperation.findMessagesFromContact(source, contact, event -> handleResult(message, event));
        }
    }

    private void handleResult(Message<JsonObject> message, AsyncResult<List<JsonObject>> result) {
        if (result.succeeded()) {
            JsonObject conversationLoadSucceeded = ChatVerticle.getStatusMessage(
                    JsonKeys.Status.SUCCESS.get(),
                    I18n.getString("conversationLoadSucceeded")
            );
            conversationLoadSucceeded.put(JsonKeys.CONVERSATION.get(), result.result());
            message.reply(conversationLoadSucceeded);
            markMessageAsRead();
        } else {
            JsonObject conversationLoadFailed = ChatVerticle.getStatusMessage(
                    JsonKeys.Status.DANGER.get(),
                    I18n.getString("conversationLoadFailed")
            );
            message.reply(conversationLoadFailed);
            result.cause().printStackTrace();
        }
    }

    private void markMessageAsRead() {
        dbOperation.resetUnreadMessageCount(source, contact, result -> {
            if (result.failed()) {
                result.cause().printStackTrace();
            }
        });

        dbOperation.updateMessagesAsRead(source, contact, result -> {
            if (result.succeeded()) {
                JsonObject messagesRead = new JsonObject().put(JsonKeys.RECEIVER.get(), source).put(JsonKeys.MESSAGE_COUNT.get(), -1);
                eventBus.publish(EventBusAddresses.CHAT_RECEIVE_READ_NOTIFICATION.get() + "." + contact, messagesRead);
            } else {
                result.cause().printStackTrace();
            }
        });
    }
}
