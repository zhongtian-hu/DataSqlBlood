package com.huzt.data;

import java.util.List;

/**
 * 血缘接口
 *
 * @author diven
 */
public interface BloodEngine {

    /**
     * 根据sql获取解析结果
     * @param sqls
     * @return	sql解析结果
     */
    public List<SelectInfo> parser(List<String> sqls) throws Exception;

    /**
     * 获取当前sql的表血缘
     * @param  sqls 语句
     * @return	TableBlood表血缘
     */
    public  List<SqlBlood> getTableBlood(List<String> sqls) throws Exception;


    /**
     * 根据血缘图获取指定表的字段血缘
     * @param sqls	sql 语句
     * @return FieldBlood	字段血缘
     */
    public  List<SqlBlood> getFieldBlood(List<String> sqls) throws Exception;

}