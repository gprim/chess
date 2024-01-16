package serverUnitTests;

import dataAccess.DataAccessException;
import dataAccess.DatabaseAccess;
import dataAccess.MemoryDAO;
import models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.TestingService;
import services.UserService;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {
  DatabaseAccess dao;

  @BeforeEach
  void prepTest() {
    dao=MemoryDAO.getInstance();
    var testingService=new TestingService(dao);
    try {
      testingService.clear();
    } catch (Exception err) {
      System.out.println("Error: " + err.getMessage());
    }
  }

  @Test
  void registerUserSuccess() {
    var user=new User("username", "password", "email");

    var userService=new UserService(dao);

    assertDoesNotThrow(() -> userService.registerUser(user));
  }

  @Test
  void registerUserAlreadyExists() {
    var user=new User("username", "password", "email");

    var userService=new UserService(dao);

    assertDoesNotThrow(() -> userService.registerUser(user));
    var err=assertThrows(DataAccessException.class, () -> userService.registerUser(user));

    assertEquals("already taken", err.getMessage());
  }
}
