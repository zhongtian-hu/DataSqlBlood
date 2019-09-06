package com.huzt.data;
/**
 * Created by 中天 on 2019/7/15.
 * 处理sql语句中的字段,常量字段的sourcolumn长度位0
 */
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectColumn {
    public List<String> expression =  new ArrayList<>();
    public Set<String> fromName =  new HashSet<>();
    public String NameParse;
}