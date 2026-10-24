package server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataaccess.DataAccessException;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.UserDAO;
import model.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class ApiContractTest {
    private Server server;
    private String base;
    private final HttpClient client = HttpClient.newHttpClient();

    @BeforeEach
    void start() {
        server = new Server();
        base = "http://localhost:" + server.run(0);
    }

    @AfterEach
    void stop() {
        server.stop();
    }

    @Test
    void gameListHasExactlyThePublishedFieldsIncludingNullSeats() throws Exception {
        String token = register();
        var created = send("POST", "/game", "{\"gameName\":\"Finals\"}", token);
        assertEquals(200, created.statusCode());
        var listed = send("GET", "/game", null, token);
        assertEquals(200, listed.statusCode());
        JsonObject game = object(listed.body()).getAsJsonArray("games").get(0).getAsJsonObject();
        assertEquals(Set.of("gameID", "whiteUsername", "blackUsername", "gameName"), game.keySet());
        assertTrue(game.get("whiteUsername").isJsonNull());
        assertTrue(game.get("blackUsername").isJsonNull());
        assertEquals("Finals", game.get("gameName").getAsString());
    }

    @Test
    void malformedAndEmptyJsonProduceBadRequest() throws Exception {
        for (String body : new String[]{"", "null", "[]", "{", "{} trailing"}) {
            var response = send("POST", "/user", body, null);
            assertEquals(400, response.statusCode());
            assertEquals("Error: bad request", object(response.body()).get("message").getAsString());
        }
    }

    @Test
    void tokenInBodyCannotReplaceAuthorizationHeader() throws Exception {
        String token = register();
        String body = "{\"gameName\":\"Game\",\"authToken\":\"" + token + "\"}";
        var response = send("POST", "/game", body, null);
        assertEquals(401, response.statusCode());
        assertEquals("Error: unauthorized", object(response.body()).get("message").getAsString());
    }

    @Test
    void unexpectedDaoFailureProducesJson500WithoutPrivateDetails() throws Exception {
        server.stop();
        UserDAO failingUsers = new UserDAO() {
            @Override
            public Optional<UserData> find(String username) {
                return Optional.empty();
            }

            @Override
            public void create(UserData user) throws DataAccessException {
                throw new DataAccessException("private database details");
            }

            @Override
            public void clear() { }
        };
        server = new Server(failingUsers, new MemoryAuthDAO(), new MemoryGameDAO());
        base = "http://localhost:" + server.run(0);
        var response = send("POST", "/user", "{\"username\":\"ana\",\"password\":\"pw\",\"email\":\"e\"}", null);
        assertEquals(500, response.statusCode());
        assertEquals("Error: internal server error", object(response.body()).get("message").getAsString());
        assertTrue(response.headers().firstValue("Content-Type").orElseThrow().contains("application/json"));
    }

    private String register() throws Exception {
        var response = send("POST", "/user", "{\"username\":\"ana\",\"password\":\"pw\",\"email\":\"e\"}", null);
        assertEquals(200, response.statusCode());
        return object(response.body()).get("authToken").getAsString();
    }

    private HttpResponse<String> send(String method, String path, String body, String token) throws Exception {
        HttpRequest.Builder request = HttpRequest.newBuilder(URI.create(base + path))
                .header("Content-Type", "application/json");
        if (token != null) {
            request.header("authorization", token);
        }
        request.method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        return client.send(request.build(), HttpResponse.BodyHandlers.ofString());
    }

    private JsonObject object(String body) {
        return JsonParser.parseString(body).getAsJsonObject();
    }
}
