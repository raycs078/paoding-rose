Jade DAO规范



# 简单的DAO接口 #

一个Jade DAO首先需要符合以下基本要求：
<br>
1、	在dao package或子package 下，如com.renren.myapp.dao；<br>
2、	是一个public的java interface 类型；<br>
3、	名称必须以大写DAO字母结尾，如UserDAO；<br>
4、	必须标注@DAO 注解；<br>
5、	不是其它类的内部接口；<br>


示例一：<br>
<pre><code>package com.renren.myapp.dao;<br>
@DAO<br>
public interface UserDAO {<br>
}<br>
</code></pre>


示例二：<br>
<pre><code>Package com.mycompany.myapp.dao.subpackage;<br>
@DAO<br>
public interface CustomerDAO {<br>
}<br>
</code></pre>

<h1>具有继承关系的DAO接口</h1>

一个接口(ChildDAO)可以扩展继承另外一个接口(ParentDAO)，只要ChildDAO符合《简单的DAO接口》的规定，它就是一个合格的Jade DAO接口类。<br>
如果ChildDAO没有标注@DAO，但其上级接口(包含祖先)标注了@DAO，则也被认为是标注了@DAO。<br>
<br>
示例一：<br>
<pre><code>package com.renren.myapp.dao.subpackage;<br>
import com.renren.myapp.dao.UserDAO;<br>
<br>
@DAO<br>
public interface ManagerDAO extends UserDAO {<br>
}<br>
</code></pre>


示例二：<br>
<pre><code>package com.renren.myapp.dao.subpackage2;<br>
import com.renren.myapp.dao.UserDAO;<br>
import com.renren.myapp.dao.subpackage.CustomerDAO;<br>
<br>
// ComplexDAO没有标注@DAO，但它是一个合法的Jade DAO 接口； <br>
public interface ComplexDAO extends UserDAO, CustomerDAO {<br>
}<br>
</code></pre>

如没有特殊指明，下文简单地使用DAO指代符合Jade 规范的DAO接口。<br>
<br>
<h1>DAO方法</h1>
一个DAO接口可以不限制地定义自己的方法，但只有标注 @SQL 注解的方法才被Jade识别为可以查询数据库的、真正的DAO方法。<br>
<br>
示例一：<br>
<pre><code>@SQL("SELECT id, name FROM user LIMIT 1")<br>
public User findOne();<br>
</code></pre>


示例二：<br>
<pre><code>@SQL("UPDATE user SET name=:2 WHERE id=:1")<br>
public void updateName(long userId, String userName);<br>
</code></pre>

<h1>DAO方法规范</h1>
1、	必须标注 @SQL 注解;<br>
2、	返回类型可以是void，也可以根据不同应用情况进行声明，详看《返回类型》；<br>
3、	方法名称不限，但应该使用符合人类思维的名字以体现我们是正常的人类；<br>
4、	方法参数列表类型、顺序不限，但是应该要和 @SQL 中的 sql 语句的站位符对应起来；<br>
5、	方法参数如果是数组、集合、Map、Java Bean的，有特别的意义，详看《方法参数》；<br>
6、	可选声明抛出spring框架中的DataAccessException；<br>


示例一：<br>
<pre><code>@SQL("SELECT id, name FROM user WHERE name LIKE :1")<br>
public List&lt;User&gt; findByNameLike(String likeString) throws DataAccessException;<br>
</code></pre>


示例二：<br>
<pre><code>@SQL("DELETE user WHERE id in (:1)")<br>
public int deleteUser(long[] userId) throws DataAccessException;<br>
</code></pre>

<h1>两种SQL类型</h1>

Jade将 SQL语句分为两种类型："查询语句"和"非查询语句"，更进一步的，Jade默认只把以 SELECT 、SHOW、DESC、DESCRIBE 开始的 SQL 语句才识别为查询语句。<br>
对于查询语句，Jade 会期望数据库返回一个结果集 ；非查询语句的，Jade 只是期望数据库返回一个更新的条目数。<br>
对于存储过程，如果没有特别指定 Jade 会把它当成非查询类型。<br>

