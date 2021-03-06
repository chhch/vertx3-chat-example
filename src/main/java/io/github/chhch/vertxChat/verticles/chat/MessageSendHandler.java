package io.github.chhch.vertxChat.verticles.chat;

import io.github.chhch.vertxChat.persistence.DbOperation;
import io.github.chhch.vertxChat.util.I18n;
import io.github.chhch.vertxChat.verticles.enums.EventBusAddresses;
import io.github.chhch.vertxChat.verticles.enums.JsonKeys;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by ch on 07.09.2015.
 */
class MessageSendHandler implements Handler<Message<JsonObject>> {

    private final DbOperation dbOperation;
    private final EventBus eventBus;

    MessageSendHandler(DbOperation dbOperation, EventBus eventBus) {
        this.dbOperation = dbOperation;
        this.eventBus = eventBus;
    }

    @Override
    public void handle(Message<JsonObject> bridgeMessage) {
        String sender = bridgeMessage.headers().get(JsonKeys.SOURCE.get());
        String receiver = bridgeMessage.body().getString(JsonKeys.RECEIVER.get());

        dbOperation.findContact(sender, receiver, foundContact -> {
            if (foundContact.succeeded() && foundContact.result() != null) {
                persistAndSendMessage(bridgeMessage);
            } else {
                JsonObject messageSendFailedReceiverIsNotAContact = ChatVerticle.getStatusMessage(
                        JsonKeys.Status.DANGER.get(),
                        I18n.getString("messageSendFailedReceiverIsNotAContact")
                );
                bridgeMessage.reply(messageSendFailedReceiverIsNotAContact);
            }
        });
    }

    private void persistAndSendMessage(Message<JsonObject> bridgeMessage) {
        String sender = bridgeMessage.headers().get(JsonKeys.SOURCE.get());

        JsonObject body = bridgeMessage.body();
        String receiver = body.getString(JsonKeys.RECEIVER.get());
        String message = body.getString(JsonKeys.MESSAGE.get());
        String now = ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        dbOperation.incrementUnreadMessageCount(sender, receiver, result -> {
            if (result.failed()) {
                result.cause().printStackTrace();
            }
        });

        dbOperation.insertMessage(sender, receiver, message, now, result -> {
            if (result.succeeded()) {
                JsonObject document = new JsonObject()
                        .put(JsonKeys.DATE.get(), new JsonObject().put("$date", now))
                        .put(JsonKeys.SENDER.get(), sender)
                        .put(JsonKeys.RECEIVER.get(), receiver)
                        .put(JsonKeys.MESSAGE.get(), message)
                        .put(JsonKeys.READ.get(), false);

                eventBus.publish(EventBusAddresses.CHAT_RECEIVE_MESSAGE.get() + "." + receiver, document);
                JsonObject messageSendSucceeded = ChatVerticle.getStatusMessage(
                        JsonKeys.Status.SUCCESS.get(),
                        I18n.getString("messageSendSucceeded")
                ).mergeIn(document);
                bridgeMessage.reply(messageSendSucceeded);
            } else {
                JsonObject messageSendFailed = ChatVerticle.getStatusMessage(
                        JsonKeys.Status.DANGER.get(),
                        I18n.getString("messageSendFailed")
                );
                bridgeMessage.reply(messageSendFailed);
                result.cause().printStackTrace();
            }
        });
    }
}
