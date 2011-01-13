package net.paoding.rose.mock.controllers.resolver;

import java.util.Date;
import java.util.List;

import net.paoding.rose.web.Invocation;
import net.paoding.rose.web.annotation.rest.Get;

/**
 * 模拟一个controller方法中的所有参数都没有标注@Param的情况
 * 
 * @author Li Weibo (weibo.leo@gmail.com) //I believe spring-brother
 * @since 2011-1-12 下午07:23:10
 */
public class NoParamAnnotationController {
	
	@Get
	public Object justgo(Invocation inv, int hostId, List<Object> objects, String style,
            Date start, Date end) {
        return "@done";
    }
	
}
