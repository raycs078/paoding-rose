package net.paoding.rose.jade.jadeinterface;

/**
 * 返回 DAO 操作生成的对象 ID.
 * 
 * @author han.liao
 */
public class Identity extends Number {

    /**
     * 生成的序列化 UID.
     */
    private static final long serialVersionUID = 6250174845871013763L;

    // 返回的对象  ID.
    protected Number number;

    /**
     * 构造对象容纳返回的对象 ID.
     * 
     * @param number - 返回的对象 ID
     */
    public Identity(Number number) {
        this.number = number;
    }

    @Override
    public int intValue() {
        return number.intValue();
    }

    @Override
    public long longValue() {
        return number.longValue();
    }

    @Override
    public float floatValue() {
        return number.floatValue();
    }

    @Override
    public double doubleValue() {
        return number.doubleValue();
    }

    @Override
    public String toString() {
        return number.toString();
    }
}
