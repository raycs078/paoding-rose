/*
 * Copyright 2009-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License i distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.paoding.rose.jade.jadeinterface.provider.jdbctemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import net.paoding.rose.jade.jadeinterface.provider.DataAccess;
import net.paoding.rose.jade.jadeinterface.provider.Modifier;

import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 * 通过 SpringJdbcTemplate 实现的 {@link DataAccess}.
 * 
 * @author 王志亮 [qieqie.wang@gmail.com]
 * @author 廖涵 [in355hz@gmail.com]
 */
public class NamedParamJdbcTemplateDataAccess extends JdbcTemplateDataAccess {

    private static final Pattern NAMED_PARAM_PATTERN = Pattern.compile("(\\:([a-zA-Z0-9_\\.]+))");

    private JdbcTemplate jdbcTemplate = new JdbcTemplate();

    public NamedParamJdbcTemplateDataAccess() {
    }

    public NamedParamJdbcTemplateDataAccess(DataSource dataSource) {
        jdbcTemplate.setDataSource(dataSource);
    }

    @Override
    public List<?> select(String sql, Modifier modifier, Map<String, ?> parameters,
            RowMapper rowMapper) {

        if (sql == null) {
            throw new IllegalArgumentException("SQL must not be null");
        }

        // 用 :name 参数的值填充列表
        final List<Object> values = new ArrayList<Object>();

        sql = resolveParam(sql, parameters, values);

        return selectByJdbcTemplate(sql, values.toArray(), rowMapper);
    }

    @Override
    public int update(String sql, Modifier modifier, Map<String, ?> parameters) {

        if (sql == null) {
            throw new IllegalArgumentException("SQL must not be null");
        }

        // 用 :name 参数的值填充列表
        final List<Object> values = new ArrayList<Object>();

        sql = resolveParam(sql, parameters, values);

        return updateByJdbcTemplate(sql, values.toArray());
    }

    @Override
    public Number insertReturnId(String sql, Modifier modifier, Map<String, ?> parameters) {

        if (sql == null) {
            throw new IllegalArgumentException("SQL must not be null");
        }

        // 用 :name 参数的值填充列表
        final List<Object> values = new ArrayList<Object>();

        sql = resolveParam(sql, parameters, values);

        return insertReturnIdByJdbcTemplate(sql, values.toArray());
    }

    /**
     * 查找 SQL 语句中所有的 :name, :name.property 参数, 将值写入列表, 并且返回包含 '?' 的 SQL 语句。
     * 
     * @param sql - SQL 语句
     * @param parameters - [in] 传入的参数
     * @param values - [out] 填充的参数列表
     * 
     * @return 包含 '?' 的 SQL 语句
     */
    private String resolveParam(String sql, Map<String, ?> parameters, final List<Object> values) {

        // 匹配符合  :name 格式的参数
        Matcher matcher = NAMED_PARAM_PATTERN.matcher(sql);
        if (matcher.find()) {

            StringBuilder builder = new StringBuilder();

            int index = 0;

            do {
                // 提取参数名称
                String name = matcher.group(1);
                if (NumberUtils.isDigits(name)) {
                    name = matcher.group();
                }

                Object value = null;

                // 解析  a.b.c 类型的名称 
                int find = name.indexOf('.');
                if (find >= 0) {

                    // 用  BeanWrapper 获取属性值
                    Object bean = parameters.get(name.substring(0, find));
                    if (bean != null) {
                        BeanWrapper beanWrapper = new BeanWrapperImpl(bean);
                        value = beanWrapper.getPropertyValue(name.substring(find + 1));
                    }

                } else {
                    // 直接获取值
                    value = parameters.get(name);
                }

                // 拼装查询语句
                builder.append(sql.substring(index, matcher.start()));

                if (value instanceof Collection<?>) {

                    // 拼装 IN (...) 的查询条件
                    builder.append('(');

                    Collection<?> collection = (Collection<?>) value;

                    if (collection.isEmpty()) {
                        builder.append("NULL");
                    } else {
                        builder.append('?');
                    }

                    for (int i = 1; i < collection.size(); i++) {
                        builder.append(", ?");
                    }

                    builder.append(')');

                    // 保存参数值
                    values.addAll(collection);

                } else {
                    // 拼装普通的查询条件
                    builder.append('?');

                    // 保存参数值
                    values.add(value);
                }

                index = matcher.end();

            } while (matcher.find());

            // 拼装查询语句
            builder.append(sql.substring(index));

            return builder.toString();
        }

        return sql;
    }

}
