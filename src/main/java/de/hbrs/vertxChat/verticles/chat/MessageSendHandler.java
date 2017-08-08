package de.hbrs.vertxChat.verticles.chat;

import de.hbrs.vertxChat.verticles.enums.EventBusAddresses;
import de.hbrs.vertxChat.persistence.DbOperation;
import de.hbrs.vertxChat.verticles.enums.JsonKeys;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.apache.commons.lang3.StringEscapeUtils;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Created by ch on 07.09.2015.
 */
class MessageSendHandler implements Handler<Message<JsonObject>> {

    private final DbOperation dbOperation;
    private final EventBus eventBus;

    public MessageSendHandler(DbOperation dbOperation, EventBus eventBus) {
        this.dbOperation = dbOperation;
        this.eventBus = eventBus;
    }

    @Override
    public void handle(Message<JsonObject> bridgeMessage) {
        String sender = bridgeMessage.headers().get(JsonKeys.SOURCE.get());
        String receiver = bridgeMessage.body().getString(JsonKeys.RECEIVER.get());
        JsonArray usernameList = ChatVerticle.getAsJsonArray(sender, receiver);
        dbOperation.countUsersWhichAreRegistered(usernameList, result -> {
            if (result.succeeded() && result.result() >= usernameList.size()) {

                dbOperation.findUserWithContactInTheirList(sender, receiver, user -> {
                    if (user.succeeded() && !user.result().isEmpty()) {
                        persistAndSendMessage(bridgeMessage);
                    } else {
                        bridgeMessage.reply(ChatVerticle.getStatusMessage(JsonKeys.Status.DANGER.get(), "Empfaenger befindet sich nicht in der Kontaktliste des Nutzers."));
                    }
                });

            } else {
                bridgeMessage.reply(ChatVerticle.getStatusMessage(JsonKeys.Status.INFO.get(), "Bitte zuerst einen Kontakt auswaehlen."));
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
                bridgeMessage.reply(ChatVerticle.getStatusMessage(JsonKeys.Status.SUCCESS.get(), "Nachricht wurde gesendet.").mergeIn(document));
            } else {
                bridgeMessage.reply(ChatVerticle.getStatusMessage(JsonKeys.Status.DANGER.get(), "Nachricht konnte nicht gesendet werden."));
                result.cause().printStackTrace();
            }
        });
    }
}
