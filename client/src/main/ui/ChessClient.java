package ui;

import chess.*;
import models.AuthToken;
import models.User;
import serverFacade.NotificationHandler;
import serverFacade.ServerFacade;
import serverFacade.WebsocketFacade;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import static ui.EscapeSequences.*;

public class ChessClient {
  private final ServerFacade server;
  private final HashMap<Integer, Integer> gameIDs;
  private final NotificationHandler notificationHandler;
  private WebsocketFacade ws;
  private State state;
  private AuthToken authToken;
  private GameImple currentGame;
  private ChessGame.TeamColor teamColor;
  private int gameID;

  public ChessClient(NotificationHandler notificationHandler) {
    server=new ServerFacade();
    state=State.SIGNED_OUT;
    gameIDs=new HashMap<>();
    this.notificationHandler=notificationHandler;
  }

  public State getState() {
    return state;
  }

  public String eval(String input) throws ClientException {
    var inputs=input.split("\\s+");

    if (inputs[0].length() == 0) return "";

    var params=Arrays.copyOfRange(inputs, 1, inputs.length);

    return switch (inputs[0]) {
      case "register" -> register(params);
      case "login" -> login(params);
      case "logout" -> logout();
      case "list" -> listGames();
      case "create" -> createGame(params);
      case "join" -> joinGame(params);
      case "observe" -> observeGame(params);
      case "help" -> help();
      case "clear" -> clear();
      case "redraw" -> redraw();
      case "resign" -> resign();
      case "leave" -> leave();
      case "move" -> move(params);
      case "highlight" -> highlight(params);
      case "quit" -> quit();
      case "info" -> info();
      default -> "Unknown command. Type 'help' to see all commands.";
    };
  }

  private String info() throws ClientException {
    return ws.toString();
  }

  private String quit() throws ClientException {
    if (ws == null) return "quit";

    ws.close();

    return "quit";
  }

  private String register(String... params) throws ClientException {
    if (params.length != 3) throw new ClientException(400, "Expected: <username> <password> <email>");

    authToken=server.registerUser(new User(params[0], params[1], params[2]));
    state=State.SIGNED_IN;
    return "Successfully registered and logged in!";
  }

  private String login(String... params) throws ClientException {
    if (params.length != 2) throw new ClientException(400, "Expected: <username> <password>");

    authToken=server.login(new User(params[0], params[1]));
    state=State.SIGNED_IN;
    return "Successfully logged in!";
  }

  private String logout() throws ClientException {
    assertSignedIn();

    server.logout(authToken);
    if (ws != null) ws.close();
    ws=null;
    state=State.SIGNED_OUT;
    return "Successfully logged out!";
  }

  private String listGames() throws ClientException {
    assertSignedIn();
    var games=server.listGames(authToken);
    gameIDs.clear();

    if (games.isEmpty()) return "No games.";

    var sb=new StringBuilder();

    var index=0;

    for (var game : games) {
      sb.append(++index);
      sb.append(") Name: '");
      sb.append(game.gameName());
      sb.append("'; White player: ");
      if (game.whiteUsername() != null) sb.append("'").append(game.whiteUsername()).append("'");
      else sb.append("Empty");
      sb.append("; Black player: ");
      if (game.blackUsername() != null) sb.append("'").append(game.blackUsername()).append("'");
      else sb.append("Empty");
      sb.append(";");
      if (index != games.size()) sb.append("\n");
      gameIDs.put(index, game.gameID());
    }

    return sb.toString();
  }

  private String clear() throws ClientException {
    server.clear();
    return "Successfully cleared the database!";
  }

  private String createGame(String... params) throws ClientException {
    assertSignedIn();

    if (params.length == 0) throw new ClientException(400, "Expected: create <gameName>");

    server.createGame(authToken, params[0]);
    return "Successfully created game!";
  }

