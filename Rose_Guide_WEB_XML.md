### Rose只要求配置一个Filter即可 ###

```
<?xml version="1.0" encoding="UTF-8"?>
<web-app>

<!--
	- RoseFilter是Rose的最主要配置,也是Rose框架本身的唯一配置了，大多少情况下，按照Rose提供web.xml中配置方式拷贝过来即可，不需要修改。
	- 默认情况下RoseFilter会过滤所有的请求，对请求进行识别，对应该由Controller处理的进行分派，不应该由Controller
	- 处理的则让它简单通过，通往它该去的地方。
	- 这里最重要的2点就是：
	-    1)要保持dispatcher含有FORWARD，INCLUDE;
	-    2)要保证filter-mapping是所有filter-mapping的最后一个
-->
<filter>
	<filter-name>roseFilter</filter-name>
	<filter-class>net.paoding.rose.RoseFilter</filter-class>
</filter>

<filter-mapping>
	<filter-name>roseFilter</filter-name>
	<url-pattern>/*</url-pattern>
	<dispatcher>REQUEST</dispatcher>
	<dispatcher>FORWARD</dispatcher>
	<dispatcher>INCLUDE</dispatcher>
</filter-mapping>

</web-app>
```

### 建议配置日志对象 ###

Rose内部使用apache的commons-logging作为日志输出接口，故Rose并没有强制使用log4j。

但实践上您可能会选择使用log4j，如果如此的话您可以在web.xml配置以下内容，同时按照下面的说明，配置log4j.properties

```
<!--
	- 这个参数告诉Log4jConfigListener Log4J的属性文件位置。
-->
<context-param>
	<param-name>log4jConfigLocation</param-name>
	<param-value>/WEB-INF/log4j.properties</param-value>
</context-param>

<!--
	- 这个就是Log4jConfigListener配置，它可以读取上面配置的log4jConfigLocation等信息，
	- 配置Log4J信息以及进行webapp根地址的暴露(暴露使其成为一个System属性，请搜索
	- log4jExposeWebAppRoot了解相关说明)
-->
<listener>
	<listener-class>org.springframework.web.util.Log4jConfigListener</listener-class>
</listener>
```

### 最简单的log4j.properties ###

在WEB-INF下创建log4j.properties

在开发、测试环境下，建议把net.paoding和org.springframework配置成DEBUG模式：

```
log4j.rootLogger=INFO, stdout

log4j.appender.stdout=org.apache.log4j.ConsoleAppender
log4j.appender.stdout.layout=org.apache.log4j.PatternLayout
log4j.appender.stdout.layout.ConversionPattern=%d %p [%c] - %m%n

log4j.logger.org.springframework=DEBUG
log4j.logger.net.paoding=DEBUG
# set roseInfo INFO to close /rose-inf
log4j.logger.net.paoding.rose.web.controllers.roseInfo=DEBUG
```