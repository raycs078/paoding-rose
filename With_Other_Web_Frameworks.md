# 如何在已有的web应用中使用rose #

## 内容介绍 ##

（略）


## Spring集成问题 ##

如果您在web项目中使用了Spring容器(不限制Spring MVC)，您一定在web.xml配置了ContextLoaderListener或ContextLoaderServlet，通常您还会配置一个名为contextConfigLocation的context-param表示Spring容器的地址，假如就是这样的话，为了在已有的Spring MVC或Struts等框架使用Rose，你需要这样改造：


  * 从web.xml中去掉ContextLoaderListener、ContextLoaderServlet，RoseFilter会充当ContextLoader功能，把Spring环境生成好，按照Spring的规范放好，使得Spring MVC或Struts可以用到；
  * 去掉contextConfigLocation配置，Rose默认会读取WEB-INF、WEB-INF/classes下的applicationContext开头的xml文件，把他们视为Spring配置文件；
  * 如果原来您使用的spring配置文件不是applicationContext开始的，请更改过来即可，文件命名可以是applicationContext.xml、applicationContext-amodule.xml等；
  * 如果原来您使用的配置文件并不直接在WEB-INF或WEB-INF/classes下的(比方是`classpath:/spring/*.xml`)，那么您可以有两种选择
    1. 把classpath:/spring下的配置文件改到WEB-INF或WEB-INF/classes下，并命名为以applicationContext开始的xml文件；
    1. 保持contextConfigLocation配置为`classpath:/spring/*.xml`，此时Rose还会读取WEB-INF/classes下的`applicationContext*.xml`，但不会再读取WEB-INF下的`applicationContext*.xml`，除非你设置contextConfigLocation为`classpath:/spring/*.xml,/WEB-INF/applicationContext*.xml`
  * 总之，无论如何Rose总是会读取WEB-INF/classes下的，也就是说：如果你没有设置contextConfigLocation，Rose还会自动读取WEB-INF/classes的。如果你设置了contextConfigLocation，则Rose也将读取WEB-INF/classes下的，所以为了不多次重复读取WEB-INF/classes下的配置，contextConfigLocation一定不要再设置`classpath:applicationContext*.xml`之类的配置，您需要把`classpath:applicationContext*.xml`的去掉，其他的保留)

> Rose的Spring读取策略不仅仅在此，如果您遇到问题，请发issure或邮件qieqie.wang#gmail.com

## RoseFilter的问题 ##

  * 首先只要做好以上Spring的工作，RoseFilter是可以和任何框架共存的
  * RoseFilter在配置上，一般要是所有Filter的最后一个，已确认Rose的请求能够走完web.xml中的所有Filter
  * struts2下，roseFilter则不要是最后一个，要在struts的filter之前，这样才能保证rose优先匹配；
  * RoseFilter在web.xml的配置，一定要按照RoseFilter.java javadoc注释说明的那样配置，既配置url-pattern为`/*`，请不要配置`*.do、*.abc`之类的，这样配置对整个web来说并没有提高性能，而且可能导致错误。即使您真的只有/admin采用rose开发,也不要配置为`/admin/*`之类的配置，这对rose的有效使用是不利的[长话以后再说，这里先亮结论]。总之，rose有优秀匹配算法，配置成`/*`不会浪费性能。
  * 当一个请求确定是Rose应该处理的，那么他就不会在分发给Servlet处理了。这样，就做到了Rose和其他Servlet框架共存了，而且是Rose优先的。