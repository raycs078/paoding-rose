package net.paoding.rose.jade.jadeinterface.app;

/**
 * 定义 Jade Dao (Data Access Object) 的工厂接口。
 * 
 * @author han.liao [in355hz@gmail.com]
 */
public interface DaoFactory {

    /**
     * 获取工厂创建的 Dao (Data Access Object) 对象。
     * 
     * @param <T> - 泛型参数: Dao 对象的类型
     * 
     * @param daoClass - Dao 对象的接口
     * 
     * @return 创建的 Dao 对象
     */
    public <T> T getDao(Class<T> daoClass);
}
