

# 定义 #
在Rose框架中，控制器Controller是指扮演接收处理web请求职责的对象类。控制器接收web请求传入的参数、调度服务处理业务、将处理结果设置到model中，返回一个指示(页面)。

在Rose框架中，控制器是application-scope的，这和有些框架把控制器做成request-scope的对象是不一样的。

# 创建控制器类 #
  * 控制器类package名规定：控制器只能放置在controllers或其子package下，controllers上可以有任意的父package，比如com.xiaonei.xxx.yyy.controllers

  * 控制器类类名规定：控制器命名应该以Controller结束，Controller前可以由任意字符组成(数字、下划线、字母)，比如UserController, OrderController

  * 命名惯例：按照REST等建议，控制器命名应采用名词，如TopicController；方法命名应采用动词，如show, list, update, add, edit等

  * 类修饰符：应写成public class，即：只能是public的，不能是abstract或interface
```
public class UserController {

}
```

  * 构造子：控制器必须默认或明确提供无参数的构造子

# 控制器映射规则 #
  * 默认映射规则：去掉后缀，并把头字母小写化
```
/user --> UserController
/userXxx --> UserXxxController
```

  * 自定义映射规则：@Path("user-xxx") 可修改UserXxxController的映射规则变为：/user-xxx --> UserXxxController；使用自定义映射后，原默认的映射规则不再存在

  * 可使用[正则表达式](Rose_Guide_ReqMapping.md)定义映射规则： @Path(`"user-{id:[0-9]+"`})

  * 可写@Path({"xxx", "yyy"})实现多个地址都映射到该控制器

# 控制器域成员(Field) #
  * 控制器是以单例的形式存在的，对于request-scope的信息，不能使用field记录

  * 一般情况下除非是控制器对下层有 **服务** 或 **资源** 有依赖或有全局性的字段的必要，否则不书写field字段

  * 如果对下层有服务或资源的依赖，可直接在字段上并标注Spring提供的@Autowired注解
    * 例如：**@Autowired** private UserService userService;
    * 也可以通过在setUserService(UserService userServer)方法上注@Autowired实现

  * 如果域成员是一个拦截器或拦截器数组、集合，这个域的拦截器将拦截到此控制器的web请求

  * 可以在控制器类中声明@Autowired private **InvocationLocal inv** ;这样可省去在method中声明Invocation inv；通过inv.addModel()可以向页面传递Value Object

# 控制器构造子成员(Constructor) #
  * 必须有一个无参构造子(默认或明确书写)

  * 无需其他构造子

# 控制器方法成员(Method) #
  * 控制器可以包含任何形式、名称的Method，和就像普通类一样

  * web请求可映射到控制器由其方法来处理，这类方法称为action方法
    * 并不是控制器的所有方法都可以处理web请求

  * 只有在本控制器类声明的方法才有可能成为action方法，除非父类标注了@AsSuperController

  * 静态方法、非public的方法均不是action方法

  * toString, hashCode, equals，wait，notify, notifyAll, finalize方法不是action方法，除非他们标注了@Get

  * public Yyy getXxx(), public boolean/Boolean isXxx()不是action方法，除非他们标注了@Get

  * public void setXxx(Yyy y)方法不是action方法，除非他们标注了@Post

  * 标注@Ignored的方法，不是action方法

  * 除以上范围之外的本类声明方法即是所属控制器的action方法

# action方法规范 #
  * 就像普通方法一样：public 返回类型 方法名(参数列表) 异常声明 {方法体语句}
