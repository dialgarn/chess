package serviceTests;

import Exception.DataAccessException;
import dataAccess.MemoryUserDAO;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.UserService;

class UserServiceTest {

    @Test
    void registerUser() throws DataAccessException {
        var userDAO = new MemoryUserDAO();
        var myObject = new UserService(userDAO);
        UserData user1 = new UserData("user1", "password1", "user1@gmail.com");
        UserData user2 = new UserData("user2", "password2", "user2@gmail.com");

        myObject.registerUser(user1);
        myObject.registerUser(user2);

        int size = userDAO.getSize();

        Assertions.assertEquals(2, size);
    }

    @Test
    void registerUserFail() throws DataAccessException {
        var myObject = new  UserService(new MemoryUserDAO());
        UserData user1 = new UserData("user1", "password1", "user1@gmail.com");

        myObject.registerUser(user1);

        Assertions.assertThrows(DataAccessException.class, ()->myObject.registerUser(user1));
    }

    @Test
    void loginSuccess() throws DataAccessException {
        var myObject = new UserService(new MemoryUserDAO());
        UserData user1 = new UserData("user1", "password1", "user1@gmail.com");

        myObject.registerUser(user1);


        Assertions.assertEquals(user1, myObject.login(user1));
    }

    @Test
    void loginFail() throws DataAccessException {
        var myObject = new UserService(new MemoryUserDAO());
        UserData user1 = new UserData("user1", "password1", "user1@gmail.com");
        UserData user2 = new UserData("user2", "password2", "user2@gmail.com");

        myObject.registerUser(user1);

        Assertions.assertThrows(DataAccessException.class, ()->myObject.login(user2));
    }

    @Test
    void clear() throws DataAccessException {
        var userDAO = new MemoryUserDAO();
        var myObject = new UserService(userDAO);

        myObject.registerUser(new UserData("testUser", "123", "test@test.com"));
        myObject.clear();

        int size = userDAO.getSize();
        int expectedSize = 0;

        Assertions.assertEquals(expectedSize, size);

    }
}