package top.swjtuhc.accounting_management_api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TypeEnum {

    INCOME(1, "收入"),
    EXPENDITURE(2, "支出");

    private final int code;
    private final String msg;
}