```
@Get("{id}")
public Object show(@Param("id") int id) throws Exception {
    return "user-show";
}
```

  * public 是必须的，否则不是action方法

  * 返回类型通常应该是String类型(因为大多数web请求都是为了输出文本)
    * 返回的值表示一个页面名称
    * 如果返回的值以r:开头表示redirect
    * ...

  * 返回类型也可以是
    * 基本类型或其封装类型，比如int， Integer，页面将直接显示其文本
    * byte数组, InputStream类型，用于输出流；
    * StringBuilder/Buffer, 普通对象的，用于输出他们的toString()结果

  * 方法名按照通常Java规范遵守即可，一般应该是一个“动词”，表示对这个控制器名词类的一种操作

  * 参数可包括以下类型：Rose内置参数、简单类型、数组类型、集合类型、接口类型、对象类型
    * 参考下面的[action方法参数规范](http://code.google.com/p/webrose/wiki/Rose_Guide_Controller#action方法参数规范)

  * 可以声明抛出Exception等异常

  * 方法体语句按照普通的Java规范遵守即可，可以调用Invocation的addModel(name, value)方法设置要渲染的数据

  * 方法体语句可以调用控制器依赖的服务请求处理事务，比如xxxServler.doBussness();

  * 对只和单个请求相关数据，不要赋值给控制器Field字段(InvocationLocal inv除外)

# action方法命名建议(建议而已) #
| public String add() { }    | 创建增加信息         |
|:---------------------------|:---------------|
| public String show() { }   | 显示指定ID的对象信息    |
| public String list() { }   | 条件查询，分页显示      |
| public String edit() { }   | 编辑修改信息         |
| public String delete() { } | 删除信息、注销信息      |

# action方法映射规则 #
  * 默认映射规则： /list --> public [返回类型] **list**(参数列表) 异常声明 方法体

  * 自定义映射规则：@Get("query")或@Post("query") 可修改方法映射规则变为：/query --> 到所在的方法，原默认的映射规则不再存在；此时方法名不再参与映射规则判断中

  * 可使用[正则表达式](Rose_Guide_ReqMapping.md)定义映射规则： @Get(`"user/{id:[0-9]+"`})等

  * 可写@Get({"xxx", "yyy"})实现多个地址都映射到该方法

  * 可定义@属性，只让所列的请求方法才映射到该action方法
  * 可有2个方法名一样的action方法，但是需要通过@Get、@Post等注解的值区别开来

# action方法参数规范 #

  * action方法的参数的值将由Rose调用时提供

  * action参数可包括以下几种类型：Rose内置的参数、基本类型参数、Bean参数等等

  * 基本类型参数应标注@Param("id")，其值将通过解析查询串或URI串的转化而来
    * 假设UserController有这个方法：
```
   @Get("{id}")
   public String abc(@Param("id") int id, @Param("name") String name) {

   }
```
    * 使用这个地址http://localhost/user/123?name=renren 请求时，id的值将是123，name的值将是renren

  * Bean参数通常是用来存放提交的表单的数据的，一般不用写@Param

  * Bean参数默认就已经放到model中了，其key是该bean的类名的头字母小写串(User --> user)
    * 标注@Param可以用来改变key


# action方法参数说明表 #
|**内置参数**|            | |
|:-------|:-----------|:|
|          | Invocation inv| Rose封装与本次调用环境相关的对象，以下的内置参数都可以通过inv的方法获取到 |
|          | Model model     | 同 inv.getModel()； 将对象add到model中，可在页面中使用类似"{user.id}"的表达式|
|          | ServletContext  | 同 inv.getServletContext()；当前ServletContext对象|
|          | ServletRequest| 当前请求对象|
|          | HttpServletRequest | 当前请求对象|
|          | ServletResponse| 当前相应对象|
|          | HttpServletResponse  |  当前响应对象|
|          | HttpSession         | 当前会话，如果当前没有会话传入null，除非标准@Param(value = "session", required=true)|
|          | MultipartFile| 上传的文件|
|          | MultipartHttpServletRequest |  上传请求对象|
|          | BindingResult| 绑定对象|
|          | Errors         | 绑定对象|
|          | Flash         | Flash对象，用于redirect前后传递信息|
|**简单类型**|            |
|          | int/long/float/double/byte/short/boolean/char| 如果解析失败，值为0, false，'\0'|
|          | Integer/Long/Float/Double/Byte/Short/Boolean/Character|如果解析失败,值为null  |
|**数组类型**|            |
|          | int[.md](.md)/long[.md](.md)/float[.md](.md)/double[.md](.md)/byte[.md](.md)/short[.md](.md)/boolean[.md](.md)/char[.md](.md) |如果解析失败，值为null，哪怕只有数组的某一项解析失败 |
|          | Integer[.md](.md)/Long[.md](.md)/Float[.md](.md)/Double[.md](.md)/Byte[.md](.md)/Short[.md](.md)/Boolean[.md](.md)/Character[.md](.md)|如果解析失败，值为null  |