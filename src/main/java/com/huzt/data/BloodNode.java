package com.huzt.data;
public class BloodNode {
    public Integer id;
    public String schemaName;
    public String tableName;
    public String fieldName;
    public String level;

    public BloodNode(String tableName) {
        this.tableName = tableName;
    }

    public BloodNode(String tableName,int level) {
        this.tableName = tableName;
        this.level="livel"+level;
    }

    public BloodNode(String schemaName, String tableName,int level) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.level="livel"+level;
    }

    public BloodNode(String schemaName, String tableName, String fieldName,int level) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.fieldName = fieldName;
        this.level="level"+level;
    }

    public Integer getId() {
        if(this.id == null) {
            return hashCode();
        }
        else {
            return this.id;
        }
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        if (schemaName!="")
            result = prime * result + ((schemaName == null) ? 0 : schemaName.hashCode());
        result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
        if (level!="")
            result = prime * result + ((level == null) ? 0 : level.hashCode());
        if (fieldName!="")
            result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BloodNode other = (BloodNode) obj;
        if (schemaName == null) {
            if (other.schemaName != null)
                return false;
        } else if (!schemaName.equals(other.schemaName))
            return false;
        if (tableName == null) {
            if (other.tableName != null)
                return false;
        } else if (!tableName.equals(other.tableName))
            return false;
        if (fieldName == null) {
            if (other.fieldName != null)
                return false;
        } else if (!fieldName.equals(other.fieldName))
            return false;
        return true;
    }


}
