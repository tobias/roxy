package org.tcrawley.integration.java;
/*
 * Copyright 2013 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * @author <a href="http://tfox.org">Tim Fox</a>
 */

import org.junit.Test;
import org.tcrawley.roxy.Router;
import org.tcrawley.roxy.Roxy;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.AsyncResultHandler;
import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

/**
 * Simple integration test which shows tests deploying other verticles, using the Vert.x API etc
 */
public class BasicIntegrationTest extends TestVerticle {

  @Test
  public void testRouter() {

      Router router = new Router(vertx);

      Roxy.registerEndpoint(vertx, "/foo", new Handler<HttpServerRequest>() {
          @Override
          public void handle(HttpServerRequest event) {
              event.response()
                      .putHeader("Content-Length", "2")
                      .end("HI");
          }
      });

    vertx.createHttpServer().requestHandler(router)
      .listen(8383, new AsyncResultHandler<HttpServer>() {
          @Override
          public void handle(AsyncResult<HttpServer> asyncResult) {
              assertTrue(asyncResult.succeeded());
              // The server is listening so send an HTTP request
              vertx.createHttpClient().setPort(8383).getNow("/foo", new Handler<HttpClientResponse>() {
                  @Override
                  public void handle(HttpClientResponse resp) {
                      assertEquals(200, resp.statusCode());
                      resp.bodyHandler(new Handler<Buffer>() {
                          @Override
                          public void handle(Buffer event) {
                              assertEquals("HI", event.toString());
                          }
                      });

                      testComplete();
                  }
              });
          }
      });
  }


    @Test
    public void testHTTP() {

        vertx.createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
            @Override
            public void handle(HttpServerRequest event) {
                event.response()
                        .putHeader("Content-Length", "2")
                        .write("HI")
                        .end();
            }
        })
                .listen(8383, new AsyncResultHandler<HttpServer>() {
                    @Override
                    public void handle(AsyncResult<HttpServer> asyncResult) {
                        assertTrue(asyncResult.succeeded());
                        // The server is listening so send an HTTP request
                        vertx.createHttpClient().setPort(8383).getNow("/", new Handler<HttpClientResponse>() {
                            @Override
                            public void handle(HttpClientResponse resp) {
                                assertEquals(200, resp.statusCode());
                                resp.bodyHandler(new Handler<Buffer>() {
                                    @Override
                                    public void handle(Buffer event) {
                                        assertEquals("HI", event.toString());
                                    }
                                });

                                testComplete();
                            }
                        });
                    }
                });
    }
}