  private String joinGame(String... params) throws ClientException {
    assertSignedIn();

    if (params.length != 2) throw new ClientException(400, "Expected: <gameID> <BLACK/WHITE>");

    var id=Integer.parseInt(params[0]);

    if (!gameIDs.containsKey(id)) throw new ClientException(400, "No such game!");

    gameID=gameIDs.get(id);

    server.joinGame(authToken, gameID, params[1]);
    teamColor="BLACK".equals(params[1]) ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

    ws=new WebsocketFacade("http://localhost:8080", notificationHandler, authToken, this);
    ws.joinPlayer(gameID, teamColor);

    state=State.IN_GAME;

    currentGame=new GameImple();
    var board=currentGame.getBoard();
    board.resetBoard();

    return "Successfully joined game!";
  }

  private String observeGame(String... params) throws ClientException {
    assertSignedIn();

    if (params.length != 1) throw new ClientException(400, "Expected: <gameID>");

    var id=Integer.parseInt(params[0]);

    if (!gameIDs.containsKey(id)) throw new ClientException(400, "No such game!");

    gameID=gameIDs.get(id);

    server.joinGame(authToken, gameID, null);

    ws=new WebsocketFacade("http://localhost:8080", notificationHandler, authToken, this);
    ws.joinObserver(gameID);

    state=State.OBSERVING;

    currentGame=new GameImple();
    var board=currentGame.getBoard();
    board.resetBoard();

    return "Successfully joined game!";
  }

  private String resign() throws ClientException {
    assertInGame();

    ws.resign(gameID);
    state=State.SIGNED_IN;
    currentGame=null;
    teamColor=null;

    return "Successfully resigned!";
  }

  private String leave() throws ClientException {
    assertInGameOrObserving();

    ws.leave(gameID);
    state=State.SIGNED_IN;
    currentGame=null;
    teamColor=null;

    return "Successfully left the game!";
  }

  private String move(String... params) throws ClientException {
    assertInGame();

    if (params.length != 2) throw new ClientException(400, "Expected: <file><rank> <file><rank>");

    var from=params[0].toLowerCase();
    var to=params[1].toLowerCase();

    if (from.length() != 2 || to.length() != 2) throw new ClientException(400, "Expected: <file><rank> <file><rank>");

    var positions=new PositionImple[]{null, null};

    var index=0;

    for (var pos : new String[]{from, to}) {
      var file=pos.charAt(0);
      var rank=pos.charAt(1);

      if (file < 97 || file > 104 || rank < 49 || rank > 56)
        throw new ClientException(400, "Expected: <[a-h]><[1-8]> <[a-h]><[1-8]>");

      positions[index++]=new PositionImple(rank - 49, 7 - (file - 97));
    }

    try {
      var piece=currentGame.getBoard().getPiece(positions[0]);

      if (piece != null && (piece.getTeamColor() != teamColor)) {
        throw new ClientException(400, "Wrong team piece!");
      }

      var move=new MoveImple(positions[0], positions[1]);

      currentGame.makeMove(move);
      ws.makeMove(move);
      return "Successfully moved!";
    } catch (InvalidMoveException ex) {
      var message=ex.getMessage().length() != 0 ? ex.getMessage() : "Invalid move!";
      throw new ClientException(400, message);
    }
  }

  private String highlight(String... params) throws ClientException {
    assertInGameOrObserving();

    if (params.length != 1 || params[0].length() != 2) throw new ClientException(400, "Expected: <file><rank>");

    var file=params[0].charAt(0);
    var rank=params[0].charAt(1);

    if (file < 97 || file > 104 || rank < 49 || rank > 56)
      throw new ClientException(400, "Expected: <[a-h]><[1-8]> <[a-h]><[1-8]>");

    var position=new PositionImple(rank - 49, 7 - (file - 97));

    var piece=currentGame.getBoard().getPiece(position);
    if (piece == null) throw new ClientException(400, "Not a piece!");

    return displayBoard(currentGame.getBoard(), teamColor, position);
  }

  private String redraw() throws ClientException {
    assertInGameOrObserving();

    return displayBoard(currentGame.getBoard(), teamColor);
  }

  public String displayGame(GameImple game) {
    currentGame=game;
    return displayBoard(game.getBoard(), this.teamColor);
  }

  private String displayBoard(ChessBoard board, ChessGame.TeamColor perspective) {
    return displayBoard(board, perspective, null);
  }

