package io.github.chhch.vertxChat.verticles.chat;

import io.github.chhch.vertxChat.persistence.DbOperation;
import io.github.chhch.vertxChat.verticles.enums.EventBusAddresses;
import io.github.chhch.vertxChat.verticles.enums.JsonKeys;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * Created by ch on 07.09.2015.
 */
class ContactAddHandler implements Handler<Message<JsonObject>> {

    private final DbOperation dbOperation;
    private final EventBus eventBus;

    public ContactAddHandler(DbOperation dbOperation, EventBus eventBus) {
        this.dbOperation = dbOperation;
        this.eventBus = eventBus;
    }

    @Override
    public void handle(Message<JsonObject> message) {
        String sender = message.headers().get(JsonKeys.SOURCE.get());
        String contact = message.body().getString(JsonKeys.CONTACT.get());
        JsonArray usernameList = ChatVerticle.getAsJsonArray(sender, contact);

        if(sender.equals(contact)) {
            message.reply(ChatVerticle.getStatusMessage(JsonKeys.Status.DANGER.get(), "Benutzer kann sich nicht selbst zur Kontaktliste hinzufuegen."));
            return;
        }

        dbOperation.countUsersWhichAreRegistered(usernameList, result -> {
            if (result.succeeded() && result.result() == usernameList.size()) {

                dbOperation.findUserWithContactInTheirList(sender, contact, user -> {
                    if (user.succeeded() && user.result().isEmpty()) {
                        persistAndSendContact(message);
                    } else {
                        message.reply(ChatVerticle.getStatusMessage(JsonKeys.Status.DANGER.get(), "Kontakt befindet sich bereits in der Liste"));
                    }
                });

            } else {
                message.reply(ChatVerticle.getStatusMessage(JsonKeys.Status.DANGER.get(), "Kontakt nicht gefunden."));
            }
        });
    }

    private void persistAndSendContact(Message<JsonObject> message) {
        String source = message.headers().get(JsonKeys.SOURCE.get());
        String newContact = message.body().getString(JsonKeys.CONTACT.get());
        dbOperation.updateUsersContactList(newContact, source, result -> {
            if(result.failed()) {
                result.cause().printStackTrace();
            }
        });
        dbOperation.updateUsersContactList(source, newContact, result -> {
            if (result.succeeded()) {
                eventBus.publish(EventBusAddresses.CHAT_RECEIVE_CONTACT.get() + "." + newContact, source);
                message.reply(ChatVerticle.getStatusMessage(JsonKeys.Status.SUCCESS.get(), "Kontaktliste wurde aktualisiert.").put(JsonKeys.CONTACT.get(), newContact));
            } else {
                message.reply(ChatVerticle.getStatusMessage(JsonKeys.Status.DANGER.get(), "Konnte Kontaktliste nicht aktualisieren."));
                result.cause().printStackTrace();
            }
        });
    }
}
