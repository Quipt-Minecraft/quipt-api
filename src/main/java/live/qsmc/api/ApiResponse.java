package live.qsmc.api;

import live.qsmc.core2.data.JsonSerializable;
import org.springframework.http.HttpEntity;

public class ApiResponse<T extends JsonSerializable> extends HttpEntity<T>  implements JsonSerializable {

    public final T data;
    public final Status status;

    public ApiResponse(T data, Status status) {
        this.data = data;
        this.status = status;
    }



    public enum Status {
        SUCCESS,
        ERROR
    }
}