<h1>@SQL的基本用法</h1>

@SQL用于标注在 DAO 方法上，表示这是一个 DAO 方法，同时说明执行这次 DAO 方法调用所要执行的 SQL 语句。<br>
可以在 @SQL 中写入完整的一个 SQL 语句：<br>

<pre><code>@SQL("UPDATE user SET name=’Mr. Right’ WHERE id=3")<br>
</code></pre>

或者是一个含有变量的 SQL 语句，此时需要使用 ":1"、":2"类似的符号表示一个变量，其中1、2表示方法的第i个参数(i=1、2、3 ……)：<br>
<br>
<br>
<pre><code>@SQL ("UPDATE user SET name=:2 WHERE id=:1")<br>
public void updateName(long userId, String userName);<br>
</code></pre>
<br>

也可以使用:id, :name的形式，但这需要在具体的方法参数前加 @SQLParam 注解进行说明：<br>
<pre><code>@SQL ("UPDATE user SET name=:name WHERE id=:1")<br>
public void updateName(long userId,  @SQLParam("name") String userName);<br>
</code></pre>


如果要执行一个存储过程，亦可以写到@SQL中，但需要该存储过程返回结果集的，则可通过 type 来指定SQL 类型：<br>
@SQL(value = "…" type = SQLType.READ")<br>
<br>
<h1>DAO 方法的返回类型</h1>

书写 DAO 方法的返回类型时要讲究，要根据不同的 SQL 类型做不同的处理。<br><br>

<h2>非查询类型语句</h2>

非查询类型的 DAO 方法，Jade 只期待数据库返回一个整型数，表示此次执行数据库变更了多少行的记录。<br>
<br>
支持的类型：<br>
整型类型：int、Integer、boolean、Boolean、long、Long、byte、Byte、short、Short<br>
浮点类型：float、Float、double、Double<br>
精类型的：java.math.BigInteger、java.math.BigDecimal<br>
特有类型：net.paoding.rose.jade.core.Identity<br>
其它类型：java.lang.Number、void<br><br>

对其它不支持的类型，1.0版本将以 null 作为返回，以后版本或许有可能直接抛出异常。<br>

如果你的 SQL 语句是一个插入语句，并且会产生一个自增 ID的，可通过声明返回类型为Idenity 明确返回该自增 ID，而非"记录变更数目"。<br>
<br>
<h2>查询类型语句</h2>

查询类型的 DAO 方法的返回类型虽然力求简单、明了，但总体比较丰富，也比较复杂：<br>
<table><thead><th> <b>方法返回类型</b>	</th><th> <b>适用的情况</b> </th></thead><tbody>
<tr><td>List<code>&lt;User&gt;</code><br>Set<code>&lt;User&gt;</code><br>User<a href='.md'>.md</a> </td><td>	返回任意行，每行单列或多列的语句<br>SELECT id, name, age FROM user<br>SELECT id, name, age FROM user WHERE name LIKE :1</td></tr>
<tr><td>List<Map<String, ?>>	</td><td> 返回任意行，每行单列或多列的语句；<br>每一行映射为一个Map，列名为key，列值为value(具体类型不同列不同)<br>SELECT id, name, age FROM user</td></tr>
<tr><td>User<br>Person <br>Student </td><td>	返回最多1行的语句；若返回0行，DAO 方法返回null给调用者；<br>如果实际结果集含有>1行，抛IncorrectResultSizeDataAccessException<br>SELECT id, name, age FROM user WHERE id=:1<br> SELECT id, name, age FROM user LIMIT 1 </td></tr>
<tr><td>long <br> int<br> boolean<br> (基本类型) </td><td>返回1行，每行1列的语句；<br>如果返回0行，抛EmptyResultDataAccessException<br>如果返回>1行，抛IncorrectResultSizeDataAccessException<br>SELECT id FROM user WHERE id > :1 LIMIT 1<br>SELECT age FROM user WHERE id= :1<br>SELECT locked FROM user WHERE id=:1<br></td></tr>
<tr><td>Long<br> Integer<br> String<br> (封箱类型)<br> byte<a href='.md'>.md</a> </td><td>	返回最多1行，每行1列的语句；若返回0行，DAO 方法返回null给调用者；<br>如果实际结果集含有>1行，抛IncorrectResultSizeDataAccessException<br>SELECT id FROM user WHERE id > :1 LIMIT 1<br>SELECT age FROM user WHERE id= :1<br>SELECT bin_column FROM user WHERE id=:1 <br>// bin_column列可以是byte<a href='.md'>.md</a>、blob类型的等 </td></tr>
<tr><td>List<code>&lt;Integer&gt;</code><br> Set<code>&lt;Long&gt;</code><br> int<a href='.md'>.md</a> / Integer<a href='.md'>.md</a><br> long<a href='.md'>.md</a> / Long<a href='.md'>.md</a><br> Byte<a href='.md'>.md</a><br> (不包含byte<a href='.md'>.md</a>)	</td><td> 返回任意行，每行1列的语句；<br>SELECT id FROM user;<br>SELECT age FROM user LIMIT 100;<br>SELECT byte_column FROM user LIMIT 50;<br></td></tr>
<tr><td>Map<Long, String>	</td><td> 返回任意行，每行2列，前一列为key，后一列为value的结果<br>SELECT id, name FROM user;</td></tr>
<tr><td>Map<Long, User>	</td><td> 返回任意行，每行不限制列，第一列为key，整行为value的结果<br>SELECT id, name, age FROM user;</td></tr></tbody></table>

<h1>Bean映射规则(默认)</h1>
DAO 方法可以声明返回一个 Bean 或者 Bean 的数组或集合，以下陈述映射规则。<br>
<br>
示例一：<br>
<br>
<pre><code>@SQL("SELECT id, first_name, flag_2,three_words_column FROM user LIMIT :1, :2")<br>
public List&lt;User&gt; find(int offset, int maxSize);<br>
</code></pre>

<table><thead><th><b>列</b></th><th><b>Bean</b> </th></thead><tbody>
<tr><td>id	            </td><td>	public void setId(Xxx id)</td></tr>
<tr><td>first_name</td><td>	public void setFirstName(Xxx firstName)</td></tr>
<tr><td>flag_2  </td><td>public void setFlag2(Xxx flag2) </td></tr>
<tr><td>three_words_column </td><td>	public void setThreeWordsColumn(Xxx threeWordColumn)</td></tr></tbody></table>

总结如下：<br>
<ul><li>Jade会找相应的set方法，将值设置给Bean对象；<br>
</li><li>如果列名是一个普通字符串，即不包含下划线、数字的列，如id，将首字母大写拼在set后：setId；<br>
</li><li>如果列名含有下划线的，将下划线后的字母变为大写，first_name  --> setFirstName<br>
</li><li>列名含有数字，且数字前没有下划线<br>
<ul><li>1.0.2版本：name2将映射给bean的name2属性了，user1_name2以及user_1_name_2列都能映射到user1Name2属性，但是user1_name_2不能映射给任何属性,必须写user1_name_2 as user1_name2。<br>
</li><li>1.0.1版本：name2(列名含有数字，且数字前没有下划线)，这样的字符串无法映射给任何 bean 属性，对此默认情况下jade会抛出InvalidDataAccessApiUsageException 异常；这要通过as来做适配：SELECT name2 as name_2 FROM user，这样才能映射给bean的name2属性<br></li></ul></li></ul>

<br>
提醒一下的是，Jade使用默认使用的映射不仅限于此，但我强烈建议各位开发者严格按照这个规则来处理映射，因为哪一天Jade将更加严格(如果做了严格限制，一旦映射失败会提示InvalidDataAccessApiUsageException异常)。<br>
<br>
<h1>自定义 Bean 映射</h1>

示例一：使用自定义的UserRowMapper<br>
<br>
<pre><code>@SQL("SELECT id, name FROM user WHERE name LIKE :1")<br>
@RowHandler(rowMapper = UserRowMapper.class)<br>
public List&lt;User&gt; findByNameLike(String likeString) throws DataAccessException;<br>
<br>
public class UserRowMapper implements RowMapper {<br>
public Object mapRow(ResultSet rs, int rowNum) throws SQLException {<br>
    // code here<br>
}<br>
}<br>
</code></pre>

<h1>方法参数</h1>

方法参数可以是和数据库列类型相对应Java类型，比如String、int、Integer、long、Long、java.util.Date、java.sql.Date等，也支持以下类型：数组、List、Set、Map、Java Bean。<br>
<br>
<h2>批量操作：使用List作为方法的第一个参数</h2>

<pre><code>@SQL("UPDATE XXX_TABLE set name=:1.name WHERE id=:1.id")<br>
public void changeNames(List&lt;User&gt; userList)；<br>
userDAO.changeNames(aUserListHere); <br>
</code></pre>


<h2>in语句的写法：使用数组或集合类型作为参数的</h2>

集合类型指实现Collection接口的一些类型，比如List、Set等。集合类型、数组 参数用于处理 in 的情况，而且这个数组或集合类型的元素应该是一些数据库列相对应的Java类型：<br>
<br>
示例一<br>
<pre><code>@SQL("SELECT name WHERE id IN (:1)")<br>
public String[] findNames(long[] idArray);<br>
</code></pre>


示例二<br>
<pre><code>@SQL("SELECT name WHERE id IN (:1)")<br>
public String[] findNames(List&lt;Long&gt; idList); <br>
</code></pre>

实际执行SQL调用时，将根据数组、集合类型的长度重写SQL语句，比如，当传入的是一个长度为3的数组时候，调用<br>
<br>
<pre><code>userDAO.findNames(new long[]{1,2,3});<br>
</code></pre>
Jade将把SQL语句改写为由3个问号组成的语句：<br>
<pre><code>SELECT name WHERE id IN (?, ?, ?)<br>
</code></pre>


注意，IN语句的查询不能这样处理：<br>
<br>
<pre><code>@SQL("SELECT name FROM XXX_TABLE WHERE id IN (:1)")<br>
public String[] findNames(String idList)；<br>
userDAO.findNames("1,2,3"); <br>
</code></pre>

<h2>Bean作为参数的</h2>
通过 ’.’ 号来使用Bean 的属性，将其作为 SQL 参数。<br>

示例一: 使用 :1.id、:1.name、:1.age<br>
<br>
<pre><code>@SQL("INSERT user (id, name, age) VALUES (:1.id, :1.name, :1.age)")<br>
public void save(User user);<br>
</code></pre>


<h2>Map作为参数的</h2>
通过方括号来使用 Map 的值，将其作为 SQL 参数。<br>
<br>
示例一: 使用  :1<a href='id.md'>id</a> :1<a href='name.md'>name</a>  :1<a href='age.md'>age</a>

<pre><code>@SQL("INSERT user (id, name, age) VALUES (:1[id], :1[name], :1[age])")<br>
public void save(Map&lt;String, ?&gt; userProperties);<br>
</code></pre>

<h2>数组元素</h2>
类似Map的使用，通过方括号来使用 数组 的值，将其作为 SQL 参数。<br>
<br>
示例一: 使用   :1<a href='0.md'>0</a> :1<a href='1.md'>1</a>  :1<a href='2.md'>2</a>
<pre><code>@SQL("INSERT user (id, name, age) VALUES (:1[0], :1[1], :1[2])")<br>
public void save(Object[] userProperties);<br>
</code></pre>