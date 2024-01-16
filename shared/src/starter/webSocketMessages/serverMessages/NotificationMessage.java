package webSocketMessages.serverMessages;

public class NotificationMessage extends ServerMessage {
  private final String message;

  public NotificationMessage(String message) {
    super(ServerMessageType.NOTIFICATION);
    this.message=message;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return "NotificationMessage{" +
            "message='" + message + '\'' +
            '}';
  }
}
