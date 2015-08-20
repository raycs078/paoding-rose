本页面主要展示一些常用的控制器类代码片段，以期能够通过这些案例了解控制器的编写要领



# 1、自定义方法映射 #
http://127.0.0.1/user/list-by-group?groupId=123

```
public class UserController {
    @Get("list-by-group")
    public String listByGroup(@Param("groupId") String groupId) {
        return "@${groupId}";
    }
}
```
# 2、使用正则表达式自定义方法映射 #
http://127.0.0.1/user/list-by-group-abc

```
public class UserController {
    @Get("list-by-group-{groupId}")
    public String listByGroup2(@Param("groupId") int groupId) {
        return "@string-${groupId}";
    }
}
```

http://127.0.0.1/user/list-by-group-123

```
public class UserController {
    @Get("list-by-group-{groupId:\\d+}")
    public String listByGroup3(@Param("groupId") String groupId) {
        return "@int-${groupId}";
    }
}
```

**有问题？**访问 http://127.0.0.1/user/list-by-group-123 打印出"string-123"而非“int-123”?<br>
这是因为listByGroup2和listByGroup3的path定义的"非常一样"，Rose自身无法判断哪个优先级更高(对不起，我们还没找到给正则表达式排序的有效方法)。<br>
在此建议如下：<br>
<ul><li>调整path定义：把第二个path改为list-by-group-n{groupId:\\d+}，然后通过http://127.0.0.1/user/list-by-group-n123 访问它<br>
</li><li>list-by-group-n{groupId} 的优先级高于 list-by-group-{gourpId}，Rose先“问”前者，只有前者不能处理时(abc不是数字，所以其不能处理)，才走后者<br>
</li><li>想知道Rose的判断优先顺序？OK，请访问 <a href='http://localhost/rose-info/tree'>http://localhost/rose-info/tree</a>
</li><li>当然，为安全考虑，rose-info/tree这个地址的可访问性，需要您明确把net.paoding.rose.web.controllers.roseInfo.TreeController的DEBUG log级别打开(通过log级别控制权限，算是我们特有的一种思路)</li></ul>



<h1>3、获取request请求参数</h1>
<a href='http://127.0.0.1/user/param1?name=rose'>http://127.0.0.1/user/param1?name=rose</a> <br>

<pre><code><br>
    public String param1(@Param("name") String name) {<br>
        return "@" + name;<br>
    }<br>
</code></pre>

<a href='http://127.0.0.1/user/param2?name=rose'>http://127.0.0.1/user/param2?name=rose</a>

<pre><code>    public String param2(Invocation inv) {<br>
        return "@" + inv.getRequest().getParameter("name");<br>
    }<br>
</code></pre>

<a href='http://127.0.0.1/user/param3/rose'>http://127.0.0.1/user/param3/rose</a>

<pre><code>    @Get("param3/{name}")<br>
    public String param3(Invocation inv, @Param("name") String name) {<br>
        // request.getParameter()也能获取@ReqMapping中定义的参数<br>
        return "@method.name=" + name + "; request.param.name=" + inv.getRequest().getParameter("name");<br>
    }<br>
</code></pre>


<h1>4、数组参数</h1>
<a href='http://127.0.0.1/usre/array?id=1&id=2&id=3'>http://127.0.0.1/usre/array?id=1&amp;id=2&amp;id=3</a> <br>
<a href='http://127.0.0.1/usre/array?id=1,2,3,4'>http://127.0.0.1/usre/array?id=1,2,3,4</a> <br>
<pre><code>    public String array(@Param("id") int[] idArray) {<br>
        return "@" + Arrays.toString(idArray);<br>
    }<br>
</code></pre>

<h1>5、Map参数</h1>
<a href='http://127.0.0.1/user/keyOfMap?map:1=paoding&map:2=rose'>http://127.0.0.1/user/keyOfMap?map:1=paoding&amp;map:2=rose</a> <br>

<pre><code>    public String keyOfMap(@Param("map") Map&lt;Integer, String&gt; map) {<br>
        return "@" + Arrays.toString(map.keySet().toArray(new int[0]));<br>
    }<br>
</code></pre>

<a href='http://127.0.0.1/user/valuOfMap?map:1=paoding&map:2=rose'>http://127.0.0.1/user/valuOfMap?map:1=paoding&amp;map:2=rose</a> <br>
<pre><code>    public String valueOfMap(@Param("map") Map&lt;Integer, String&gt; map) {<br>
        return "@" + Arrays.toString(map.values().toArray(new String[0]));<br>
    }<br>
</code></pre>
<a href='http://127.0.0.1/user/map?map:1=paoding&map:2=rose'>http://127.0.0.1/user/map?map:1=paoding&amp;map:2=rose</a> <br>
<pre><code>    public String map(@Param("map") Map&lt;Integer, String&gt; map) {<br>
        StringBuilder sb = new StringBuilder();<br>
        for (Map.Entry&lt;Integer, String&gt; entry : map.entrySet()) {<br>
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append("&lt;br&gt;");<br>
        }<br>
        return "@" + sb;<br>
    }<br>
</code></pre>
<h1>6、表单提交</h1>

POST <a href='http://127.0.0.1/user?id=1&name=rose'>http://127.0.0.1/user?id=1&amp;name=rose</a>

<pre><code>    @Post<br>
    public String post(User user) {<br>
         return "@" + user.getId() + "=" + user.getName();<br>
    }<br>
</code></pre>

<h1>7、设置内嵌对象的属性值</h1>


POST <a href='http://127.0.0.1/user?id=1&name=rose&level.id=3'>http://127.0.0.1/user?id=1&amp;name=rose&amp;level.id=3</a>

<pre><code>    @Post<br>
    public String post(User user) {<br>
         return "@" + user.getId() + "; level.id=" + user.getLevel().getId();<br>
    }<br>
</code></pre>

<b>下一页</b> <a href='Rose_Code_Fragment_Controller3.md'>控制器类代码3</a>