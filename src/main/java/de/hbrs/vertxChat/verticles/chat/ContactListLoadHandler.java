package de.hbrs.vertxChat.verticles.chat;

import de.hbrs.vertxChat.persistence.DbOperation;
import de.hbrs.vertxChat.verticles.enums.JsonKeys;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * Created by ch on 07.09.2015.
 */
class ContactListLoadHandler implements Handler<Message<JsonObject>> {

    private final DbOperation dbOperation;
    private final EventBus eventBus;

    public ContactListLoadHandler(DbOperation dbOperation, EventBus eventBus) {
        this.dbOperation = dbOperation;
        this.eventBus = eventBus;
    }

    @Override
    public void handle(Message<JsonObject> message) {
        String source = message.headers().get(JsonKeys.SOURCE.get());

        dbOperation.findUsersContactList(source, result -> {
            if (result.succeeded()) {
                message.reply(result.result());
            } else {
                message.reply(ChatVerticle.getStatusMessage(JsonKeys.Status.DANGER.get(), "Konnte Kontaktliste nicht laden."));
                result.cause().printStackTrace();
            }
        });
    }
}
