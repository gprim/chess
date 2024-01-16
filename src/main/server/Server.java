package server;

import com.google.gson.Gson;
import dataAccess.DataAccessException;
import dataAccess.DatabaseAccess;
import dataAccess.MySqlDAO;
import models.AuthToken;
import models.Game;
import models.GameInfo;
import models.User;
import services.AuthService;
import services.GameService;
import services.TestingService;
import services.UserService;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import static spark.Spark.*;

public class Server {
  AuthService authService;
  UserService userService;
  GameService gameService;
  TestingService testingService;
  DatabaseAccess dao;

  public Server() {
    try {
      dao=new MySqlDAO();

      authService=new AuthService(dao);
      userService=new UserService(dao);
      gameService=new GameService(dao);
      testingService=new TestingService(dao);
    } catch (DataAccessException e) {
      System.out.println(e.getMessage());
    }

    webSocket("/connect", WebsocketHandler.class);
    
    exception(Exception.class, this::errorHandler);

    externalStaticFileLocation("web");

    post("/user", this::registerUser);

    post("/session", this::login);
    delete("/session", this::logout);

    get("/game", this::listGames);
    post("/game", this::createGame);
    put("/game", this::joinGame);

    delete("/db", this::clear);

  }


  public static void main(String[] args) {
    port(8080);
    System.out.println("Starting server...");
    var server=new Server();
    server.start();
    System.out.println("Server started.");
    System.out.println("Waiting for requests...");
  }

  public void start() {
    // JUST SHUT UP SOLAR LINT
  }

  private Object registerUser(Request req, Response res) {
    var user=new Gson().fromJson(req.body(), User.class);
    AuthToken authToken;
    try {
      authToken=userService.registerUser(user);
      res.status(200);
      res.body(toJSON(authToken));
      return toJSON(authToken);
    } catch (DataAccessException err) {
      return databaseErrorHandler(err, req, res);
    }
  }

  private Object login(Request req, Response res) {
    var user=new Gson().fromJson(req.body(), User.class);
    AuthToken authToken;
    try {
      authToken=authService.login(user);
      res.status(200);
      res.body(toJSON(authToken));
      return toJSON(authToken);
    } catch (DataAccessException err) {
      return databaseErrorHandler(err, req, res);
    }
  }

  private Object logout(Request req, Response res) {
    String authTokenString=req.headers().contains("authorization") ? req.headers("authorization") : req.headers("Authorization");
    AuthToken authToken=new AuthToken(authTokenString, "");
    try {
      authService.logout(authToken);
      res.status(200);
      return "{}";
    } catch (DataAccessException err) {
      return databaseErrorHandler(err, req, res);
    }
  }

  private Object listGames(Request req, Response res) {
    String authTokenString=req.headers().contains("authorization") ? req.headers("authorization") : req.headers("Authorization");
    AuthToken authToken=new AuthToken(authTokenString, "");
    try {
      var games=gameService.listGames(authToken);
      ArrayList<GameInfo> gameInfos=new ArrayList<>();
      for (var game : games) {
        gameInfos.add(GameInfo.fromGame(game));
      }
      res.status(200);
      return toJSON(Collections.singletonMap("games", gameInfos));
    } catch (DataAccessException err) {
      return databaseErrorHandler(err, req, res);
    }
  }

  private Object createGame(Request req, Response res) {
    String authTokenString=req.headers().contains("authorization") ? req.headers("authorization") : req.headers("Authorization");
    AuthToken authToken=new AuthToken(authTokenString, "");
    try {
      var game=gameService.createGame(authToken, new Gson().fromJson(req.body(), Game.class));
      res.status(200);
      return toJSON(Collections.singletonMap("gameID", game.gameID()));
    } catch (DataAccessException err) {
      return databaseErrorHandler(err, req, res);
    }
  }

  private Object joinGame(Request req, Response res) {
    String authTokenString=req.headers().contains("authorization") ? req.headers("authorization") : req.headers("Authorization");
    AuthToken authToken=new AuthToken(authTokenString, "");
    try {
      var body=new Gson().fromJson(req.body(), HashMap.class);
      int gameID=(int) Math.round((Double) body.get("gameID"));
      var playerColor=(String) body.get("playerColor");
      var white="WHITE".equals(playerColor) ? playerColor : null;
      var black="BLACK".equals(playerColor) ? playerColor : null;
      var game=new Game(gameID, white, black, null, null);
      gameService.joinGame(authToken, game);
      res.status(200);
      return "{}";
    } catch (DataAccessException err) {
      return databaseErrorHandler(err, req, res);
    }
  }

  private Object clear(Request request, Response response) {
    try {
      testingService.clear();
    } catch (Exception err) {
      return errorHandler(err, request, response);
    }
    response.status(200);
    response.body("{}");
    return "{}";
  }

  private Object errorHandler(Exception err, Request req, Response res) {
    var body=getJSONError(err.getMessage());
    res.type("application/json");
    res.status(500);
    res.body(body);
    return body;
  }

  private Object databaseErrorHandler(DataAccessException err, Request req, Response res) {
    int status;
    var body=getJSONError(err.getMessage());

    switch (err.getMessage()) {
      case "unauthorized" -> status=401;
      case "already taken" -> status=403;
      default -> status=400;
    }

    res.type("application/json");
    res.body(body);
    res.status(status);
    return body;
  }

  private String getJSONError(String message) {
    return new Gson().toJson(new ErrorResponse(message));
  }

  private String toJSON(Object obj) {
    return new Gson().toJson(obj);
  }

  private record ErrorResponse(String message) {
    ErrorResponse(String message) {
      this.message="Error: " + message;
    }
  }
}
