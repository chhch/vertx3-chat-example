package de.hbrs.vertxChat.verticles.website;

import de.hbrs.vertxChat.verticles.enums.EventBusAddresses;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.mongo.MongoAuth;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.handler.sockjs.BridgeOptions;
import io.vertx.ext.web.handler.sockjs.PermittedOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.templ.HandlebarsTemplateEngine;

import java.util.LinkedList;
import java.util.List;


/**
 * Manages the website access.
 *
 * Created by ch on 31.08.2015.
 */
public class WebsiteVerticle extends AbstractVerticle {

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);
        MongoClient mongoClient = MongoClient.createShared(vertx, new JsonObject());
        MongoAuth authProvider = MongoAuth.create(mongoClient, new JsonObject());

        // We need cookies, sessions, request bodies for authentication/authorisation
        // And user session handler, to make sure the user is stored in the session between requests
        router.route().handler(CookieHandler.create());
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
        router.route().handler(BodyHandler.create());
        router.route().handler(UserSessionHandler.create(authProvider));

        router.route("/chat/*").handler(RedirectAuthHandler.create(authProvider, "/login.hbs"));
        router.route("/chat/chat.hbs").handler(TemplateContextHandler.create()); // Put data to the context
        router.routeWithRegex(".+\\.hbs").handler(TemplateHandler.create(HandlebarsTemplateEngine.create()));
        // e.g. chat/chat.hbs -> chat/chat (path, param0) '\\' .hbs (extension, param1)

        router.route("/logout").handler(LogoutHandler.create());
        router.route("/registerhandler").handler(FormRegisterHandler.create(authProvider));
        router.routeWithRegex("/registerhandler|/loginhandler").handler( // login and auto. login after FormRegisterHandler
                FormLoginHandler.create(authProvider).setDirectLoggedInOKURL("/chat/chat.hbs"));

        router.route("/eventbus/*").handler(SockJSHandler.create(vertx).bridge(getBrideOptions(), BridgeEventHandler.create()));
        router.route().failureHandler(ErrorHandler.create());
        router.route().handler(StaticHandler.create());

//        router.route().handler(ctx -> {
//            // we define a hardcoded title for our application
//            ctx.put("name", "Vert.x Web");
//
//            // and now delegate to the engine to render it.
//            HandlebarsTemplateEngine.create().render(ctx, "webroot/login.hbs", res -> {
//                if (res.succeeded()) {
//                    ctx.response().end(res.result());
//                } else {
//                    ctx.fail(res.cause());
//                }
//            });
//        });

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    private BridgeOptions getBrideOptions() {
        List<PermittedOptions> allowedInComings = new LinkedList<>();
        List<PermittedOptions> allowedOutComings = new LinkedList<>();
        for (EventBusAddresses address : EventBusAddresses.values()) {
            switch (address.getDirection()) {
                case TO_SERVER:
                    allowedInComings.add(new PermittedOptions()
                            .setAddress(address.get())
                            .setRequiredAuthority(MongoAuth.ROLE_PREFIX + "user"));
                    break;
                case TO_CLIENT:
                    allowedOutComings.add(new PermittedOptions()
                            .setAddressRegex(address.get() + ".*")
                            .setRequiredAuthority(MongoAuth.ROLE_PREFIX + "user"));
                    break;
                case BOTH:
                    throw (new UnsupportedOperationException("BOTH not implemented yet!"));
                case NONE:
                    break;
            }
        }

        BridgeOptions opts = new BridgeOptions().setPingTimeout(Long.MAX_VALUE).setReplyTimeout(Long.MAX_VALUE); // TODO: only for testing
        opts.setInboundPermitted(allowedInComings);
        opts.setOutboundPermitted(allowedOutComings);
        return opts;
    }
}
