package de.hbrs.vertxChat.verticles.website;

import io.vertx.core.Handler;
import io.vertx.core.MultiMap;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.web.RoutingContext;

import java.util.Arrays;

/**
 * @see io.vertx.ext.web.handler.impl.FormLoginHandlerImpl
 */
class FormRegisterHandler implements Handler<RoutingContext> {

    private static final Logger log = LoggerFactory.getLogger(FormRegisterHandler.class);
    private final MongoAuth authProvider;
    /**
     * The default identifier of the form attribute which will contain the username
     */
    String DEFAULT_USERNAME_PARAM = "username";
    /**
     * The default identifier of the form attribute which will contain the password
     */
    String DEFAULT_PASSWORD_PARAM = "password";

    public FormRegisterHandler(MongoAuth authProvider) {
        this.authProvider = authProvider;
    }

    /**
     * Create a handler
     *
     * @param authProvider the auth service to use
     * @return the handler
     */
    public static FormRegisterHandler create(MongoAuth authProvider) {
        return new FormRegisterHandler(authProvider);
    }

    @Override
    public void handle(RoutingContext context) {
        HttpServerRequest req = context.request();
        if (req.method() != HttpMethod.POST) {
            context.fail(405); // Must be a POST
        } else {
            if (!req.isExpectMultipart()) {
                throw new IllegalStateException("Form body not parsed - do you forget to include a BodyHandler?");
            }
            MultiMap params = req.formAttributes();
            String username = params.get(DEFAULT_USERNAME_PARAM);
            String password = params.get(DEFAULT_PASSWORD_PARAM);
            if (username == null || password == null) {
                log.warn("No username or password provided in form - did you forget to include a BodyHandler?");
                context.fail(400);
            } else {
                authProvider.insertUser(username, password, Arrays.asList("user"), Arrays.asList(), event -> {
                    if (event.succeeded()) {
                        params.set(DEFAULT_USERNAME_PARAM, username);
                        context.next();
                    } else {
                        context.fail(403);  // Failed login
                    }
                });
            }
        }
    }
}
