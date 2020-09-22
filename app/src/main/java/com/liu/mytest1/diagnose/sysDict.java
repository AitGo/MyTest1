package com.liu.mytest1.diagnose;


import java.io.Serializable;

/**
 * @创建者 ly
 * @创建时间 2019/4/4
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */
public class sysDict implements Serializable {
    private static final long serialVersionUID = 8023926597195423032L;
    private String id;//
    private String dictLevel;//字典级别
    private String dictKey;//字典代码值
    private String parentKey;//父级代码值
    private String rootKey;//根级代码值
    private String dictValue;//字典中文值
    private String dictPy;
    private String remark;

    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getDictLevel() {
        return this.dictLevel;
    }
    public void setDictLevel(String dictLevel) {
        this.dictLevel = dictLevel;
    }
    public String getDictKey() {
        return this.dictKey;
    }
    public void setDictKey(String dictKey) {
        this.dictKey = dictKey;
    }
    public String getParentKey() {
        return this.parentKey;
    }
    public void setParentKey(String parentKey) {
        this.parentKey = parentKey;
    }
    public String getRootKey() {
        return this.rootKey;
    }
    public void setRootKey(String rootKey) {
        this.rootKey = rootKey;
    }
    public String getDictValue() {
        return this.dictValue;
    }
    public void setDictValue(String dictValue) {
        this.dictValue = dictValue;
    }
    public String getDictPy() {
        return this.dictPy;
    }
    public void setDictPy(String dictPy) {
        this.dictPy = dictPy;
    }
    public String getRemark() {
        return this.remark;
    }
    public void setRemark(String remark) {
        this.remark = remark;
    }

}
