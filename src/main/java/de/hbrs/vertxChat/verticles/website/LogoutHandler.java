package de.hbrs.vertxChat.verticles.website;


import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;


class LogoutHandler implements Handler<RoutingContext> {

    /**
     * Create a handler
     *
     * @return the handler
     */
    public static LogoutHandler create() {
        return new LogoutHandler();
    }

    public LogoutHandler() {
    }

    @Override
    public void handle(RoutingContext context) {
        context.clearUser();
        context.response().putHeader("location", "/").setStatusCode(302).end();
    }
}
