package com.huzt.data;


public class BloodEdge {

    public Integer id;
    public Integer source;
    public Integer target;

    public BloodEdge(Integer source, Integer target) {
        this.source = source;
        this.target = target;
    }

    public Integer getId() {
        if(id == null) {
            return this.hashCode();
        }
        return this.id;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((source == null) ? 0 : source.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
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
        BloodEdge other = (BloodEdge) obj;
        if (source == null) {
            if (other.source != null)
                return false;
        } else if (!source.equals(other.source))
            return false;
        if (target == null) {
            if (other.target != null)
                return false;
        } else if (!target.equals(other.target))
            return false;
        return true;
    }


}
