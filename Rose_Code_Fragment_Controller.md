本页面主要展示一些常用的控制器类代码片段，以期能够通过这些案例了解控制器的编写要领



# 1、起步 #

http://127.0.0.1/user/test

```
// 控制器必须声明在controllers或其子package下，注意controllers后面有个s
package com.xiaonei.rose.usage.controllers;

// 控制器要以Controller结尾，可不继承或实现其他类或接口
public class UserController {

   public String test() {
       // 返回@开始的字符串，表示将紧跟@之后的字符串显示在页面上
       return "@" + new java.util.Date();
   }
}
```

# 2、返回一个velocity页面 #

http://127.0.0.1/user/velocity

```
public class UserController {

   public String velocity() {
       // 返回一个普通字符串，表示要从webapp/views/目录下找第一个以user-velocity.开始的页面
       // 运行本程序时，请在webapp/views/目录下创建一个名为user-velocity.vm的文件，写上写文本字符
       return "user-velocity";
   }
}
```
# 3、返回一个jsp页面 #

http://127.0.0.1/user/jsp

```
public class UserController {

   public String jsp() {
       // 在webapp/views/目录下创建user-jsp.jsp的文件即可 (UTF-8的)。
       return "user-jsp";
   }
}
```

# 4、在页面渲染业务数据 #

http://127.0.0.1/user/render

```
import net.paoding.rose.web.Invocation;

public class UserController {

   public String render(Invocation inv) {
       // 在vm/jsp中可以使用$now渲染这个值
       inv.addModel("now", new java.util.Date());
       // 在vm/jsp中可以使用$user.id, $user.name渲染user的值
       inv.addModel("user", new User(1, "qieqie.wang")); // id=1, name=qieqie.wang
       return "user-render";
   }
}
```

# 5、更改控制器映射 #

http://127.0.0.1/u/test

```
import net.paoding.rose.web.annotation.Path;

// 在控制器上标注@Path设置"u"，自定义映射规则(默认是/user，现改为/u)
@Path("u")
public class UserController {
    public String test() {
        return "@" + new java.util.Date();
    }
}
```

# 6、重定向(Redirect) #
http://127.0.0.1/user/redirect

```
public class UserController {

   public String redirect() {
       // 以r:开始表示重定向
       return "r:/user/test"; // 或 r:http://127.0.0.1/user/test
   }
}
```

# 7、转发(Forward) #
http://127.0.0.1/user/forward <br>
<a href='http://127.0.0.1/user/forward2'>http://127.0.0.1/user/forward2</a>

<pre><code>public class UserController {<br>
<br>
   public String forward() {<br>
       // 大多数情况下，以/开始即是转发(除非存在webapp/user/test文件)<br>
       return "/user/test"; <br>
   }<br>
<br>
   public String forward2() {<br>
       // a:开始表示转发到同一个控制器的acton方法forward()，更多参数有m:/module:,c:/controller:<br>
       return "a:forward?note=可以带参数"; <br>
   }<br>
}<br>
</code></pre>

<b>下一页</b>  <a href='Rose_Code_Fragment_Controller2.md'>控制器类代码2</a>