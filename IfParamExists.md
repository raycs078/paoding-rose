# @IfParamExists的使用 #


  * @IfParamExists标注在控制器的方法上，表示只有满足@IfParamExists定义的内容才有可能有该方法处理请求。
  * 不同的控制器方法，可以有相同的映射地址和HTTP方法，每个方法上配置不同的@IfParamExists
  * @IfParamExists判断只在地址和HTTP方法已经满足的条件上才进行
  * @IfParamExists("c") 表示只有存在c非空串参数才由该方法处理
  * @IfParamExists("c=:[0-9]+") 表示只有存在c非空串参数，且参数值符合冒号后的正则表达式才由该方法处理
  * @IfParamExists("c=3") 表示只有存在c参数且值为3时才由该方法处理
  * 如果有多个方法符合该规则，则c=3优先级大于c=:[0-9]+，c=:[0-9]+又大于c
