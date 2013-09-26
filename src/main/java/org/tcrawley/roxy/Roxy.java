package org.tcrawley.roxy;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import java.util.UUID;

public class Roxy {
    public static final String REGISTRATION_ADDRESS = "roxy.registrations"; //TODO: should be dynamic to support multiple routers

    public static void registerEndpoint(Vertx vertx, String route, Handler<HttpServerRequest> handler) {
        String endpointAddress = "roxy.endpoint." + UUID.randomUUID().toString();
        EventBus eb = vertx.eventBus();
        eb.registerHandler(endpointAddress, new EndpointHttpHandler(vertx, handler));
        JsonObject msg = new JsonObject();
        msg.putString("route", route);
        msg.putString("endpointAddress", endpointAddress);
        eb.publish(REGISTRATION_ADDRESS, msg);

    }
}
