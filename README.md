

# SpringBoot+mybatis+springsecurity实现用户角色数据库管理



## 一.正式开始前

开发工具：IDEA 2018.2

开发环境：

+ jdk 1.8  
+ Spring Boot 1.5.6.RELEASE
+  Spring Security4

## 二.具体步骤

1. 设计数据库并添加数据

   ![](http://pkh8npf4z.bkt.clouddn.com/qiniu_picGo/4630295-0c5f35e25dd6d3bd.png)

>  具体根据sql脚本文件执行,教程的作者建立了三个表，其实两个表（用户表和角色表）就够了，但是作者不喜欢是用外键，而喜欢借助sql语句的多表关联查询来完善没有外键的情况（就多一次关联查询咯）

2. 新建Spring Boot项目，注意版本号

3. 引入pom依赖

   ```xml
   <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-starter-web</artifactId>
           </dependency>
   
           <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-starter-security</artifactId>
           </dependency>
   
           <dependency>
               <groupId>org.springframework.boot</groupId>
               <artifactId>spring-boot-starter-thymeleaf</artifactId>
           </dependency>
   
           <dependency>
               <groupId>org.mybatis.spring.boot</groupId>
               <artifactId>mybatis-spring-boot-starter</artifactId>
               <version>1.3.2</version>
           </dependency>
           <!--集成过程中thymeleaf页面展示控制中需要用到-->
           <dependency>
               <groupId>org.thymeleaf.extras</groupId>
               <artifactId>thymeleaf-extras-springsecurity4</artifactId>
           </dependency>
   
           <dependency>
               <groupId>mysql</groupId>
               <artifactId>mysql-connector-java</artifactId>
               <version>5.1.41</version>
           </dependency>
   
           <dependency>
               <groupId>org.projectlombok</groupId>
               <artifactId>lombok</artifactId>
           </dependency>
   
   ```

   > 其中最后一个lombok依赖是为了方便写实体类的时候，不用再写get，set方法以及构造方法，这个具体使用可以在实体类中看到(注解)

4. **application.properties**

   ```properties
   # 数据源配置
   spring.datasource.driver-class-name=com.mysql.jdbc.Driver
   spring.datasource.url=jdbc:mysql://localhost:3306/spring_security
   spring.datasource.username=root
   spring.datasource.password=1027
   
   # thymeleaf的配置(其实前缀和后缀，不配置的话，
   # 也是如此，这就体现了Spring Boot的“约定优于配置”)
   spring.thymeleaf.cache=false
   spring.thymeleaf.prefix=classpath:/templates/
   spring.thymeleaf.suffix=.html
   
   # mybatis的配置
   # 1.mapper（xml）的路径
   mybatis.mapper-locations=classpath*:mapper/*.xml 
   # 2.实体类的路径
   mybatis.type-aliases-package=com.ajin.domain
   
   ```

   5. 创建用户以及角色的实体类

      + 用户

      ```java
      @Data//不用写get set
      public class SysUser implements UserDetails {
          //UserDetails是Spring Security验证框架内部提供的用户验证接口，
          // 主要是来完成自定义用户认证功能，
          // 需要实现getAuthorities方法内容，将定义的角色列表添加到授权的列表内。
          private Integer id;
          private String username;
          private String password;
          private List<SysRole> roles;
      
          // 这是最重要的方法,必须重写
          //将定义的角色列表添加到授权的列表内。
          @Override
          public Collection<? extends GrantedAuthority> getAuthorities() {
              List<GrantedAuthority> auths = new ArrayList<>();
              List<SysRole> roles = getRoles();
              for(SysRole role:roles) {
                  auths.add(new SimpleGrantedAuthority(role.getName()));
              }
              return auths;
          }
      
          @Override
          public boolean isAccountNonExpired() {
              return true;
          }
      
          @Override
          public boolean isAccountNonLocked() {
              return true;
          }
      
          @Override
          public boolean isEnabled() {
              return true;
          }
      
          @Override
          public boolean isCredentialsNonExpired() {
              return true;
          }
      }
      ```

      * 角色

        ```java
        @Data
        public class SysRole {
            private Integer id;
            private String name;
        }
        ```

   6. 编写mapper类和mapper.xml文件

      ```java
      @Repository //注解可以不加
      public interface UserMapper {
          SysUser findByUserName(@Param("username") String username);
      }
      ```

      ```xml
      <?xml version="1.0" encoding="utf-8" ?>
      <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
      <mapper namespace="com.ajin.dao.UserMapper">
          <resultMap id="UserMap" type="com.ajin.domain.SysUser">
              <id property="id" column="id"></id>
              <result property="username" column="username"/>
              <result property="password" column="password"/>
              <collection property="roles" ofType="com.ajin.domain.SysRole">
                  <result column="name" property="name"/>
              </collection>
          </resultMap>
          <!--教程作者不建议在数据库中使用外键，它认为涉及到外键的逻辑全部在应用代码里实现。-->
          <!--但是这样做的后果，就是会有两个关联查询，会不会影响sql的性能-->
          <select id="findByUserName" resultMap="UserMap" parameterType="java.lang.String">
              select u.*
              ,r.name
              from Sys_User u
              LEFT JOIN sys_role_user sru on u.id= sru.Sys_User_id
              LEFT JOIN Sys_Role r on sru.Sys_Role_id=r.id
              where username= #{username}
      
      
          </select>
      </mapper>
      ```

   7. 新建CustomUserService类

      ```java
      @Service
      public class CustomUserService implements UserDetailsService {
          @Autowired
          UserMapper userMapper;
          //重写loadUserByUsername方法，获得UserDetails类型用户
          @Override
          public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
              SysUser user =userMapper.findByUserName(username);
              if(user!=null) {
                  return  user;
              }else {
                  throw new UsernameNotFoundException("user"+username+"doesn't exits!");
              }
          }
      }
      
      ```

   8. 写Spring Security的核心配置类WebSecurityConfig

      ```java
      @Configuration
      @EnableWebSecurity
      public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
          @Bean
          UserDetailsService customUserService() {
              return new CustomUserService();
          }
      
          /**
           * 配置忽略的静态文件，不加的话，登录之前页面的css,js不能正常使用，得登录之后才能正常.
           */
          @Override
          public void configure(WebSecurity web) throws Exception {
              //忽略url
              web.ignoring().antMatchers("/**/*.js", "/lang/*.json", "/**/*.css", "/**/*.js", "/**/*.map", "/**/*.html",
                      "/**/*.png");
          }
      
          @Override
          protected void configure(HttpSecurity http) throws Exception {
              http.authorizeRequests()
                      .anyRequest().authenticated()//任何请求，登录后才能访问
                      .and()
                      .formLogin()
                      .loginPage("/login")
                      .failureUrl("/login?error") //登录失败，返回error
                      .permitAll()//登录页面的请求，任何用户都可以访问
                      .and()
                      .logout().permitAll();//注销，任意用户访问（包括匿名用户，也就是没有登录进来的用户）
          }
      }
      
      ```

   9. controller层

      > 在此之前，要添加一个Msg消息类，返回消息给前台页面

      ```java
      @Data
      @AllArgsConstructor//全参数构造方法
      @NoArgsConstructor//默认的无参构造方法
      public class Msg {
          private String title;
          private String content;
          private String etraInfo;
      }
      
      ```

      

      ```java
      @Controller
      public class HomeController {
          @RequestMapping("/")
          public String index(Model model){
              Msg msg =  new Msg("测试标题","测试内容","额外信息，只对管理员显示");
              model.addAttribute("msg", msg);
              return "home";
          }
          @RequestMapping("/admin")
          @ResponseBody
          public String hello(){
              return "hello admin";
          }
          @RequestMapping("/login")
          public String login(){
              return "login";
          }
      
      }
      
      ```

   10. 前台页面编写

       + 在static文件夹下新建css文件夹，放入bootstrap.min.css

       + 在templates文件夹下新建login.html，home.html（主要是thymeleaf模板加上bootstrap）

         >  login.html

         ```html
         <!DOCTYPE html>
         <html xmlns:th="http://www.thymeleaf.org">
         <head>
             <meta content="text/html;charset=UTF-8"/>
             <title>登录页面</title>
             <link rel="stylesheet" th:href="@{css/bootstrap.min.css}"/>
             <style type="text/css">
                 body {
                     padding-top: 50px;
                 }
                 .starter-template {
                     padding: 40px 15px;
                     text-align: center;
                 }
             </style>
         </head>
         <body>
         
         <nav class="navbar navbar-inverse navbar-fixed-top">
             <div class="container">
                 <div class="navbar-header">
                     <a class="navbar-brand" href="#">Spring Security演示</a>
                 </div>
                 <div id="navbar" class="collapse navbar-collapse">
                     <ul class="nav navbar-nav">
                         <li><a th:href="@{/}"> 首页 </a></li>
         
                     </ul>
                 </div><!--/.nav-collapse -->
             </div>
         </nav>
         <div class="container">
         
             <div class="starter-template">
                 <p th:if="${param.logout}" class="bg-warning">已成功注销</p><!-- 1 -->
                 <p th:if="${param.error}" class="bg-danger">有错误，请重试</p> <!-- 2 -->
                 <h2>使用账号密码登录</h2>
                 <form name="form" th:action="@{/login}" action="/login" method="POST"> <!-- 3 必须是Post形式才可以-->
                     <div class="form-group">
                         <label for="username">账号</label>
                         <input type="text" class="form-control" name="username" value="" placeholder="账号" />
                     </div>
                     <div class="form-group">
                         <label for="password">密码</label>
                         <input type="password" class="form-control" name="password" placeholder="密码" />
                     </div>
                     <input type="submit" id="login" value="Login" class="btn btn-primary" />
                 </form>
             </div>
         
         </div>
         
         </body>
         </html>
         
         ```

         > home.html

         ```java
         <!DOCTYPE html>
         <html xmlns:th="http://www.thymeleaf.org"
               xmlns:sec="http://www.thymeleaf.org/thymeleaf-extras-springsecurity4">
         <head>
             <meta content="text/html;charset=UTF-8"/>
             <title sec:authentication="name"></title>
             <link rel="stylesheet" th:href="@{css/bootstrap.min.css}" />
             <style type="text/css">
                 body {
                     padding-top: 50px;
                 }
                 .starter-template {
                     padding: 40px 15px;
                     text-align: center;
                 }
             </style>
         </head>
         <body>
         <nav class="navbar navbar-inverse navbar-fixed-top">
             <div class="container">
                 <div class="navbar-header">
                     <a class="navbar-brand" href="#">Spring Security演示</a>
                 </div>
                 <div id="navbar" class="collapse navbar-collapse">
                     <ul class="nav navbar-nav">
                         <li><a th:href="@{/}"> 首页 </a></li>
                         <li><a th:href="@{/admin}"> admin </a></li>
                     </ul>
                 </div><!--/.nav-collapse -->
             </div>
         </nav>
         
         
         <div class="container">
         
             <div class="starter-template">
                 <h1 th:text="${msg.title}"></h1>
         
                 <p class="bg-primary" th:text="${msg.content}"></p>
         
                 <div sec:authorize="hasRole('ROLE_ADMIN')"> <!-- 用户类型为ROLE_ADMIN 显示 -->
                     <p class="bg-info" th:text="${msg.etraInfo}"></p>
                 </div>
         
                 <div sec:authorize="hasRole('ROLE_USER')"> <!-- 用户类型为 ROLE_USER 显示 -->
                     <p class="bg-info">无更多信息显示</p>
                 </div>
         
                 <form th:action="@{/logout}" method="post">
                     <input type="submit" class="btn btn-primary" value="注销"/>
                 </form>
             </div>
         </div>
         </body>
         </html>
         
         
         ```

         > 具体thymeleaf配合Spring Security给登录用户鉴权的逻辑，不是太清楚

       打开浏览器输入http://localhost:8080 去看看效果

       

       
