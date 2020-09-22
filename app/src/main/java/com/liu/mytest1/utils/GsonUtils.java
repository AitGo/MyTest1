package com.liu.mytest1.utils;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.liu.mytest1.diagnose.Json1;
import com.liu.mytest1.diagnose.JsonResult;
import com.liu.mytest1.diagnose.sysDict;
import com.liu.mytest1.diagnose.sysOrgan;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ikidou.reflect.TypeBuilder;


/**
 * @创建者 ly
 * @创建时间 2019/4/22
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */
public class GsonUtils {

    public static void main(String[] args) {
        String s = "{\n" +
                "\t\"result\": {\n" +
                "\t\t\"code\": \"0000\",\n" +
                "\t\t\"msg\": \"调用服务成功\",\n" +
                "\t\t\"pagesize\": \"10\",\n" +
                "\t\t\"index\": \"1\",\n" +
                "\t\t\"count\": \"0\",\n" +
                "\t\t\"data\": [{\n" +
                "\t\t\t\"sysDict\": {\"msg\": \"成功\",\n" +
                "\t\t\t\t\"code\": 0,\n" +
                "\t\t\t\t\"data\": [{\n" +
                "\t\t\t\t\t\"unitName\": \"测试刑科所\",\n" +
                "\t\t\t\t\t\"unitCode\": \"500000000000\",\n" +
                "\t\t\t\t\t\"id\": \"12345678901234567890123456789a01\",\n" +
                "\t\t\t\t\t\"shortName\": \"测试刑科所\",\n" +
                "\t\t\t\t\t\"parentId\": \"12345678901234567890000000000010\"\n" +
                "\t\t\t\t}]}\n" +
                "\t\t\t\n" +
                "\t\t}]\n" +
                "\t}\n" +
                "}";
//        String s = "{\n" +
//                "\t\"result\": {\n" +
//                "\t\t\"code\": \"0000\",\n" +
//                "\t\t\"msg\": \"调用服务成功\",\n" +
//                "\t\t\"pagesize\": \"10\",\n" +
//                "\t\t\"index\": \"1\",\n" +
//                "\t\t\"count\": \"0\",\n" +
//                "\t\t\"data\": []\n" +
//                "\t}\n" +
//                "}";
        JsonResult jsonResult = GsonUtils.gsonBean(s, JsonResult.class);
        JsonResult.ResultBean result = jsonResult.getResult();
        if(StringUtils.checkString(result.getCode()) && result.getCode().equals("0000")) {
            if(result.getData().size() > 0) {
                Json1 sysDict = result.getData().get(0).getSysDict();
                if(sysDict.getCode() == 0) {
                    Object data = sysDict.getData();
                    System.out.println(data.toString());
                    List<sysOrgan> organs = GsonUtils.jsonToList(data.toString(), sysOrgan.class);
                    if(organs.size() > 0) {
                        System.out.println(organs.get(0).getParentId());
                    }
                }
            }
        }else if(StringUtils.checkString(result.getCode()) && result.getCode().equals("9999")) {
            //服务出现异常：原因
            result.getMsg();
        }
    }

    private static Gson gson = null;

    static {
        if (gson == null) {
            gson = new Gson();
        }
    }

    /**
     * 将object对象转成json字符串
     *
     * @param object
     * @return
     */
    public static String gsonString(Object object) {
        String gsonString = null;
        if (gson != null) {
            gsonString = gson.toJson(object);
        }
        return gsonString;
    }

    /**
     * 将gsonString转成泛型bean
     *
     * @param gsonString
     * @param cls
     * @return
     */
    public static <T> T gsonBean(String gsonString, Class<T> cls) {
        T t = null;
        if (gson != null) {
            t = gson.fromJson(gsonString, cls);
        }
        return t;
    }

    public static <T> List<T> jsonToList(String json, Class<T> cls) {
        List<T> list = new ArrayList<T>();
        JsonArray array = new JsonParser().parse(json).getAsJsonArray();
        for(final JsonElement elem : array){
            list.add(new Gson().fromJson(elem, cls));
        }
        return list;
    }

}
