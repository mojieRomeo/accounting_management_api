package top.swjtuhc.accounting_management_api.util;

import lombok.Data;


@Data
public class PageRequest {

    private Long current;

    private Long size;

}