package live.qsmc.api.account;


import live.qsmc.quipt.core.data.JsonSerializable;

public class Permission implements JsonSerializable {

    String id;

    public Permission(String id) {
        this.id = id;
    }

}
