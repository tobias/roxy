package org.tcrawley.roxy;

import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.HttpVersion;
import org.vertx.java.core.http.RouteMatcher;
import org.vertx.java.core.impl.CaseInsensitiveMultiMap;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class Router implements Handler<HttpServerRequest> {


    public Router(Vertx vertx) {
        this.vertx = vertx;
        this.routeMatcher = new RouteMatcher();
        init();
    }

    private void init() {
        this.vertx.eventBus().registerHandler(Roxy.REGISTRATION_ADDRESS, new Handler<Message<JsonObject>>() {
            @Override
            public void handle(Message<JsonObject> message) {
                JsonObject body = message.body();
                //message should be: route, vhost, expects multipart?, address
                routeMatcher.all(body.getString("route"),
                                 new RoutingHandler(body.getString("endpointAddress")));
            }
        });
    }

    @Override
    public void handle(HttpServerRequest event) {
        this.routeMatcher.handle(event);
    }

    private Vertx vertx;
    private RouteMatcher routeMatcher;

    class RoutingHandler implements Handler<HttpServerRequest> {

        public RoutingHandler(String endpointAddress) {
            this.endpointAddress = endpointAddress;
        }

        @Override
        public void handle(final HttpServerRequest request) {
            //request.expectMultiPart(true);
            String id = UUID.randomUUID().toString();
            String responseAddress = this.endpointAddress + ".response." + id;
            final String dataAddress = this.endpointAddress + ".data." + id;

            JsonObject meta = new JsonObject();
            meta.putNumber("version", (request.version() == HttpVersion.HTTP_1_0 ? 0 : 1));
            meta.putString("method", request.method());
            meta.putString("uri", request.uri());
            meta.putString("path", request.path());
            meta.putString("query", request.query());
            meta.putArray("headers", Util.serializeMultiMap(request.headers()));
            meta.putArray("params", Util.serializeMultiMap(request.params()));
            meta.putArray("formAttributes", new JsonArray()); //Util.serializeMultiMap(request.formAttributes()));
            //TODO: remoteAddress, absoluteURI, peerCertificateChain?

            meta.putString("responseAddress", responseAddress);
            meta.putString("dataAddress", dataAddress);

            final EventBus eb = vertx.eventBus();

            eb.registerHandler(responseAddress, new ResponseHandler(request.response()));

            eb.send(this.endpointAddress, meta, new Handler<Message<Boolean>>() {
                @Override
                public void handle(Message<Boolean> event) {
                    //any response is fine
                    request.dataHandler(new Handler<Buffer>() {
                        @Override
                        public void handle(Buffer event) {
                            eb.send(dataAddress, event);
                        }
                    });
                }
            });

        }

        private String endpointAddress;
    }

    class ResponseHandler implements Handler<Message> {
        public ResponseHandler(HttpServerResponse response) {
            this.response = response;
        }

        @Override
        public void handle(Message event) {
            Object body = event.body();
            if (body instanceof JsonObject) {
                JsonObject meta = (JsonObject)body;
                if ("end".equals(meta.getString("command"))) {
                    this.response.end();
                } else {
                    this.response.setStatusCode(meta.getInteger("statusCode"));
                    this.response.headers().add(Util.deserializeMultiMap(meta.getArray("headers")));
                    //TODO: statusMessage, chunked, trailers, sendFile
                }
            } else if (body instanceof Buffer) {
                this.response.write((Buffer) body);
            } else {
                throw new IllegalStateException("Invalid data");
            }

        }

        private HttpServerResponse response;
    }
}

