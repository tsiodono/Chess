package service;

import chess.ChessGame;
import dataaccess.AlreadyTakenException;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.NotFoundException;
import model.AuthData;
import model.CreateGameRequest;
import model.CreateGameResult;
import model.GameData;
import model.GameInfo;
import model.JoinGameRequest;
import model.ListGamesRequest;
import model.ListGamesResult;

public final class GameService {
    private final AuthDAO auths;
    private final GameDAO games;

    public GameService(AuthDAO auths, GameDAO games) {
        this.auths = auths;
        this.games = games;
    }

    public ListGamesResult listGames(ListGamesRequest request) throws DataAccessException {
        requireAuth(request == null ? null : request.authToken());
        return new ListGamesResult(games.list().stream()
                .map(g -> new GameInfo(g.gameID(), g.whiteUsername(), g.blackUsername(), g.gameName()))
                .toList());
    }

    public CreateGameResult createGame(CreateGameRequest request) throws DataAccessException {
        requireAuth(request == null ? null : request.authToken());
        if (request.gameName() == null || request.gameName().isBlank()) {
            throw new ServiceException(400, "bad request");
        }
        GameData game = games.create(request.gameName());
        return new CreateGameResult(game.gameID());
    }

    public void joinGame(JoinGameRequest request) throws DataAccessException {
        AuthData auth = requireAuth(request == null ? null : request.authToken());
        if (request.gameID() == null || request.gameID() <= 0 || request.playerColor() == null) {
            throw new ServiceException(400, "bad request");
        }
        ChessGame.TeamColor color;
        try {
            color = ChessGame.TeamColor.valueOf(request.playerColor());
        } catch (IllegalArgumentException ex) {
            throw new ServiceException(400, "bad request");
        }
        try {
            games.claimSeat(request.gameID(), color, auth.username());
        } catch (NotFoundException ex) {
            throw new ServiceException(400, "bad request");
        } catch (AlreadyTakenException ex) {
            throw new ServiceException(403, "already taken");
        }
    }

    private AuthData requireAuth(String token) throws DataAccessException {
        if (token == null || token.isBlank()) {
            throw new ServiceException(401, "unauthorized");
        }
        return auths.find(token).orElseThrow(() -> new ServiceException(401, "unauthorized"));
    }
}
