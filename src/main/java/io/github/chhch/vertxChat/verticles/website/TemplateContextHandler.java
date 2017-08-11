package io.github.chhch.vertxChat.verticles.website;

import io.github.chhch.vertxChat.verticles.enums.EventBusAddresses;
import io.github.chhch.vertxChat.verticles.enums.JsonKeys;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;

/**
 * Created by ch on 04.09.2015.
 */
class TemplateContextHandler implements Handler<RoutingContext> {

    public TemplateContextHandler() {
    }

    /**
     * Create a handler
     *
     * @return the handler
     */
    public static TemplateContextHandler create() {
        return new TemplateContextHandler();
    }

    @Override
    public void handle(RoutingContext routingContext) {
//        routingContext.put("request_path", routingContext.request().path());
//        routingContext.put("session_data", routingContext.session().data());

        String username = routingContext.user().principal().getString("username");
        routingContext.put(JsonKeys.USERNAME.get(), username);

        EventBus eventBus = routingContext.vertx().eventBus();
        DeliveryOptions options = new DeliveryOptions().addHeader(JsonKeys.SOURCE.get(), username);
        eventBus.send(EventBusAddresses.CHAT_GET_CONTACT_MAP.get(), null, options, reply -> {
            if (reply.succeeded()) {
                // The data from the RoutingContext is stored in a HashMap. Thus Handlebar uses the MapValueResolver to
                //  resolve the data from the RoutingContext. Because the Resolver don't change while resolving, all the
                //  data added to the RoutingContext <b>must</b> also be a Map or a identifier. Otherwise Handlebar can't
                //  resolve it. (@see Context#build()). Don't use JsonNode!
                String contactMapAsJson = reply.result().body().toString();
                HashMap contactMap = Json.decodeValue(contactMapAsJson, HashMap.class);
                routingContext.put(JsonKeys.USER_DATA.get(), contactMap);
            }

            routingContext.next();
        });
    }
}