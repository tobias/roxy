package org.tcrawley.roxy;


import org.vertx.java.core.Handler;
import org.vertx.java.core.MultiMap;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServerFileUpload;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.HttpServerResponse;
import org.vertx.java.core.http.HttpVersion;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.net.NetSocket;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.security.cert.X509Certificate;
import java.net.InetSocketAddress;
import java.net.URI;

public class RoxyHttpRequest implements HttpServerRequest {
    public RoxyHttpRequest(Vertx vertx, JsonObject base, EndpointHttpHandler wrappingHandler) {
        this.vertx = vertx;
        this.version = base.getInteger("version") == 0 ? HttpVersion.HTTP_1_0 : HttpVersion.HTTP_1_1;
        this.method = base.getString("method");
        this.uri = base.getString("uri");
        this.path = base.getString("path");
        this.query = base.getString("query");
        this.headers = Util.deserializeMultiMap(base.getArray("headers"));
        this.params = Util.deserializeMultiMap(base.getArray("params"));
        this.formAttributes = Util.deserializeMultiMap(base.getArray("formAttributes"));
        this.responseAddress = base.getString("responseAddress");
        this.wrapper = wrappingHandler;

    }

    public HttpVersion version() {
        return this.version;
    }

    public String method() {
        return this.method;
    }

    public String uri() {
        return this.uri;
    }

    public String path() {
        return this.path;
    }

    public String query() {
        return this.query;
    }

    public HttpServerResponse response() {
        if (this.response == null) {
            this.response = new RoxyHttpResponse(this.vertx, this.responseAddress, this);
        }

        return this.response;
    }

    public MultiMap headers() {
        return this.headers;
    }

    public MultiMap params() {
        return this.params;
    }

    public MultiMap formAttributes() {
        return this.formAttributes;
    }

    @Override
    public InetSocketAddress remoteAddress() {
        throw new UnsupportedOperationException();
    }

    @Override
    public X509Certificate[] peerCertificateChain() throws SSLPeerUnverifiedException {
        throw new UnsupportedOperationException();
    }

    @Override
    public URI absoluteURI() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerRequest bodyHandler(Handler<Buffer> bodyHandler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public NetSocket netSocket() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerRequest expectMultiPart(boolean expect) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerRequest uploadHandler(Handler<HttpServerFileUpload> uploadHandler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerRequest dataHandler(Handler<Buffer> handler) {
        this.wrapper.dataHandler(handler);
        return this;
    }

    @Override
    public HttpServerRequest pause() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerRequest resume() {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerRequest endHandler(Handler<Void> endHandler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public HttpServerRequest exceptionHandler(Handler<Throwable> handler) {
        throw new UnsupportedOperationException();
    }

    private Vertx vertx;
    private HttpVersion version;
    private String method;
    private String uri;
    private String path;
    private String query;
    private MultiMap headers;
    private MultiMap params;
    private MultiMap formAttributes;
    private String responseAddress;
    private EndpointHttpHandler wrapper;
    private HttpServerResponse response;
}
