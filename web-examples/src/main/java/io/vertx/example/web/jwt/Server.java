package io.vertx.example.web.jwt;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.json.JsonObject;
import io.vertx.example.util.Runner;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.jwt.JWTAuth;
import io.vertx.ext.auth.jwt.JWTOptions;
import io.vertx.ext.auth.shiro.ShiroAuth;
import io.vertx.ext.auth.shiro.ShiroAuthRealmType;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.LocalSessionStore;

/*
 * @author <a href="mailto:pmlopes@gmail.com">Paulo Lopes</a>
 */
public class Server extends AbstractVerticle {

  // Convenience method so you can run it in your IDE
  public static void main(String[] args) {
    Runner.runExample(Server.class);
  }

  @Override
  public void start() throws Exception {

    Router router = Router.router(vertx);

    // Create a JWT Auth Provider
    JWTAuth jwt = JWTAuth.create(new JsonObject()
      .put("keyStoreType", "jceks")
      .put("keyStoreURI", "classpath:///keystore.jceks")
      .put("keyStorePassword", "secret"));

    // protect the API
    router.route("/api/*").handler(JWTAuthHandler.create(jwt, "/api/newToken"));

    // this route is excluded from the auth handler
    router.get("/api/newToken").handler(ctx -> {
      ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
      ctx.response().end(jwt.generateToken(new JsonObject(), new JWTOptions().setExpiresInSeconds(60)));
    });

    // this is the secret API
    router.get("/api/protected").handler(ctx -> {
      ctx.response().putHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
      ctx.response().end("a secret you should keep for yourself...");
    });

    // Serve the non private static pages
    router.route().handler(StaticHandler.create());

    vertx.createHttpServer().requestHandler(router::accept).listen(8080);
  }
}

