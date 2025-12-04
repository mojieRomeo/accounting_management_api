package top.swjtuhc.accounting_management_api.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

@Data
public class PageResponse<T> {


    private Long current;

    private Long total;

    private Long size;

    private List<T> record;

    public PageResponse(Page<T> page) {
        this.current = page.getCurrent();
        this.total = page.getTotal();
        this.size = page.getSize();
        this.record = page.getRecords();
    }

    public PageResponse() {
    }

    public PageResponse(Long current, Long total, Long size, List<T> record) {
        this.current = current;
        this.total = total;
        this.size = size;
        this.record = record;
    }

    public PageResponse(Page page, List<T> record) {
        this.current = page.getCurrent();
        this.total = page.getTotal();
        this.size = page.getSize();
        this.record = record;
    }
}