  private String displayBoard(ChessBoard board, ChessGame.TeamColor perspective, ChessPosition pieceMovesToHighlight) {
    var horizontalChars=new char[]{'h', 'g', 'f', 'e', 'd', 'c', 'b', 'a'};
    var verticalChars=new char[]{'1', '2', '3', '4', '5', '6', '7', '8'};

    var order=new int[]{0, 1, 2, 3, 4, 5, 6, 7};

    if (perspective == ChessGame.TeamColor.WHITE) {
      for (int index=0; index < order.length / 2; ++index) {
        var temp=order[index];
        order[index]=order[7 - index];
        order[7 - index]=temp;
      }
    }

    var tempGame=new GameImple();
    tempGame.setBoard(board);
    Collection<ChessMove> moves=new HashSet<>();
    if (pieceMovesToHighlight != null) moves=tempGame.validMoves(pieceMovesToHighlight);

    var highlightedPos=new HashSet<ChessPosition>();
    highlightedPos.add(pieceMovesToHighlight);

    for (var move : moves) {
      highlightedPos.add(move.getEndPosition());
    }

    var sb=new StringBuilder();

    for (int y=0; y < 10; ++y) {
      for (int x=0; x < 10; ++x) {
        String background;
        String textColor=SET_TEXT_COLOR_GREEN;
        char charToPlace=' ';
        if (x == 0 || x == 9 || y == 0 || y == 9) {
          background=SET_BG_COLOR_LIGHT_GREY;
          textColor=SET_TEXT_COLOR_BLACK;
          if (y == 0 || y == 9) {
            if (x != 0 && x != 9) charToPlace=horizontalChars[order[x - 1]];
          } else {
            charToPlace=verticalChars[order[y - 1]];
          }
        } else {
          if (y % 2 == 0) {
            if (x % 2 == 1) background=SET_BG_COLOR_BLACK;
            else background=SET_BG_COLOR_WHITE;
          } else {
            if (x % 2 == 0) background=SET_BG_COLOR_BLACK;
            else background=SET_BG_COLOR_WHITE;
          }

          ChessPosition pos;

          if (perspective == ChessGame.TeamColor.WHITE) pos=new PositionImple(7 - (y - 1), 7 - (x - 1));
          else pos=new PositionImple(y - 1, x - 1);

          if (highlightedPos.contains(pos)) {
            if (background.equals(SET_BG_COLOR_BLACK)) background=SET_BG_COLOR_DARK_GREEN;
            else background=SET_BG_COLOR_GREEN;
          }

          var piece=board.getPiece(pos);
          if (piece != null) {
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) textColor=SET_TEXT_COLOR_BLUE;
            else textColor=SET_TEXT_COLOR_RED;

            charToPlace=switch (piece.getPieceType()) {
              case ROOK -> 'R';
              case KNIGHT -> 'N';
              case BISHOP -> 'B';
              case KING -> 'K';
              case QUEEN -> 'Q';
              case PAWN -> 'P';
            };
          }
        }

        sb.append(background).append(textColor);
        sb.append(" ").append(charToPlace).append(" ");
      }
      sb.append(RESET + "\n");
    }
    return sb.toString();
  }

  public String help() {
    return switch (state) {
      case SIGNED_IN -> """
              - help
              - logout
              - create <gameName>
              - join <gameID> <BLACK/WHITE>
              - observe <gameID>
              - quit
              """;
      case SIGNED_OUT -> """
              - help
              - login <username> <password>
              - register <username> <password> <email>
              - quit
              """;
      case IN_GAME -> """
              - help
              - redraw
              - leave
              - move <file><rank> <file><rank>
              - resign
              - highlight <file><rank>
              """;
      case OBSERVING -> """
              - help
              - redraw
              - leave
              - highlight <file><rank>
              """;
      default -> "Unknown state";
    };
  }

  private void assertSignedIn() throws ClientException {
    if (state != State.SIGNED_IN) throw new ClientException(400, "Not signed in!");
  }

  private void assertInGame() throws ClientException {
    if (state != State.IN_GAME) throw new ClientException(400, "Not playing in game!");
  }

  private void assertInGameOrObserving() throws ClientException {
    if (state != State.OBSERVING && state != State.IN_GAME) throw new ClientException(400, "Not in game!");
  }
}
