package com.sforca.rinha_backend

import com.sforca.rinha_backend.router.restApi
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise

class MainVerticle : AbstractVerticle() {

  override fun start(startPromise: Promise<Void>) {
    vertx
      .createHttpServer()
      .requestHandler(restApi)
      .listen(9999) { http ->
        if (http.succeeded()) {
          startPromise.complete()
          println("HTTP server started on port 9999")
        } else {
          startPromise.fail(http.cause());
        }
      }
  }
}
