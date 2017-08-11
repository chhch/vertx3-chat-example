package io.github.chhch.vertxChat.verticles.chat;

import io.github.chhch.vertxChat.persistence.DbOperation;
import io.github.chhch.vertxChat.verticles.enums.EventBusAddresses;
import io.github.chhch.vertxChat.verticles.enums.JsonKeys;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * Created by ch on 07.09.2015.
 */
class MessageReadHandler implements Handler<Message<JsonObject>> {

    private final DbOperation dbOperation;
    private final EventBus eventBus;

    public MessageReadHandler(DbOperation dbOperation, EventBus eventBus) {
        this.dbOperation = dbOperation;
        this.eventBus = eventBus;
    }

    @Override
    public void handle(Message<JsonObject> message) {
        String receiver = message.headers().get(JsonKeys.SOURCE.get());
        String sender = message.body().getString(JsonKeys.SENDER.get());

        dbOperation.decrementUnreadMessageCount(sender, receiver, result -> {
            if (result.failed()) {
                result.cause().printStackTrace();
            }
        });

        // No reason to wait of the db transaction result before informing the sender, because at this time the receiver
        //  has already ?confirmed that he read the message. This didn't change although the db transaction fail.
        JsonObject messageJson = new JsonObject().put(JsonKeys.RECEIVER.get(), receiver).put(JsonKeys.MESSAGE_COUNT.get(), 1);
        eventBus.publish(EventBusAddresses.CHAT_RECEIVE_READ_NOTIFICATION.get() + "." + sender, messageJson);

        dbOperation.updateFirstUnreadMessageOfOneConversationAsRead(receiver, sender, result -> {
            if (result.failed()) {
                result.cause().printStackTrace();
            }
        });
    }
}
