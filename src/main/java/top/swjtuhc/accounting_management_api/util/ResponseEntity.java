package top.swjtuhc.accounting_management_api.util;

import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Accessors(chain = true)
public class ResponseEntity<T> {

    private String msg;

    private Integer status;

    private T data;

    public static <T> ResponseEntity<T> ok() {
        return new ResponseEntity<T>().setMsg("success").setStatus(200);
    }

    public static <T> ResponseEntity<T> ok(T data) {
        return new ResponseEntity<T>().setData(data).setStatus(200).setMsg("success");
    }

    public static <T> ResponseEntity<T> ok(String msg) {
        return new ResponseEntity<T>().setData(null).setStatus(200).setMsg(msg);
    }

    public static <T> ResponseEntity<T> ok(T data, String msg) {
        return new ResponseEntity<T>().setData(data).setStatus(200).setMsg(msg);
    }

    public static <T> ResponseEntity<T> fail(int status, String msg) {
        return new ResponseEntity<T>().setStatus(status).setMsg(msg);
    }

    public static <T> ResponseEntity<T> fail(int status, String msg, T data) {
        return new ResponseEntity<T>().setStatus(status).setMsg(msg).setData(data);
    }


    public static <T> ResponseEntity<T> fail(String msg) {
        return new ResponseEntity<T>().setMsg(msg).setStatus(500);
    }
}
