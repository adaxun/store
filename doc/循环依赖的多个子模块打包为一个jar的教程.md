在Spring Boot项目中，如果你有多个子模块，其中一个子模块依赖于其他的子模块，并且启动类也在这个依赖其他子模块的子模块中，你可以通过以下步骤将它们打包成一个可执行的jar包（通常称为“fat jar”或“über jar”）：

1. **确保子模块间的依赖关系**：
   在依赖子模块的pom.xml文件中，通过 `<dependencies>` 标签添加对其他子模块的依赖，例如：

   ```xml
   <!-- 在依赖子模块的pom.xml中 -->
   <dependencies>
     <dependency>
       <groupId>com.example.group</groupId>
       <artifactId>dependent-module</artifactId>
       <version>1.0.0-SNAPSHOT</version> <!-- 请替换为你子模块的实际版本 -->
     </dependency>
     <!-- 其他依赖的子模块 -->
   </dependencies>
   ```

2. **主模块设置**：
   创建一个聚合模块（Aggregator Module），也被称为父模块或聚合项目，它的作用是管理和构建所有的子模块。在父模块的pom.xml文件中，加入 `<modules>` 标签列出所有的子模块，并且确保这个父模块包含了`spring-boot-maven-plugin`以构建可执行的jar。

   ```xml
   <!-- 在父模块的pom.xml中 -->
   <groupId>com.example.group</groupId>
   <artifactId>parent-module</artifactId>
   <packaging>pom</packaging>
   
   <modules>
     <module>dependency-module</module>
     <module>dependent-module-with-start-class</module>
     <!-- 其他子模块 -->
   </modules>
   
   <!-- spring-boot-maven-plugin需要在含有启动类的子模块的pom.xml中配置 -->
   ```

3. **含有启动类的子模块配置**：
   在含有启动类的那个子模块的pom.xml中，配置`spring-boot-maven-plugin`来创建可执行的jar，同时确保`mainClass`指向正确的启动类。

   ```xml
   <!-- 在含有启动类的子模块的pom.xml中 -->
   <build>
     <plugins>
       <plugin>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-maven-plugin</artifactId>
         <configuration>
           <mainClass>com.example.startup.YourApplication</mainClass> <!-- 替换为你的启动类全路径 -->
         </configuration>
         <executions>
           <execution>
             <goals>
               <goal>repackage</goal>
             </goals>
           </execution>
         </executions>
       </plugin>
     </plugins>
   </build>
   ```

4. **构建可执行jar**：
   在命令行中，定位到父模块的目录，执行以下命令打包所有子模块：

   ```sh
   mvn clean install
   ```

   然后在含有启动类的子模块目录下，执行：

   ```sh
   mvn package spring-boot:repackage
   ```

   生成的jar文件将在`target`目录下，这个jar包将包含所有子模块的代码和依赖库，可以直接通过`java -jar`命令运行。

注意，这种方式构建的jar包体积较大，因为它包含了所有依赖的子模块和第三方依赖。在大型项目中，可能需要考虑微服务架构或模块间通过网络通信的方式，而不是打包成一个大的单体应用。但在小型项目或某些特定场景下，打包成一个单一jar包是一种可行的部署方案。



PS：如果出现失败情况，尝试修改本地Java_Home 环境配置，和在本地cmd中运行mvn