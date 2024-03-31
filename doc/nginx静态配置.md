Linux中Nginx两个路径：

1. `/etc/nginx/conf.d/` 文件夹，是我们进行子配置的配置项存放处，`/etc/nginx/nginx.conf` 主配置文件会默认把这个文件夹中所有子配置项都引入；

2. `/usr/share/nginx/html/` 文件夹，通常静态文件都放在这个文件夹，也可以根据你自己的习惯放其他地方



如果没有配置https，要用http去请求



启动nginx

`sudo systemctl start nginx`

检查状态

`sudo systemctl status nginx`
