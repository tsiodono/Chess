package server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import io.javalin.http.Context;
import model.ErrorResult;
import service.ServiceException;

/** Converts HTTP JSON and maps errors without exposing internal exception details. */
final class JsonUtil {
    private final Gson gson = new GsonBuilder()
            .serializeNulls()
            .create();

    <T> T parse(Context ctx, Class<T> type) {
        try {
            T request = gson.fromJson(ctx.body(), type);
            if (request == null) {
                throw new ServiceException(400, "bad request");
            }
            return request;
        } catch (JsonParseException ex) {
            throw new ServiceException(400, "bad request");
        }
    }

    void write(Context ctx, Object body) {
        ctx.status(200);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(body));
    }

    void error(Context ctx, int status, String message) {
        ctx.status(status);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(new ErrorResult("Error: " + message)));
    }
}
