Rose 是由 [人人网](http://www.renren.com)、[糯米网](http://www.nuomi.com/i/rT6nZnJ) 提供的、基于Servlet规范、Spring“规范”的开放源代码WEB开发框架。


# 20120922号外：paoding-rose 已经从 maven 转成 gradle 管理，并将她提交到 github https://github.com/Qieqie/paoding-rose, 里面包含了一个完全可运行的sample了，Spring 也升级到3.1.x了 #





# 正文 #
Rose是面向使用Java开发的同仁们的。Rose 提供的各种特性和约束惯例，目的就是为了使您在能够轻松地开发web程序。如果您觉得Grails的想法很好，您不必转向它，Rose可以给您这种感觉，同时基于您对Java的熟悉，您又能更好地控制Rose。

我们希望Rose对各种技术的整合和规范，能使您摆脱犹豫，摆脱选择的困难，规避没有经验带来的开发风险。Rose不仅整合技术，同时还强调最佳实践，甚至包括名称规范。我们不仅仅只是提供技术，我们还会引导您应该如何使用好技术。

Rose规范了对Spring的使用，虽然大部分时间之内，您可能只是使用 @Autowired 即可，大多数时候的确这样也就够了。但 Rose 也允许您放置applicationContext-xxx.xml文件来扩展Rose。

不熟悉Spring的人，不用去重温Spring的知识也能够开始，并书写漂亮的程序！对熟悉Spring的人，则你们可以看到更多。


**线上互联网应用 [每天接受着亿数量级PV的挑战]**

> 【伟大的日子】 2009年08月xx日<br>
<blockquote>人人网公共主页：  <a href='http://page.renren.com'>http://page.renren.com</a> <br>
人人网光良主页：  <a href='http://page.renren.com/600002233'>http://page.renren.com/600002233</a><br><br></blockquote>

<blockquote>【伟大的日子】 2009年11月26日<br>
人人网Home页：    <a href='http://www.renren.com'>http://www.renren.com</a>    <br>
人人网个人页：    <a href='http://www.renren.com/profile.do?id=237391798'>http://www.renren.com/profile.do?id=237391798</a><br><br></blockquote>

<blockquote>【伟大的日子】 2010年06月23日<br>
<blockquote>糯米网：    <a href='http://www.nuomi.com'>http://www.nuomi.com</a>    <br></blockquote></blockquote>

<blockquote>【100%】<br>
<blockquote>你通过浏览器访问到的人人网、糯米网各业务以及子业务均使用rose作为web框架，全中国每天这么多用户不断地点击和挑战，rose值得您信任。</blockquote></blockquote>

<b>第一支程序，5分钟!</b>

<blockquote><a href='Rose_Guide_Getting_Started.md'>第一支程序</a></blockquote>


<b>5分钟之后，一切就已经顺利。您可以开始围观一些别人写的代码以及第二支程序</b>

<blockquote><a href='Rose_Code_Fragment_Controller.md'>控制器类代码</a><br>
<a href='Rose_Guide_Application2.md'>第二支程序</a></blockquote>

<b>Rose原理概要</b>

Rose 是一个基于Servlet规范、Spring“规范”的WEB开发框架。<br>
<br>
Rose 框架通过在web.xml配置过滤器拦截并处理匹配的web请求，如果一个请求应该由在Rose框架下的类来处理， 该请求将在Rose调用中完成对客户端响应. 如果一个请求在Rose中没有找到合适的类来为他服务，Rose将把该请求移交给web容器的其他组件来处理。<br>
<br>
Rose使用过滤器而非Servlet来接收web请求，这有它的合理性以及好处。<br>
<br>
Servlet规范以“边走边看”的方式来处理请求， 当服务器接收到一个web请求时，并没有要求在web.xml必须有相应的Servlet组件时才能处理，web请求被一系列Filter过滤时， Filter可以拿到相应的Request和Response对象 ，当Filter认为自己已经能够完成整个处理，它将不再调用chain.doNext()来使链中下个组件(Filter、Servlet、JSP)进行处理。<br>
<br>
使用过滤器的好处是，Rose可以很好地和其他web框架兼容。这在改造遗留系统、对各种uri的支持具有天然优越性。正是使用过滤器，Rose不再要求请求地址具有特殊的后缀。<br>
<br>
为了更好地理解，可以把Rose看成这样一种特殊的Servlet：它能够优先处理认定的事情，如无法处理再交给其它Filter、Servlet或JSP来处理。这个刚好是普通Servlet无法做到的 ： 如果一个请求以后缀名配置给他处理时候 ，一旦该Servlet处理不了，Servlet规范没有提供机制使得可以由配置在web.xml的其他正常组件处理 (除404，500等错误处理组件之外)。<br>
<br>
一个web.xml中可能具有不只一个的Filter，Filter的先后顺序对系统具有重要影响，特别的，Rose自己的过滤器的配置顺序更是需要讲究 。如果一个请求在被Rose处理前，还应该被其它一些过滤器过滤，请把这些过滤器的mapping配置在Rose过滤器之前。<br>
<br>
RoseFilter的配置，建议按以下配置即可：<br>
<pre><code> 	&lt;filter&gt;<br>
 		&lt;filter-name&gt;roseFilter&lt;/filter-name&gt;<br>
 		&lt;filter-class&gt;net.paoding.rose.RoseFilter&lt;/filter-class&gt;<br>
 	&lt;/filter&gt;<br>
 	&lt;filter-mapping&gt;<br>
 		&lt;filter-name&gt;roseFilter&lt;/filter-name&gt;<br>
 		&lt;url-pattern&gt;/*&lt;/url-pattern&gt;<br>
 		&lt;dispatcher&gt;REQUEST&lt;/dispatcher&gt;<br>
 		&lt;dispatcher&gt;FORWARD&lt;/dispatcher&gt;<br>
 		&lt;dispatcher&gt;INCLUDE&lt;/dispatcher&gt;<br>
 	&lt;/filter-mapping&gt;<br>
</code></pre>
1) 大多数请况下，filter-mapping 应配置在所有Filter Mapping的最后。<br>
2) 不能将 FORWARD、INCLUDE 的 dispatcher 去掉，否则forward、 include的请求Rose框架将拦截不到<br>
<br>
Rose框架内部采用"匹配->执行"两阶段逻辑。Rose内部结构具有一个匹配树， 这个数据结构可以快速判断一个请求是否应该由Rose处理并进行， 没有找到匹配的请求交给过滤器的下一个组件处理。匹配成功的请求将进入”执行“阶段。 执行阶段需要经过6个步骤处理：“参数解析 -〉 验证器 -〉 拦截器 -〉 控制器 -〉 视图渲染 -〉渲染后"的处理链。<br>
<br>
<b>匹配树:</b>
匹配树是一个多叉树，下面是一个例子：<br>
<br>
ROOT<br>
<blockquote>GET="HomeController#index" package="com.xiaonei.xxx.controllers"<br>
/about<br>
GET="HomeController#about" package="com.xiaonei.xxx.controllers"<br>
/book<br>
GET="BookController#list" package="com.xiaonei.xxx.controllers.sub"</blockquote>

<blockquote>POST="BookController#add" package="com.xiaonei.xxx.controllers.sub"</blockquote>

<blockquote>/book/<br>
<blockquote>/book/{id}<br>
<blockquote>GET="BookController#show" package="com.xiaonei.xxx.controllers.sub"<br>
/help<br>
</blockquote></blockquote>GET="HomeController#help" package="com.xiaonei.xxx.controllers"</blockquote>

ROOT代表这是一个根地址，也就是 <a href='http://localhost/'>http://localhost/</a> 代表的地址；<br>
<br>
ROOT的下级有个GET结点，代表对该地址支持GET访问，不支持POST等其它访问，如果进行POST访问将以405错误回应。<br>
<br>
/book代表这是一个/book地址，也就是 <a href='http://localhost/book'>http://localhost/book</a> 代表的地址；<br>
<br>
/book下级有GET、POST两个结点，说明它支持GET和POST方法，根据HTTP语义，GET代表浏览，POST代表追加(向一个集合中追加一个条目)。<br>
<br>
/book下还有/book/地址，这个地址有点特别，它以'/'结尾，但实际它不会被任何地址访问到，rose对http://localhost/book/的处理会将它等价于 <a href='http://localhost/book'>http://localhost/book</a> 。<br>
<br>
这个特别的地址的存在完全是匹配树结构所需导致的，但不对实际匹配有任何坏的影响，所以也没有任何GET、POST等子结点。<br>
<br>
/book/{id}代表是一个/book/123456、/book/654321这样的地址，当然这可以支持正则表达式的。<br>
<br>
大部分情况下，匹配树的结构和实际的URI结构会一致，也因此匹配树的深度并不固定，每一个中间结点或叶子节点都有可能代表一个最终的URI地址，可以处理GET、POST等请求。对于那些匹配树存在的地址，但没有GET、POST、DELETE等子结点的，一旦用户请求了该地址，rose将直接把该请求转交给web容器处理，如果容器也不能处理它，最终用户将得到404响应。<br>
<br>
<b>匹配过程:</b>
Rose以请求的地址作为处理输入(不包含Query串，即问号后的字符串)。如果匹配树中存在对应的地址，且含有对应请求方法(GET、POST、PUT、DELETE)的，则表示匹配成功；如果含有其他方法的，但没有当前方法的（比如只支持GET，但当前是POST的），则也表示匹配成功，但最后会以405响应出去；如果所给的地址没有任何支持的方法或者没有找到匹配地址的，则表示匹配失败。1.0.1不支持回朔算法，1.0.2将支持部分回朔算法(待发布时再做详细介绍)。<br>
<br>
<b>参数解析:</b>
在调用验证器、拦截器 控制器之前，Rose完成2个解析：解析匹配树上动态的参数出实际值，解析控制器方法中参数实际的值。参数可能会解析失败(例如转化异常等等 )，此时该参数以默认值进行代替，同时Rose解析失败和异常记录起来放到专门的类中，继续下一个过程而不打断执行。<br>
<br>
<b>拦截器:</b>
Rose使用自定义的拦截器接口而非一般的拦截器接口这是有理由的。使用Rose自定义的拦截器接口可以更容易地操作、控制Rose拦截。 所谓拦截即是对已经匹配的控制器调用进行拦截，在其调用之前、之后以及页面渲染之后执行某些逻辑。设计良好的拦截器可以被多个控制器使用。<br>
<br>
<b>控制器:</b>

CHANGELOG<br>
<br>
<br>
0.9.6<br>
<br>
1、如果@Param没有设置默认值时，@Param会有一个内置字符串来表示开发者没有定义，现在更改这个字符串的值，并使用JAVA_DEFAULT来表示这个值<br>
<blockquote>【对应用程序没有影响】</blockquote>

2、去除ParameterBindingResult对rejectValue方法的覆盖，使其回归原始意思；<br>
<blockquote>如果出现绑定类型错误(TypeMismatch)，改addError(filedError)登记这个错误，而非使用rejectValue<br>
【对应用程序没有影响】</blockquote>

3、构造控制器的方法参数时，如因非type mismatch导致的异常(比如构造函数异常)，这个错误不再作为error登记到bindingResult，而是rethrow异常出来<br>
<blockquote>【可能影响到应用程序，使原先可以“因为构造函数错误但还能运行”的服务变得不可服务】</blockquote>

4、支持对Bean的Date、DateTime、TimeStamp属性进行绑定<br>
<blockquote>【可能影响应用程序，会对原先不能解析的date属性进行试图解析，把请求参数中的值解析到该参数】</blockquote>

5、执行异常时在异常信息中加注Rose和Spring的版本号<br>
<blockquote>【对应用程序没有影响】</blockquote>

6、如果有特定的请求方法rose本身不支持(比如PROPFIND)，原来是抛IllegalArgumentException，现改为405 Method Not Allowed<br>
<blockquote>【可能影响应用程序，原来抛出的请求方法不支持的异常，现在改为method allowed出去】</blockquote>

7、bugfix: 如果控制器目录下的rose.properties的module.path定义为一个未以'/'开始的串，且父package没有为空package或没有有效的rose类时，未能自动加上父亲路径的bug<br>
<blockquote>【可能影响程序：如果原先的程序定义了rose.properties，定义了module.path非以/开始的，但中间含有/字符的要特别认真校对】<br>
<blockquote>如果module.path以/开始，表示这是他的映射路径；如果不是以/开始的，表示需要加上它的父路径，举例如下:</blockquote></blockquote>

<blockquote><table><thead><th>package                       </th><th>rose.properties的module.path        </th><th>实际的module.path</th></thead><tbody>
<tr><td>controllers.application       </td><td>app                                 </td><td>/app          </td></tr>
<tr><td>controllers.application.abc   </td><td>abc                                 </td><td>/app/abc      </td></tr>
<tr><td>controllers.application.edf   </td><td>/edf                                </td><td>/edf          </td></tr>
<tr><td>controllers.application2      </td><td>/app2                               </td><td>/app2         </td></tr>
<tr><td>controllers.application2.xyz  </td><td>xyz                                 </td><td>/app2/xyz     </td></tr></blockquote></tbody></table>

8、bugfix: 解决参数解析互串的问，本质上PropertyEditor线程不安全，不能使用同一个propertyEditor对所有线程同时服务。<br>
<blockquote>【对应用程序没有影响；但因此造成的原来出现的一些“串页”问题将得到避免】<br>
9、支持ignoredPaths设置，使rose不对配置的地址进行映射和处理<br>
</blockquote><blockquote>配置方式：</blockquote>

<ol><li>在web.xml中，roseFilter的param name为ignoredPaths，value是可逗号分隔的多项地址<br>
</li><li>地址可有4种配置方式(均大小写敏感)：相等方式、前缀方式、后缀方式、正则表达式方式</li></ol>

10、支持:continue指示，在控制器、拦截器、验证器等返回此字符串指示rose把这个请求送给下一个过滤器以及Servlet<br>
<br>
11、增加inv.getModel(String name)方法，等价于inv.getModel().get(String name)<br>
<br>
12、增加ParamValidator接口，和拦截器、控制器有类同语义的风格的处理方式<br>
<br>
13、修改model.getAttributes语义，不再返回rose框架自身设置的model数据(即keyt以$$paoding-rose开始的属性)