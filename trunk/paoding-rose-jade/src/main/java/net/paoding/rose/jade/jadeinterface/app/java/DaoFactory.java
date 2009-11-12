package net.paoding.rose.jade.jadeinterface.app.java;

/**
 * 定义 Dao 对象的工厂接口。
 * 
 * @author han.liao
 */
public interface DaoFactory {

    /**
     * 获取指定类型的 DAO 对象。
     * 
     * @param <T> - DAO 对象的类型
     * 
     * @param daoClass - DAO 对象的类
     * 
     * @return DAO 对象
     */
    <T> T getDao(Class<T> daoClass);
}
