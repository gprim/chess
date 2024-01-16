package serverFacade;

import com.google.gson.Gson;
import models.AuthToken;
import models.Game;
import models.GameInfo;
import models.User;
import ui.ClientException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ServerFacade {
  final String urlString;
  final int port;

  public ServerFacade(String urlString, int port) {
    this.urlString=urlString;
    this.port=port;
  }

  public ServerFacade() {
    this.urlString="localhost";
    this.port=8080;
  }

  public void clear() throws ClientException {
    makeRequest("DELETE", "db", null, null, null);
  }

  public AuthToken registerUser(User user) throws ClientException {
    return makeRequest("POST", "user", user, AuthToken.class, null);
  }

  public AuthToken login(User user) throws ClientException {
    return makeRequest("POST", "session", user, AuthToken.class, null);
  }

  public void logout(AuthToken authToken) throws ClientException {
    makeRequest("DELETE", "session", null, null, authToken);
  }

  public List<Game> listGames(AuthToken authToken) throws ClientException {
    var games=makeRequest("GET", "game", null, GamesList.class, authToken);
    return games.games().stream().map(GameInfo::toGame).toList();
  }

  public Game createGame(AuthToken authToken, String gameName) throws ClientException {
    return makeRequest("POST", "game", new Game(gameName), Game.class, authToken);
  }

  public void joinGame(AuthToken authToken, int gameID, String playerColor) throws ClientException {
    makeRequest("PUT", "game", new JoinGameRequest(gameID, playerColor), null, authToken);
  }

  public <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, AuthToken authToken) throws ClientException {
    try {
      var connection=getConnection(path);
      connection.setRequestMethod(method);
      connection.setReadTimeout(5000);
      if (authToken != null) connection.addRequestProperty("Authorization", authToken.authToken());

      if (request != null) sendData(request, connection);

      if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
        var response=readData(connection, ErrorResponse.class);
        throw new ClientException(connection.getResponseCode(), response.message());
      }

      if (responseClass == null) return null;

      return readData(connection, responseClass);
    } catch (IOException ex) {
      throw new ClientException(HttpURLConnection.HTTP_BAD_REQUEST, ex.getMessage());
    }
  }

  private HttpURLConnection getConnection(String path) throws ClientException, IOException {
    var url=getURL(path);

    return (HttpURLConnection) url.openConnection();
  }

  private URL getURL(String path) throws ClientException {
    try {
      return new URI("http://" + urlString + ":" + port + (path == null ? "" : ("/" + path))).toURL();
    } catch (URISyntaxException | MalformedURLException ex) {
      throw new ClientException(400, ex.getMessage());
    }
  }

  private void sendData(Object data, HttpURLConnection connection) throws ClientException {
    try {
      connection.setDoOutput(true);
      connection.addRequestProperty("Content-Type", "application/json");
      var os=connection.getOutputStream();
      byte[] input=new Gson().toJson(data).getBytes(StandardCharsets.UTF_8);
      os.write(input, 0, input.length);
    } catch (IOException ex) {
      throw new ClientException(400, ex.getMessage());
    }
  }


  public <T> T readData(HttpURLConnection connection, Class<T> classOfT) throws ClientException {
    try {
      InputStream responseBody;
      try {
        responseBody=connection.getInputStream();
      } catch (IOException ignored) {
        responseBody=connection.getErrorStream();
      }
      var reader=new BufferedReader(new InputStreamReader(responseBody));
      var response=new StringBuilder();
      String line;

      while ((line=reader.readLine()) != null) {
        response.append(line);
      }
      reader.close();
      responseBody.close();

      return new Gson().fromJson(response.toString(), classOfT);
    } catch (IOException ex) {
      throw new ClientException(HttpURLConnection.HTTP_BAD_REQUEST, ex.getMessage());
    }
  }

  private record ErrorResponse(String message) {
  }

  private record GamesList(ArrayList<GameInfo> games) {
  }

  private record JoinGameRequest(int gameID, String playerColor) {
  }
}
