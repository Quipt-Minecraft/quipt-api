package live.qsmc.api;

import live.qsmc.core2.data.JsonSerializable;

public class Greeting implements JsonSerializable {

    public String message = "Hello World!";
    public String timestamp = String.valueOf(System.currentTimeMillis());



    @Override
    public String toString() {
        return "Greeting{" +
                "message='" + message + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
