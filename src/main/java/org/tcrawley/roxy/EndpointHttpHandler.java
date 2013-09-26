package org.tcrawley.roxy;

import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.eventbus.impl.JsonObjectMessage;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;

import javax.activation.DataHandler;

public class EndpointHttpHandler implements Handler<Message<JsonObject>> {

    public EndpointHttpHandler(Vertx vertx, Handler<HttpServerRequest> handler) {
        this.vertx = vertx;
        this.handler = handler;
    }

    public void handle(Message<JsonObject> msg) {
        JsonObject body = msg.body();
        this.handler.handle(new RoxyHttpRequest(this.vertx, body, this));
        this.vertx.eventBus()
                .registerHandler(body.getString("dataAddress"),
                                 new Handler<Message<Buffer>>() {
                                     @Override
                                     public void handle(Message<Buffer> event) {
                                         handleData(event.body());
                                     }
                                 });
        msg.reply(true);
    }

    public void dataHandler(Handler<Buffer> handler) {
        this.dataHandler = handler;
        if (this.queuedData.length() > 0) {
            handler.handle(this.queuedData);
        }
        this.queuedData = null;
    }

    private void handleData(Buffer data) {
        if (this.dataHandler != null) {
            this.dataHandler.handle(data);
        } else {
            queuedData.appendBuffer(data);
        }
    }

    private Vertx vertx;
    private Handler<HttpServerRequest> handler;
    private Handler<Buffer> dataHandler;
    private Buffer queuedData = new Buffer();
}
