拦截器代码参考




# "跟踪"拦截器 #

```
// 直接放在controllers或其子package下(也可以放到其他package下，但需要applicationContext配置)
package com.xiaonei.rose.usage.controllers;

public class TrackInterceptor extends ControllerInterceptorAdapter {
    // 调用控制器action方法之前,before方法被Rose调用
    @Override
    public Object before(Invocation inv) throws Exception {
        // 打印一个日志，看看即将要调用哪个控制器的哪个方法
        System.out.println("invoking " + inv.getControllerClass().getName() + "." + inv.getMethod().getName());
        // 返回true，表示继续下一个拦截器
        return true;
    }

    // 调用控制器action方法之后,after方法被Rose调用
    @Override
    public Object after(Invocation inv, Object instruction) throws Exception {
        // 调用结束后，打印一个结果
        System.out.println("return " + instruction + " by " + inv.getControllerClass().getName() + "." + inv.getMethod());
        // instruction是控制器或上一个拦截器返回的
        return instruction;
    }
}
```

# 登录验证拦截器 #
```
package com.xiaonei.rose.usage.controllers;

public class LoginRequiredInterceptor extends ControllerInterceptorAdapter {

    // 覆盖这个方法，表示只有标注@LoginRequired的控制器或方法才会被此拦截器拦截
    @Override
    public Class<? extends Annotation> getRequiredAnnotationClass() {
        return LoginRequired.class;
    }

    @Override
    public Object before(Invocation inv) throws Exception {
        HttpServletRequest request = inv.getRequest();
        // 在此，假设我们判断是否已经登录的方法是session是否存在
        // 当然，对于互联网应用来说，不能使用默认的这种session机制
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            // 如果没有登录，重定向到登录页面
            return "r:http://localhost/login?origURL=登录后返回的地址";
        }
        return true;
    }
}
```

**LoginRequired.java**
```
@Inherited
@Target( { ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoginRequired {

}

```

# 介绍：拦截器的作用范围 #

rose拦截器实现AOP编程的一个手段，rose将这些拦截器分为2种：局部拦截器和全局拦截器。

## 局部拦截器 ##

> 所谓局部拦截器是指，那些只能作用于某个模块的拦截器。当你把拦截器创建在controllers包或子包下，就意味它是一个局部拦截器；或者你把拦截器器创建在其他包下，但把它作为一个bean配置到controllers包或子包下的applicationContext.xml文件中，这时它也是一个局部拦截器。他被创建的地方或配置的地方称为“所在”的模块。特别的，对于通过配置形式的局部拦截器，其所在的模块可能有多个。

> 局部拦截器只能应用于“所在”的模块或其子模块，其他模块不会看到这个拦截器。需要特别强调的，rose所称“模块”和package是相关的，但不完全一致。不同的package一定属于不同模块，相同的package，但属于不同的jar包、不同的classes地址也是不同的模块。

> 局部拦截器默认可以应用于所在模块的子模块，但可以通过在该拦截器上标识@NotForSubModules禁止这种默认行为。


## 全局拦截器 ##

> 所谓全局拦截器是指，那些不是局部拦截器的其它拦截器，并且作为bean配置到root context中的拦截器。所谓root context默认是由`WEB-INF/applicationContext*.xml、classes/applicationContext*.xml、xxx.jar/applicationContext*.xml`组成的ApplicationContext。

> 如果你的拦截器是公司级别的，希望在多个项目中共用的，就可以考虑提供一个单独的拦截器包，把拦截器创建在这个包中，并把它配置在src/applicationContext-intercetors.xml下。

> 为了使jar文件根目录下的`applicationContext*.xml`能够被rose“认识到”，你需要在xxx.jar/META-INF下创建一个rose.properties文件，并写入一个属性rose=applicationContext 。如果没有这个属性，即使你的xxx.jar包根目录含有`applicationContext*.xml`文件，也不会被rose识别。


## 作用范围与实际拦截的区别 ##

> 当一个拦截器作用于某个范围时，不代表就能够拦截到那个范围内的控制器。每个拦截器可以覆盖getRequiredAnnotationClass、isForAction等方法允许或排除某些控制器或方法，控制器及其方法也可以通过@Intercepted的allow和deny属性进行控制。