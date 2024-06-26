package ui.websocket;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import model.GameData;
import webSocketMessages.userCommands.*;
import webSocketMessages.serverMessages.*;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

//need to extend Endpoint for websocket to work properly
public class WebSocketFacade extends Endpoint {

    Session session;
    private CountDownLatch messageLatch;

    private ChessGame.TeamColor playerColor;

    public interface MessageReceivedCallback {
        void onMessageReceived(ServerMessage message);
    }

    private MessageReceivedCallback messageReceivedCallback;

    public void setMessageReceivedCallback(MessageReceivedCallback callback) {
        this.messageReceivedCallback = callback;
    }

    public WebSocketFacade(String url) throws Exception {
        try {
            messageLatch = new CountDownLatch(1);
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/connect");

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);


            //set message handler
            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage notification = new Gson().fromJson(message, ServerMessage.class);
                    if (messageReceivedCallback != null) {
                        messageReceivedCallback.onMessageReceived(notification);
                    }
                    switch (notification.getServerMessageType()) {
                        case LOAD_GAME:
                            LoadGameMessage notif = new Gson().fromJson(message, LoadGameMessage.class);
                            GameData game = notif.getGame();
                            var teamColor = notif.getTeamColor();
                            if (teamColor == ChessGame.TeamColor.WHITE || playerColor == ChessGame.TeamColor.WHITE) {
                                System.out.print("\n");
                                System.out.println(game.game().getBoard().realToStringWhite());
                            } else {
                                System.out.print("\n");
                                System.out.println(game.game().getBoard().realToStringBlack());
                            }
                            break;
                        case ERROR:
                            ErrorMessage errorMessage = new Gson().fromJson(message, ErrorMessage.class);
                            System.out.print("\n");
                            System.out.println(errorMessage.getMessage());
                        case NOTIFICATION:
                            NotificationMessage notificationMessage = new Gson().fromJson(message, NotificationMessage.class);
                            System.out.print("\n");
                            System.out.println(notificationMessage.getMessage());
                    }
                    System.out.print("\r>>> ");
                    messageLatch.countDown();
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new Exception();
        }
    }

    //Endpoint requires this method, but you don't have to do anything
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {

    }

    public void joinPlayer(String authToken, ChessGame.TeamColor teamColor, int gameID) {
        try {
            var command = new JoinCommand(authToken, teamColor, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
            boolean messageReceived = messageLatch.await(2, TimeUnit.SECONDS); // Adjust timeout as needed
            if (!messageReceived) {
                System.out.println("Timeout waiting for message.");
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            messageLatch = new CountDownLatch(1);
        }
    }

    public void joinObserver(String authToken, int gameID) {
        try {
            var command = new JoinObserver(authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
            boolean messageReceived = messageLatch.await(2, TimeUnit.SECONDS); // Adjust timeout as needed
            if (!messageReceived) {
                System.out.println("Timeout waiting for message.");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            messageLatch = new CountDownLatch(1);
        }
    }

    public void leave(String authToken, int gameID) {
        try {
            var command = new LeaveCommand(authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
            messageLatch.await(1, TimeUnit.SECONDS); // Adjust timeout as needed
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            messageLatch = new CountDownLatch(1);
        }
    }

    public void resign(String authToken, int gameID) {
        try {
            var command = new ResignCommand(authToken, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
            messageLatch.await(1, TimeUnit.SECONDS); // Adjust timeout as needed
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            messageLatch = new CountDownLatch(1);
        }
    }

    public void move(String authToken, ChessMove move, int gameID) {
        try {
            var command = new MakeMoveCommand(authToken, move, gameID);
            this.session.getBasicRemote().sendText(new Gson().toJson(command));
            messageLatch.await(1, TimeUnit.SECONDS); // Adjust timeout as needed
        } catch (Throwable e) {
            throw new RuntimeException(e);
        } finally {
            messageLatch = new CountDownLatch(1);
        }
    }

    public void setPlayerColor(ChessGame.TeamColor color) {
        this.playerColor = color;
    }

}
