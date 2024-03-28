
# 导出sql文件：

```shell
mysqldump -u your_username -p your_password your_database > your_database.sql
```

# 拉取镜像
docker pull mysql:8.0.30

# 创建容器
```
docker run -d --name mysql -p 3306:3306 -v /var/lib/mysql:/var/lib/mysql -v /etc/mysql:/etc/mysql --restart=always --privileged=true -e MYSQL_ROOT_PASSWORD=1234 mysql:8.0.30
```

* Linux下 mysql -uroot -p 登录你的 MySQL 数据库，然后 执行这条SQL：

```sql
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY '123456';
```

然后再重新配置SQLyog的连接，重新填写密码，则可连接成功了。 

# 执行.sql

```
create database database_name;
user database_name;
source xxx.sql
```



# 关闭 容器

docker stop my_container_name_or_id



# 继续运行

docker start name_or_id