package io.github.chhch.vertxChat.persistence;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.FindOptions;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.mongo.MongoClientUpdateResult;
import io.vertx.ext.mongo.UpdateOptions;

import java.util.List;

/**
 * Created by ch on 07.09.2015.
 */
public class DbOperation {

    private MongoClient mongoClient;

    public DbOperation(MongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    public void updateMessagesAsRead(String source, String contact, Handler<AsyncResult<MongoClientUpdateResult>> asyncResultHandler) {
        updateMessagesAsRead(source, contact, asyncResultHandler, true);
    }

    public void updateFirstUnreadMessageAsRead(String receiver, String sender, Handler<AsyncResult<MongoClientUpdateResult>> asyncResultHandler) {
        updateMessagesAsRead(receiver, sender, asyncResultHandler, false);
    }

    public void resetUnreadMessageCount(String user, String contact, Handler<AsyncResult<MongoClientUpdateResult>> asyncResultHandler) {
        updateUnreadMessageCount(user, contact, "$set", 0, asyncResultHandler);
    }

    public void incrementUnreadMessageCount(String sender, String receiver, Handler<AsyncResult<MongoClientUpdateResult>> asyncResultHandler) {
        updateUnreadMessageCount(receiver, sender, "$inc", 1, asyncResultHandler);
    }

    public void decrementUnreadMessageCount(String sender, String receiver, Handler<AsyncResult<MongoClientUpdateResult>> asyncResultHandler) {
        updateUnreadMessageCount(receiver, sender, "$inc", -1, asyncResultHandler);
    }

    public void findUnreadMessagesFromContact(String source, String contact, Handler<AsyncResult<List<JsonObject>>> asyncResultHandler) {
        JsonObject query = new JsonObject()
                .put(DbIdentifier.MESSAGE_SENDER_FLD.get(), contact)
                .put(DbIdentifier.MESSAGE_RECEIVER_FLD.get(), source)
                .put(DbIdentifier.MESSAGE_READ_FLD.get(), false);

        FindOptions options = new FindOptions().setSort(new JsonObject().put(DbIdentifier.MESSAGE_DATE_FLD.get(), 1));

        mongoClient.findWithOptions(DbIdentifier.MESSAGE_COL.get(), query, options, asyncResultHandler);
    }

    public void findMessagesFromContact(String source, String contact, Handler<AsyncResult<List<JsonObject>>> asyncResultHandler) {
        JsonObject query = new JsonObject().put("$or", new JsonArray()
                .add(new JsonObject()
                        .put(DbIdentifier.MESSAGE_SENDER_FLD.get(), source)
                        .put(DbIdentifier.MESSAGE_RECEIVER_FLD.get(), contact))
                .add(new JsonObject()
                        .put(DbIdentifier.MESSAGE_SENDER_FLD.get(), contact)
                        .put(DbIdentifier.MESSAGE_RECEIVER_FLD.get(), source))
        );
        FindOptions options = new FindOptions().setSort(new JsonObject().put(DbIdentifier.MESSAGE_DATE_FLD.get(), 1));

        mongoClient.findWithOptions(DbIdentifier.MESSAGE_COL.get(), query, options, asyncResultHandler);
    }

    public void insertMessage(String sender, String receiver, String message, String date, Handler<AsyncResult<String>> asyncResultHandler) {
        JsonObject document = new JsonObject()
                .put(DbIdentifier.MESSAGE_DATE_FLD.get(), new JsonObject().put("$date", date))
                .put(DbIdentifier.MESSAGE_SENDER_FLD.get(), sender)
                .put(DbIdentifier.MESSAGE_RECEIVER_FLD.get(), receiver)
                .put(DbIdentifier.MESSAGE_MESSAGE_FLD.get(), message)
                .put(DbIdentifier.MESSAGE_READ_FLD.get(), false);

        mongoClient.insert(DbIdentifier.MESSAGE_COL.get(), document, asyncResultHandler);
    }

    public void findContactList(String source, Handler<AsyncResult<JsonObject>> asyncResultHandler) {
        JsonObject query = new JsonObject().put(DbIdentifier.USER_NAME_FLD.get(), source);
        JsonObject fields = new JsonObject().put(DbIdentifier.USER_CONTACTS_MAP.get(), 1);

        mongoClient.findOne(DbIdentifier.USER_COL.get(), query, fields, asyncResultHandler);
    }

    public void updateContactList(String user, String newContact, Handler<AsyncResult<MongoClientUpdateResult>> asyncResultHandler) {
        JsonObject query = new JsonObject().put(DbIdentifier.USER_NAME_FLD.get(), user);
        JsonObject update = new JsonObject().put("$addToSet", new JsonObject().put(DbIdentifier.USER_CONTACTS_MAP.get(), new JsonObject()
                .put(DbIdentifier.USER_CONTACTS_NAME_FLD.get(), newContact).put(DbIdentifier.USER_CONTACTS_UNREADMESSAGES_FLD.get(), 0)));

        mongoClient.updateCollection(DbIdentifier.USER_COL.get(), query, update, asyncResultHandler);
    }

    public void findUser(String user, Handler<AsyncResult<JsonObject>> asyncResultHandler1) {
        JsonObject query = new JsonObject().put(DbIdentifier.USER_NAME_FLD.get(), user);
        JsonObject fields = new JsonObject().put(DbIdentifier.USER_NAME_FLD.get(), 1);

        mongoClient.findOne(DbIdentifier.USER_COL.get(), query, fields, asyncResultHandler1);
    }

    public void findContact(String user, String contact, Handler<AsyncResult<JsonObject>> asyncResultHandler) {
        JsonObject query = new JsonObject()
                .put(DbIdentifier.USER_NAME_FLD.get(), user)
                .put(DbIdentifier.USER_CONTACTS_MAP.get() + "." + DbIdentifier.USER_CONTACTS_NAME_FLD.get(), contact);
        JsonObject fields = new JsonObject().put(DbIdentifier.USER_NAME_FLD.get(), 1);

        mongoClient.findOne(DbIdentifier.USER_COL.get(), query, fields, asyncResultHandler);
    }

    private void updateUnreadMessageCount(String user, String contact, String operator, int value, Handler<AsyncResult<MongoClientUpdateResult>> asyncResultHandler) {
        JsonObject query = new JsonObject()
                .put(DbIdentifier.USER_NAME_FLD.get(), user)
                .put(DbIdentifier.USER_CONTACTS_MAP.get() + "." + DbIdentifier.USER_CONTACTS_NAME_FLD.get(), contact);
        JsonObject update = new JsonObject().put(operator, new JsonObject()
                .put(DbIdentifier.USER_CONTACTS_MAP.get() + ".$." + DbIdentifier.USER_CONTACTS_UNREADMESSAGES_FLD.get(), value));

        mongoClient.updateCollection(DbIdentifier.USER_COL.get(), query, update, asyncResultHandler);
    }

    private void updateMessagesAsRead(String source, String contact, Handler<AsyncResult<MongoClientUpdateResult>> asyncResultHandler, boolean multi) {
        JsonObject query = new JsonObject()
                .put(DbIdentifier.MESSAGE_READ_FLD.get(), false)
                .put(DbIdentifier.MESSAGE_SENDER_FLD.get(), contact)
                .put(DbIdentifier.MESSAGE_RECEIVER_FLD.get(), source);
        JsonObject update = new JsonObject().put("$set", new JsonObject().put(DbIdentifier.MESSAGE_READ_FLD.get(), true));
        UpdateOptions options = new UpdateOptions().setMulti(multi);

        mongoClient.updateCollectionWithOptions(DbIdentifier.MESSAGE_COL.get(), query, update, options, asyncResultHandler);
    }

    /**
     * Created by ch on 04.09.2015.
     */
    public enum DbIdentifier {
        USER_COL("user"),
        USER_ID_FLD("_id"),
        USER_NAME_FLD("username"),
        USER_ROLES_FLD("roles"),
        USER_PERMISSIONS_FLD("permissions"),
        USER_SALT_FLD("salt"),
        USER_PASSWORD_FLD("password"),
        USER_CONTACTS_MAP("contactList"),
        USER_CONTACTS_NAME_FLD("name"),
        USER_CONTACTS_UNREADMESSAGES_FLD("unreadMessages"),

        MESSAGE_COL("message"),
        MESSAGE_ID_FLD("_id"),
        MESSAGE_SENDER_FLD("sender"),
        MESSAGE_RECEIVER_FLD("receiver"),
        MESSAGE_MESSAGE_FLD("message"),
        MESSAGE_READ_FLD("read"),
        MESSAGE_DATE_FLD("date");

        private String identifier;

        DbIdentifier(String identifier) {
            this.identifier = identifier;
        }

        public String get() {
            return identifier;
        }

        @Override
        public String toString() {
            return get();
        }
    }
}
