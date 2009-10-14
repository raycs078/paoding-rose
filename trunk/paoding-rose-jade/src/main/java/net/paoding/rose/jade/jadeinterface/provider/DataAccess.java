package net.paoding.rose.jade.jadeinterface.provider;

import java.util.List;
import java.util.Map;

import org.springframework.jdbc.core.RowMapper;

/**
 * 数据库访问层的扩展接口。
 * 
 * @author 王志亮 [qieqie.wang@gmail.com], han.liao
 */
public interface DataAccess {

    /**
     * 执行 SELECT 语句。
     * 
     * @param sql - 执行的语句
     * @param parameters - 参数
     * @param rowMapper - 对象映射方式
     * 
     * @return 返回的对象列表
     */
    public List<?> select(String sql, Map<String, ?> parameters, RowMapper rowMapper);

    /**
     * 执行 UPDATE / DELETE 语句。
     * 
     * @param sql - 执行的语句
     * @param parameters - 参数
     * 
     * @return 更新的记录数目
     */
    public int update(String sql, Map<String, ?> parameters);

    /**
     * 执行 INSERT 语句，并返回插入对象的 ID.
     * 
     * @param sql - 执行的语句
     * @param parameters - 参数
     * 
     * @return 插入对象的 ID
     */
    public Number insertReturnId(String sql, Map<String, ?> parameters);
}
