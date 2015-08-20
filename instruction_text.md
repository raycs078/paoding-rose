# @的使用 #

### 基本使用 ###

  * "@123456"表示输出123456到页面
  * "@中文english"表示输出"中文english"到页面

### 规范化contentType ###
  * "@json:xxxx"、"@applicaiton/x-json:xxxx"表示输出content-type为application/x-json的xxx字符串到客户端
  * "@xml:xxxx"、"@application/xml:xxxx" 表示输出content-type为application/xml的xxxx字符串到客户端
  * "@plain:xxxx"、"@text:xxxxx"、"@text/plain:xxxxx" 表示输出content-type为text/plain的字符串到客户端
  * "@xxxxx"、"@html:xxxx"、"@text/html:xxxxx" 表示输出content-type为text/html的字符串到客户端

我们支持以下Content-Type：json、xml、html、plain、text、所有以text/开始的，比如text/html、text/plain、text/javascript等；所有以application/开头的，比如applicaton/x-json等，其他的概不支持。

### 可以加;charset=UTF-8等修饰 ###
  * "@json:xxxx;charset=UTF-8"、"@applicaiton/x-json:xxxx;charset=UTF-8"表示输出content-type为application/x-json;charset=UTF-8的xxx字符串到客户端

## 技巧 ##
  * "@text/plain:json:xxxxx"、"@plain:json:xxxxx"、"@text:json:xxxxx"表示输出content-type为text/plain的"json:xxxxx"到客户端