package live.qsmc.api.util;

import live.qsmc.core2.data.JsonSerializable;
import org.json.JSONObject;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpEntity;

import java.util.Map;

public class ApiResponse<T> extends HttpEntity<Map<String, Object>> implements JsonSerializable {

    public final T data;
    public final Status status;

    public ApiResponse(Status status, T data) {
        this.data = data;
        this.status = status;
    }

    @Override
    public @Nullable Map<String, Object> getBody() {
        JSONObject json = new JSONObject();
        json.put("status", status.name());
        json.put("data", data);
        return json.toMap();
    }

    public enum Status {
        SUCCESS,
        FAILURE,
        WARNING,
        NO_ACTION,
        NO_RESPONSE,
        NO_PERMISSION,
        NO_ACCOUNT,
        NO_TOKEN,
        NO_DATA,
        NO_PERMISSION_DATA,
        NO_ACCOUNT_DATA,
        NO_TOKEN_DATA,
        NO_STORAGE,
        NO_PERMISSION_STORAGE,
        NO_ACCOUNT_STORAGE,
        NO_TOKEN_STORAGE,
    }

    public static class JsonResponse implements JsonSerializable {

        public JSONObject data = new JSONObject();
    }
}
