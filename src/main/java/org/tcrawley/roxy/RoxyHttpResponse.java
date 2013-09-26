package org.tcrawley.roxy;

import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.impl.CaseInsensitiveMultiMap;
import org.vertx.java.core.json.JsonObject;

public class RoxyHttpResponse implements HttpServerResponse {

    public RoxyHttpResponse(Vertx vertx, String responseAddress, HttpServerRequest request) {
        this.vertx = vertx;
        this.responseAddress = responseAddress;
        this.request = request;
    }

    @Override
    public HttpServerResponse exceptionHandler(Handler<Throwable> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStatusCode() {
        return this.statusCode;
    }

    @Override
    public HttpServerResponse setStatusCode(int statusCode) {
        this.statusCode = statusCode;

        return this;
    }

    @Override
    public String getStatusMessage() {
        return this.statusMessage;
    }

    @Override
    public HttpServerResponse setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;

        return this;
    }

    @Override
    public HttpServerResponse setChunked(boolean chunked) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isChunked() {
        throw new UnsupportedOperationException();
    }

    @Override
    public MultiMap headers() {
        if (this.headers == null) {
            this.headers = request.headers();
        }

        return this.headers;
    }

    @Override
    public HttpServerResponse putHeader(String name, String value) {
        headers().add(name, value);

        return this;
    }

    @Override
    public HttpServerResponse putHeader(String name, Iterable<String> values) {
        headers().add(name, values);

        return this;
    }

    @Override
    public MultiMap trailers() {
        if (this.trailers == null) {
            this.trailers = new CaseInsensitiveMultiMap();
        }

        return this.trailers;
    }

    @Override
    public HttpServerResponse putTrailer(String name, String value) {
        trailers().add(name, value);

        return this;
    }

    @Override
    public HttpServerResponse putTrailer(String name, Iterable<String> values) {
        trailers().add(name, values);

        return this;
    }

    @Override
    public HttpServerResponse closeHandler(Handler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse write(Buffer chunk) {
        sendMeta();
        sendData(chunk);

        return this;
    }

    @Override
    public HttpServerResponse setWriteQueueMaxSize(int maxSize) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean writeQueueFull() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse drainHandler(Handler<Void> handler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse write(String chunk, String enc) {
        sendMeta();
        sendData(new Buffer(chunk, enc));

        return this;
    }

    @Override
    public HttpServerResponse write(String chunk) {
        sendMeta();
        sendData(new Buffer(chunk));

        return this;
    }

    @Override
    public void end(String chunk) {
        write(chunk);
        sendEnd();
    }

    @Override
    public void end(String chunk, String enc) {
        write(chunk, enc);
        sendEnd();
    }

    @Override
    public void end(Buffer chunk) {
        write(chunk);
        sendEnd();
    }

    @Override
    public void end() {
        sendMeta();
        sendEnd();
    }

    @Override
    public HttpServerResponse sendFile(String filename) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerResponse sendFile(String filename, String notFoundFile) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException();
    }

    private void sendMeta() {
        if (!metaSent) {
            JsonObject meta = new JsonObject();
            meta.putNumber("statusCode", this.getStatusCode());
            meta.putArray("headers", Util.serializeMultiMap(this.headers()));
            //TODO: other fields

            this.vertx.eventBus().send(this.responseAddress, meta);
            this.metaSent = true;
        }
    }

    private void sendData(Buffer data) {
        this.vertx.eventBus().send(this.responseAddress, data);
    }

    private void sendEnd() {
        JsonObject msg = new JsonObject();
        msg.putString("command", "end");
        this.vertx.eventBus().send(this.responseAddress, msg);
    }

    private boolean metaSent = false;
    private Vertx vertx;
    private String responseAddress;
    private HttpServerRequest request;
    private int statusCode = 200;
    private String statusMessage = "OK";
    private MultiMap headers;
    private MultiMap trailers;
}
