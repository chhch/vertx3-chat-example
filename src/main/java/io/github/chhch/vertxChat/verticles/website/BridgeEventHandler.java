package io.github.chhch.vertxChat.verticles.website;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.handler.sockjs.BridgeEvent;

/**
 * {@link WebsiteVerticle#getBrideOptions()}
 */
class BridgeEventHandler implements Handler<BridgeEvent> {

    public BridgeEventHandler() {
    }

    /**
     * Create a handler
     *
     * @return the handler
     */
    public static BridgeEventHandler create() {
        return new BridgeEventHandler();
    }

    /**
     * Enhanced message header with the name of the user, which <b>sent</b> the messages.
     * <p>
     * { headers : { _source : <username> } }
     *
     * @param bridgeEvent event to handle
     */
    @Override
    public void handle(BridgeEvent bridgeEvent) {
        User user = bridgeEvent.socket().webUser();
        String username = user.principal().getString("username");

        switch (bridgeEvent.type()) {
            case REGISTER:
                String address = bridgeEvent.getRawMessage().getString("address");
                if (!address.endsWith(username)) {
                    bridgeEvent.complete(false);
                    break;
                }
            case SEND:
            case PUBLISH:
                bridgeEvent.getRawMessage().put("headers", new JsonObject().put("_source", username));
            default:
                bridgeEvent.complete(true);
        }
    }
}
