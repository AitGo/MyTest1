package com.liu.mytest1.diagnose;

import java.util.List;

/**
 * @创建者 ly
 * @创建时间 2020/8/31
 * @描述
 * @更新者 $
 * @更新时间 $
 * @更新描述
 */
public class Json1<T> {

    /**
     * msg : 成功
     * code : 0
     * data : [{"unitName":"测试刑科所","unitCode":"500000000000","id":"12345678901234567890123456789a01","shortName":"测试刑科所","parentId":"12345678901234567890000000000010"},{"unitName":"浏阳刑科所","unitCode":"501200000000","id":"2f869bcd6cffeb3e016d0000fddf0003","shortName":"浏阳刑科所","parentId":"12345678901234567890123456789a01"}]
     */

    private String msg;
    private int code;
    private T data;

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
