package serverUnitTests;

import dataAccess.DatabaseAccess;
import dataAccess.MemoryDAO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import services.TestingService;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class TestingServiceTest {
  DatabaseAccess dao;

  @BeforeEach
  void prepTest() {
    dao=MemoryDAO.getInstance();
  }

  @Test
  void clear() {
    var testingService=new TestingService(dao);

    assertDoesNotThrow(testingService::clear);
  }
}