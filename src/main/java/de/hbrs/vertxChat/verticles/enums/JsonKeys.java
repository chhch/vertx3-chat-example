package de.hbrs.vertxChat.verticles.enums;

/**
 * Created by ch on 07.09.2015.
 */
public enum JsonKeys {
    SOURCE("_source"),
    SENDER("sender"),
    RECEIVER("receiver"),
    CONTACT("contact"),
    MESSAGE("message"),
    READ("read"),
    DATE("date"),
    CONVERSATION("conversation"),
    ONLY_UNREAD("onlyUnread"),
    MESSAGE_COUNT("count"),
    STATUS("status"),
    STATUS_MESSAGE("statusMessage"),
    USERNAME("username"),
    USER_DATA("userData");

    private String key;

    JsonKeys(String key) {
        this.key = key;
    }

    public String get() {
        return key;
    }

    @Override
    public String toString() {
        return get();
    }

    public enum Status {
        SUCCESS("success"),
        INFO("info"),
        WARNING("warning"),
        DANGER("danger");

        private String key;

        Status(String key) {
            this.key = key;
        }

        public String get() {
            return key;
        }

        @Override
        public String toString() {
            return get();
        }
    }
}
