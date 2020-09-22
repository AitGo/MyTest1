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
public class JsonResult {


    /**
     * result : {"code":"0000","msg":"调用服务成功","pagesize":"10","index":"1","count":"0","data":[{"sysDict":{"msg":"成功","code":0,"data":[{"unitName":"测试刑科所","unitCode":"500000000000","id":"12345678901234567890123456789a01","shortName":"测试刑科所","parentId":"12345678901234567890000000000010"}]}}]}
     */

    private ResultBean result;

    public ResultBean getResult() {
        return result;
    }

    public void setResult(ResultBean result) {
        this.result = result;
    }

    public static class ResultBean {
        /**
         * code : 0000
         * msg : 调用服务成功
         * pagesize : 10
         * index : 1
         * count : 0
         * data : [{"sysDict":{"msg":"成功","code":0,"data":[{"unitName":"测试刑科所","unitCode":"500000000000","id":"12345678901234567890123456789a01","shortName":"测试刑科所","parentId":"12345678901234567890000000000010"}]}}]
         */

        private String code;
        private String msg;
        private String pagesize;
        private String index;
        private String count;
        private List<DataBeanX> data;

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getPagesize() {
            return pagesize;
        }

        public void setPagesize(String pagesize) {
            this.pagesize = pagesize;
        }

        public String getIndex() {
            return index;
        }

        public void setIndex(String index) {
            this.index = index;
        }

        public String getCount() {
            return count;
        }

        public void setCount(String count) {
            this.count = count;
        }

        public List<DataBeanX> getData() {
            return data;
        }

        public void setData(List<DataBeanX> data) {
            this.data = data;
        }

        public static class DataBeanX {
            /**
             * sysDict : {"msg":"成功","code":0,"data":[{"unitName":"测试刑科所","unitCode":"500000000000","id":"12345678901234567890123456789a01","shortName":"测试刑科所","parentId":"12345678901234567890000000000010"}]}
             */

            private Json1 sysDict;

            public Json1 getSysDict() {
                return sysDict;
            }

            public void setSysDict(Json1 sysDict) {
                this.sysDict = sysDict;
            }
        }
    }
}
