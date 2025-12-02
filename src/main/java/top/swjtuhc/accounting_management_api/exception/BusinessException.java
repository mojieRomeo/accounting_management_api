package top.swjtuhc.accounting_management_api.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException{

    private int code;
    public BusinessException(String msg, Throwable t) {
        super(msg, t);
        this.code = 500;
    };

    public BusinessException(String msg) {
        super(msg);
        this.code = 500;
    }

    public BusinessException(String msg, int code) {
        super(msg);
        this.code = code;
    }

}