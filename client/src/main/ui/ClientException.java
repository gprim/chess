package ui;

public class ClientException extends Exception {
  final private int statusCode;

  public ClientException(int statusCode, String message) {
    super(message);
    this.statusCode=statusCode;
  }

  public ClientException(String message) {
    super(message);
    this.statusCode=400;
  }

  public int getStatusCode() {
    return statusCode;
  }
}
