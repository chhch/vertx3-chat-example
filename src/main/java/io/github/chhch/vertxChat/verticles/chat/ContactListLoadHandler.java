package io.github.chhch.vertxChat.verticles.chat;

import io.github.chhch.vertxChat.util.I18n;
import io.github.chhch.vertxChat.persistence.DbOperation;
import io.github.chhch.vertxChat.verticles.enums.JsonKeys;
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

    ContactListLoadHandler(DbOperation dbOperation, EventBus eventBus) {
        this.dbOperation = dbOperation;
        this.eventBus = eventBus;
    }

    @Override
    public void handle(Message<JsonObject> message) {
        String source = message.headers().get(JsonKeys.SOURCE.get());

        dbOperation.findContactList(source, result -> {
            if (result.succeeded()) {
                message.reply(result.result());
            } else {
                JsonObject contactListLoadFailed = ChatVerticle.getStatusMessage(
                        JsonKeys.Status.DANGER.get(),
                        I18n.getString("contactListLoadFailed")
                );
                message.reply(contactListLoadFailed);
                result.cause().printStackTrace();
            }
        });
    }
}
