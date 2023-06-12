# akka-rest-with-hbase
### restful with akka and hbase on kerberos with scala 

###License
* Apache License 2.0

###Features
* 基于 akka-http 和 akka-actor 的 restful API，整体使用scala语言实现
* 使用 hbase 作为数据存储，支持 kerberos的hbase认证
* 可配置 actor的数量,通过验证，actor数量在5-10个时，性能最佳
* 为比较性能，增加了 netty http的实现
* 从netty http 与akka http的测试结果看，netty htt的性能比akka http高出约1.5倍左右，与具体环境有关系

###启动
* 要求 jdk1.8
* 命令 java -cp hadoopConfDir:hbaseConfDir:akkahbase-1.0-SNAPSHOT-jar-with-dependencies.jar com.example.akka.Boot
* 启用kerberos的hbase，命令：java -cp hadoopConfDir:hbaseConfDir:akkahbase-1.0-SNAPSHOT-jar-with-dependencies.jar com.example.akka.Boot jaas.conf krb5.conf KrbLogin


