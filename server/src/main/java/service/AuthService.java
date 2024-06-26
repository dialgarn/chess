package service;

import dataAccess.AuthDAO;
import Exception.DataAccessException;
import model.AuthData;
import model.UserData;

public class AuthService {
    private final AuthDAO dataAccess;

    public AuthService(AuthDAO dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData createAuth(UserData user) throws DataAccessException {
        return this.dataAccess.createAuth(user);
    }

    public void deleteAuth(String authToken) throws DataAccessException {
        this.dataAccess.deleteAuth(authToken);
    }

    public AuthData verify(String authToken) throws DataAccessException {
        return this.dataAccess.verify(authToken);
    }

    public void clear() throws DataAccessException {
        this.dataAccess.clear();
    }
}
