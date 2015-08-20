数据源配置



# 说明 #

按照jade要求，应用程序只需要在spring配置文件中按规范配置数据源，即能够被jade使用。

假如你有一个dao接口为com.xiaonei.xyz.UserDAO，那么jade默认情况下，按照以下规则来为此DAO配置DataSource：

  1. 是否有id为jade.dataSource.com.xiaonei.xyz.UserDAO的bean? 如有，则该DAO使用此DataSource
  1. 是否有id为jade.dataSource.com.xiaonei.xyz的bean? 如有，则该DAO使用此DataSource
  1. 是否有id为jade.dataSource.com.xiaonei的bean? 如有，则该DAO使用此DataSource
  1. 是否有id为jade.dataSource.com的bean? 如有，则该DAO此使用DataSource
  1. 是否有id为jade.dataSource的bean? 如有，则该DAO使用此DataSource
  1. 是否有dataSource的bean? 如有，则该DAO使用此DataSource

如果没有以上任何一个配置，jade将抛出异常：
not found dataSource for catalog: ''; you should set a dataSource bean  (with id='jade.dataSource[.daopackage[.daosimpleclassname]]' or 'dataSource' )in applicationContext for this catalog.

也因此，不同的DAO实际可以有不同的DataSource配置。

# 配置示例 #

  * 在WEB-INF下创建`applicationContext*.xml (*代表任何字符或无字符)`

  * 示例配置内容：
```
<bean id="dataSource" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
  <property name="driverClass" value="com.mysql.jdbc.Driver"></property>
  <property name="url" value="jdbc:mysql://localhost:3306/dbname"></property>
  <property name="username" value="usernamehere"></property>
  <property name="password" value="passwordhere"></property>
</bean>

```