package server.webSocket;

import com.google.gson.Gson;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import webSocketMessages.serverMessages.LoadGameMessage;
// import webSocketMessages.Notification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, Connection> connections = new ConcurrentHashMap<>();
    public final ConcurrentHashMap<String, Integer> games = new ConcurrentHashMap<>();

    public void add(String authToken, int gameID, Session session) {
        var connection = new Connection(authToken, session);
        connections.put(authToken, connection);
        games.put(authToken, gameID);
    }

    public void remove(String authToken) {
        connections.remove(authToken);
        games.remove(authToken);
    }

    public void broadcast(String excludeAuthToken, int gameID, String notification) {
        var removeList = new ArrayList<String>();

        for (var entry : connections.entrySet()) {
            String authToken = entry.getKey();
            Connection c = entry.getValue();
            try {
                Integer clientGameId = games.get(authToken);
                if (clientGameId != null && clientGameId.equals(gameID) && c.session.isOpen() && !authToken.equals(excludeAuthToken)) {
                    c.send(notification);
                } else if (!c.session.isOpen()) {
                    removeList.add(authToken);
                }
            } catch (IOException e) {
                System.err.println("Error broadcasting message to " + authToken + ": " + e.getMessage());
                removeList.add(authToken);
            }
        }

        removeList.forEach(this::remove);
    }

    public void broadcastMove(GameData game) {
        var removeList = new ArrayList<String>(); // Use authToken for the removal list

        for (var entry : connections.entrySet()) {
            String authToken = entry.getKey();
            Connection c = entry.getValue();
            try {
                Integer clientGameId = games.get(authToken); // Retrieve gameID directly using authToken
                if (clientGameId != null && clientGameId == game.gameID() && c.session.isOpen()) {
                    LoadGameMessage loadGameMessage = new LoadGameMessage(game, null);
                    c.send(new Gson().toJson(loadGameMessage)); // Send the "LOAD GAME" message
                } else if (!c.session.isOpen()) {
                    removeList.add(authToken); // Mark for removal if session is closed
                }
            } catch (IOException e) {
                System.err.println("Error broadcasting LOAD GAME to " + authToken + ": " + e.getMessage());
                removeList.add(authToken); // Optionally mark for removal on IOException
            }
        }

        // Clean up connections marked for removal
        removeList.forEach(this::remove); // Use ConnectionManager's remove method for cleanup

    }



}
