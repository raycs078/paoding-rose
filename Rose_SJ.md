# 时文：一场技术的圣战：rose开源框架之portal #

（54chen按：此文为客座博文，王志亮大侠，人人网架构师，庖丁分词创始人，rose是他的另一开源大作。关于69圣战，请看 http://www.baidu.com/s?wd=69%CA%A5%D5%BD&n=2  ）

2010年的6月9日是一个圣战的日子，零点一到就有人开始，好戏也如约在晚上7点发生。人人网战场是SJ的公共主页：http://page.renren.com/sj

对不同人，这个日子意味着不同，滋味也不同。作为人人网技术团队，我们要保证服务能力、用户体验能够应付得了这个挑战。

某一个服务器的能力总有限，为了应付突然增长的读写量，web服务架构、内部服务架构、数据库架构等要能够轻松通过服务器调配来满足。就web服务器而言，我们增加了1倍的机器。现在再回头来看监控的数据，一切显得美好。这个期间整个服务做到了服务能力没有中断。除此之外，在这次圣战中，其中还有一项我们独有的技术起到了重要的作用：rose portal ，下面作一个介绍：


这是sj的主页：

> ![http://paoding-rose.googlecode.com/svn/other/imgs/sj/1.jpg](http://paoding-rose.googlecode.com/svn/other/imgs/sj/1.jpg)
> > 图1 sj在人人网的公共主页

这个页面分为三列：

左边有 “推荐给好友”、“基本信息”、“相册”;
中间有“给SJ留言”、“好友留言”;
右边有“好友”，“人人的用户还关注”等。


在后台，这些被分解为不同的模块，我们称之为”window”。这每一个window都意味着可能连接一个独立的服务集群，比如基本信息服务、留言服务、好友服务、相册服务等等。这样，一个公共主页就等于多个独立的、可配置的window模块组成，如下图：


> ![http://paoding-rose.googlecode.com/svn/other/imgs/sj/2.jpg](http://paoding-rose.googlecode.com/svn/other/imgs/sj/2.jpg)
> > 图2 公共主页的window模块组成

随着伟大圣战的深入，这个页面就变成这样(右边的栏目不见了)：


> ![http://paoding-rose.googlecode.com/svn/other/imgs/sj/3.jpg](http://paoding-rose.googlecode.com/svn/other/imgs/sj/3.jpg)
> > 图3 圣战进行中时模块的自动保护

产品同学看到此情此景，仍然很开心：“只要留言的window能在，其它的没在不要紧”

但是不一会，继续恶化：


> ![http://paoding-rose.googlecode.com/svn/other/imgs/sj/4.jpg](http://paoding-rose.googlecode.com/svn/other/imgs/sj/4.jpg)
> > 图4 圣战进行中压力进一步增加

甚至：


> ![http://paoding-rose.googlecode.com/svn/other/imgs/sj/5.jpg](http://paoding-rose.googlecode.com/svn/other/imgs/sj/5.jpg)
> > 图5 圣战进行中压力进一步增加

黄晶看着公共主页呈现出这种状况时，笑着形容这样的图“缺胳膊少腿”：“怎么还没加机器”。当公共主页技术团队把机器逐步增加一倍的时候，这种情况变少了，甚至就没有了。

虽然这些页面看起来“缺胳膊少腿”，但要知道在以前，这种情况，我们整个页面的某个模块堵了可会导致用户浏览器长期空白，直至最后提示网页不可显示。这给用户带来很不好的体验，同时因为网页一直不释放连接，恶性循环导致web服务器最后全哑了。

好在，早在半年前我们开发了rose portal框架，解决了此问题，rose portal是一个服务端portal技术，基于rose框架 (也就是servlet容器) 下的服务端portal技术，rose portal不是Java常说的portlet技术，也不是基于ajax的客户端portal技术。

rose poral提供这些特性；

能够将一个页面分为多个窗口；
开发者使用一个主控制器，在主控制器中不断通过portal.addWindow方法，将请求并发转发给多个窗口；
每个窗口有单独的控制器处理逻辑、可以返回独立视图就像一个web请求一样；
框架能够处理并发转发、并发逻辑处理、并发渲染，并最后统一返回把html输出给浏览器；
提供了“整体超时控制”手段，使得某个窗口因一时服务能力下降时不影响整个页面的输出；


这里有一个portal开发示例：

http://code.google.com/p/paoding-rose/wiki/Rose_Portal_Demo


献上现在最新的sj主页（2010/6/10 10:46）作为结束，目前粉丝 粉丝 21w+、留言146w+，谢谢（另感谢李伟博同学，以上图片均是他收集的）：


> ![http://paoding-rose.googlecode.com/svn/other/imgs/sj/6.jpg](http://paoding-rose.googlecode.com/svn/other/imgs/sj/6.jpg)
> > 图6 最新的sj主页


转自五四科学院：
http://www.54chen.com/architecture/rose-open-source-portal-framework.html