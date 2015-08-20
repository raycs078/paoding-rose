# 散表 #
根据一些参数把程序中的SQL中的表名映射为数据库中实际的表名，比如程序中有一条SQL “select id, name from user”, 但实际的数据库用来存储该user数据的表不止一个，而是十个，表名分别是user\_0、user\_1、...、user\_9

因此，在执行该select的时侯，jade就必须根据某些条件，将送到MySQL服务器的语句解析为实际的SQL，例如select id, name from user\_1 。

支持这种特性的数据库设计，我们称之为具有散表功能。