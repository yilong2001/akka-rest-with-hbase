# akka-rest-with-hbase
### restful with akka and hbase on kerberos 

###License
* Apache License 2.0

###Features
* 基于 akka-http 和 akka-actor 的 restful API
* 使用 hbase 作为数据存储，支持 kerberos的hbase认证
* 在启动时候，可配置 actor的数量

###启动
* 要求 jdk1.8
* 命令 java -cp hadoopConfDir:hbaseConfDir:akkahbase-1.0-SNAPSHOT-jar-with-dependencies.jar com.example.akka.Boot
* 启用kerberos的hbase，命令：java -cp hadoopConfDir:hbaseConfDir:akkahbase-1.0-SNAPSHOT-jar-with-dependencies.jar com.example.akka.Boot jaas.conf krb5.conf KrbLogin

###联系
* yilong2001@126.com
* wx:yilong2001

