## window

/bin/

+ 先运行nameServer，配置地址

``` mqnamesrv.cmd -n localhost:9876```

![image-20240328111810531](./img/image-20240328111810531.png)

+ 再运行broker，注册给nameserver

``` mqbroker.cmd -n localhost:9876 -c ./conf/broker.conf```

![image-20240328112027678](./img/image-20240328112027678.png)

日志位置：在配置文件的路径

