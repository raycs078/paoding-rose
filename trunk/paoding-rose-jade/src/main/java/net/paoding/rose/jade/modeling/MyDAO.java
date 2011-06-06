package net.paoding.rose.jade.modeling;

import net.paoding.rose.jade.annotation.DAO;
import net.paoding.rose.jade.annotation.SQL;
import net.paoding.rose.jade.annotation.SQLParam;

@DAO
public interface MyDAO {

    @SQL("select id, name from user where id=:id")
    User get(@SQLParam("id") int id);
}
