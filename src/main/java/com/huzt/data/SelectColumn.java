package com.huzt.data;
/**
 * Created by 中天 on 2019/7/15.
 * 处理sql语句中的字段,常量字段的sourcolumn长度位0
 */
import java.util.ArrayList;
import java.util.List;

public class SelectColumn {
    public String expression;
    public List<String> sourcolumn =  new ArrayList<>();
    public String alias;
}

