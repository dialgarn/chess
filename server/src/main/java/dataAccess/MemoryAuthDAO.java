package dataAccess;

import model.AuthData;
import model.UserData;

import java.util.HashSet;
import java.util.Objects;
import java.util.UUID;
import Exception.DataAccessException;

public class MemoryAuthDAO implements AuthDAO {
    private final HashSet<AuthData> authList = new HashSet<>();

    public AuthData createAuth(UserData user){
        String authToken = UUID.randomUUID().toString();
        AuthData authData = new AuthData(authToken, user.username());
        authList.add(authData);
        return authData;
    }


    public void deleteAuth(String authToken) throws DataAccessException {
        for (AuthData auth : authList) {
            if (Objects.equals(auth.authToken(), authToken)) {
                authList.remove(auth);
                return;
            }
        }
        throw new DataAccessException("Unauthorized");
    }

    public AuthData verify(String authToken) throws DataAccessException {
        for (AuthData auth : authList) {
            if (Objects.equals(auth.authToken(), authToken)) {
                return auth;
            }
        }
        throw new DataAccessException("Unauthorized");
    }

    public void clear() {
        authList.clear();
    }

    public int getSize() {
        return authList.size();
    }

    public boolean contains(AuthData auth) {
        return authList.contains(auth);
    }
}
