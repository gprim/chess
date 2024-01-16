package server;

import models.AuthToken;
import org.eclipse.jetty.websocket.api.Session;

import java.util.Objects;

record Connection(AuthToken authToken, Session session, int gameID) {
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Connection that=(Connection) o;
    return gameID == that.gameID && authToken.equals(that.authToken) && session.equals(that.session);
  }

  @Override
  public int hashCode() {
    return Objects.hash(authToken, session, gameID);
  }
}