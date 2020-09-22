package com.liu.mytest1.diagnose;


/**
 * @创建者 ly
 * @创建时间 2019/4/4
 * @描述 ${TODO}
 * @更新者 $Author$
 * @更新时间 $Date$
 * @更新描述 ${TODO}
 */
public class sysOrgan {
    private String id;
    private String parentId;//父级id
    private String unitName;//单位名称
    private String shortName;//单位简称
    private String unitCode;//单位代码

    public String getId() {
        return this.id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getParentId() {
        return this.parentId;
    }
    public void setParentId(String parentId) {
        this.parentId = parentId;
    }
    public String getUnitName() {
        return this.unitName;
    }
    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }
    public String getShortName() {
        return this.shortName;
    }
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
    public String getUnitCode() {
        return this.unitCode;
    }
    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

}
