package net.paoding.rose.mock.resolvers;

import net.paoding.rose.web.annotation.ParamResolver;

@ParamResolver(BeanResolver.class)
public interface Bean {

    public String get();

}
