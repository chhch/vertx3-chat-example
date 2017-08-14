package io.github.chhch.vertxChat.verticles.chat;

import io.github.chhch.vertxChat.persistence.DbOperation;
import io.github.chhch.vertxChat.util.I18n;
import io.github.chhch.vertxChat.verticles.enums.EventBusAddresses;
import io.github.chhch.vertxChat.verticles.enums.JsonKeys;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

/**
 * Created by ch on 07.09.2015.
 */
class ContactAddHandler implements Handler<Message<JsonObject>> {

    private final DbOperation dbOperation;
    private final EventBus eventBus;

    ContactAddHandler(DbOperation dbOperation, EventBus eventBus) {
        this.dbOperation = dbOperation;
        this.eventBus = eventBus;
    }

    @Override
    public void handle(Message<JsonObject> message) {
        String sender = message.headers().get(JsonKeys.SOURCE.get());
        String contact = message.body().getString(JsonKeys.CONTACT.get());

        if (sender.equals(contact)) {
            JsonObject contactAddFailedSelfAdding = ChatVerticle.getStatusMessage(
                    JsonKeys.Status.DANGER.get(),
                    I18n.getString("contactAddFailedSelfAdding")
            );
            message.reply(contactAddFailedSelfAdding);
            return;
        }

        Future<JsonObject> findUser = Future.future();
        Future<JsonObject> findContact = Future.future();
        dbOperation.findUser(contact, findUser.completer());
        dbOperation.findContact(sender, contact, findContact.completer());

        CompositeFuture.join(findUser, findContact).setHandler(result -> {
            if (result.result().failed(0) || result.result().resultAt(0) == null) {
                JsonObject contactAddFailedUserNotFound = ChatVerticle.getStatusMessage(
                        JsonKeys.Status.DANGER.get(),
                        I18n.getString("contactAddFailedUserNotFound")
                );
                message.reply(contactAddFailedUserNotFound);
            } else if (result.result().failed(1) || result.result().resultAt(1) != null) {
                JsonObject contactAddFailedAlreadyAdded = ChatVerticle.getStatusMessage(
                        JsonKeys.Status.DANGER.get(),
                        I18n.getString("contactAddFailedAlreadyAdded")
                );
                message.reply(contactAddFailedAlreadyAdded);
            } else {
                persistAndSendContact(message);
            }
        });
    }

    private void persistAndSendContact(Message<JsonObject> message) {
        String source = message.headers().get(JsonKeys.SOURCE.get());
        String newContact = message.body().getString(JsonKeys.CONTACT.get());

        dbOperation.updateContactList(newContact, source, result -> {
            if (result.failed()) {
                result.cause().printStackTrace();
            }
        });

        dbOperation.updateContactList(source, newContact, result -> {
            if (result.succeeded()) {
                eventBus.publish(EventBusAddresses.CHAT_RECEIVE_CONTACT.get() + "." + newContact, source);
                JsonObject contactListRefreshSucceeded = ChatVerticle.getStatusMessage(
                        JsonKeys.Status.SUCCESS.get(),
                        I18n.getString("contactListRefreshSucceeded")
                );
                contactListRefreshSucceeded.put(JsonKeys.CONTACT.get(), newContact);
                message.reply(contactListRefreshSucceeded);
            } else {
                JsonObject contactListRefreshFailed = ChatVerticle.getStatusMessage(
                        JsonKeys.Status.DANGER.get(),
                        I18n.getString("contactListRefreshFailed")
                );
                message.reply(contactListRefreshFailed);
                result.cause().printStackTrace();
            }
        });
    }
}
