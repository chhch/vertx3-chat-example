package io.github.chhch.vertxChat.verticles.chat;

import io.github.chhch.vertxChat.I18n;
import io.github.chhch.vertxChat.persistence.DbOperation;
import io.github.chhch.vertxChat.verticles.enums.EventBusAddresses;
import io.github.chhch.vertxChat.verticles.enums.JsonKeys;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;

/**
 * Created by ch on 07.09.2015.
 */
class ConversationLoadHandler implements Handler<Message<JsonObject>> {

    private final DbOperation dbOperation;
    private final EventBus eventBus;
    private String source;
    private String contact;

    public ConversationLoadHandler(DbOperation dbOperation, EventBus eventBus) {
        this.dbOperation = dbOperation;
        this.eventBus = eventBus;
    }

    @Override
    public void handle(Message<JsonObject> message) {
        String sender = message.headers().get(JsonKeys.SOURCE.get());
        String contact = message.body().getString(JsonKeys.CONTACT.get());
        JsonArray usernameList = ChatVerticle.getAsJsonArray(sender, contact);

        dbOperation.countUsersWhichAreRegistered(usernameList, result -> {
            if (result.succeeded() && result.result() >= usernameList.size()) {
                markMessagesAsReadAndSendThem(message);
            } else {
                JsonObject conversationLoadFailedUserNotFound = ChatVerticle.getStatusMessage(
                        JsonKeys.Status.DANGER.get(),
                        I18n.getString("conversationLoadFailedUserNotFound")
                );
                message.reply(conversationLoadFailedUserNotFound);
            }
        });
    }

    private void markMessagesAsReadAndSendThem(Message<JsonObject> message) {
        boolean onlyUnreadFromContact = message.body().getBoolean(JsonKeys.ONLY_UNREAD.get());
        source = message.headers().get(JsonKeys.SOURCE.get());
        contact = message.body().getString(JsonKeys.CONTACT.get());

        if (onlyUnreadFromContact) {
            dbOperation.findUnreadMessagesFromContact(source, contact, result -> handleResult(message, result));
        } else {
            dbOperation.findConversation(source, contact, result -> handleResult(message, result));
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

        dbOperation.updateMessagesOfOneConversationAsRead(source, contact, result -> {
            if (result.succeeded()) {
                JsonObject messagesRead = new JsonObject().put(JsonKeys.RECEIVER.get(), source).put(JsonKeys.MESSAGE_COUNT.get(), -1);
                eventBus.publish(EventBusAddresses.CHAT_RECEIVE_READ_NOTIFICATION.get() + "." + contact, messagesRead);
            } else {
                result.cause().printStackTrace();
            }
        });
    }
}
