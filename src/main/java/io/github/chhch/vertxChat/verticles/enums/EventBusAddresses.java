package io.github.chhch.vertxChat.verticles.enums;

/**
 * Contains the addresses, which client and server use.
 * <p>Address names of client point of view, e.g. CHAT_ADD_CONTACT means the client added contact.</p>
 * <ul>
 * <li>GET: Client send a query, server reply data</li>
 * <li>SEND/ADD: Client send data, server may reply something</li>
 * <li>RECEIVE: Server send Client data</li>
 * </ul>
 * Created by ch on 31.08.2015.
 */
public enum EventBusAddresses {
    CHAT_ADD_CONTACT("chat.add.contact", CommunicationDirection.TO_SERVER),
    CHAT_RECEIVE_CONTACT("chat.receive.contact", CommunicationDirection.TO_CLIENT),
    CHAT_GET_CONTACT_MAP("chat.get.contactList", CommunicationDirection.TO_SERVER),
    CHAT_SEND_MESSAGE("chat.send.message", CommunicationDirection.TO_SERVER),
    CHAT_RECEIVE_MESSAGE("chat.receive.message", CommunicationDirection.TO_CLIENT),
    CHAT_SEND_READ_NOTIFICATION("chat.send.read.notification", CommunicationDirection.TO_SERVER),
    CHAT_RECEIVE_READ_NOTIFICATION("chat.receive.read.notification", CommunicationDirection.TO_CLIENT),
    CHAT_GET_CONVERSATION("chat.get.conversation", CommunicationDirection.TO_SERVER);

    private String address;
    private CommunicationDirection direction;

    EventBusAddresses(String address, CommunicationDirection direction) {
        this.address = address;
        this.direction = direction;
    }

    public String get() {
        return address;
    }

    public CommunicationDirection getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return get();
    }

    public enum CommunicationDirection {
        TO_SERVER, TO_CLIENT, BOTH, NONE
    }
}

