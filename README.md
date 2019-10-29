## 1.说明
平台redis框架包以前版本都是使用jedis来连接Redis服务的。Jedis 是一个很老牌的 Redis 的 Java 开发包，使用很稳定，使用范围最广的 Redis 开发包。但是 Jedis 比较推出时间比较早，整个设计思路比较传统，例如不支持异步操作，接口设计比较繁琐老套（相比其他开发包而已），使用连接池占用很多的物理连接资源。当然，这个是可以理解的，比较一个比较早期的开发包，相对其做大的结构调整是很难的，而且用户也不一定会接受。

相比较 Jedis ，我觉得 Lettuce 的优点有如下几个方面：
- 更加直观、结构更加良好的接口设计
- 基于 Netty NIO 可以高效管理 Redis 连接，不用连接池方式
- 支持异步操作（J2Cache 暂时没用到这个特性）
- 文档非常详尽

LettuceConnectionFactory 类里面有个参数 shareNativeConnection，默认为 true，意思是共用这一个连接，所以默认情况下 lettuce 的连接池是没有用的；如果需要使用连接池，shareNativeConnection 设置为 false 就可以了。

spring-data-redis中的luttucefactory 默认情况是复用一个redis连接的，如果以下情况，则是会新生成一个connection
- 1.请求批量下发，即禁止调用命令后立即flush
- 2.使用`BLPOP`这种阻塞命令
- 3.事务操作
- 4.有多个数据库的情况


但在升级之前，我们还需确认lettuce的性能如何，下面就开始lettuce和jedis的性能测试对比。

## 2. Redis-server环境说明
- 版本：3.2.20版本
- cpu : 8核
- 内存：16G

## 3. 测试结果
序号|client | 线程数 | redis连接数 | 每个方法循环次数 | get方法 Throughput（ops/s）| set方法 Throughput（ops/s）
---|---|---|---|---|---|---
1 | jedis连接池 | 100 | 100 | 100 | 90645.493 ± 12276.622 | 72707.919 ± 17990.890
2 | spring-data-redis使用lettuce单连接(shareNativeConnection=true) | 100 | 1 | 100 |93949.487 ± 6821.183 | 83292.187 ± 8109.392
3 | spring-data-redis使用lettuce连接池(shareNativeConnection=false)  | 100  | 100 | 100 | 120551.719 ± 10950.769 | 89572.367 ± 19262.930
4 | lettuce原生单连接 | 100 | 1 | 100 | 107118.958 ± 11730.931 | 85994.687 ± 18903.881
5 | lettuce原生多连接 | 100 | 100 | 100 | 111293.148 ± 11762.763  | 104956.130 ± 12792.028

### 3.1 推论
1. 通过对比jedis与lettuce原生版本比较，lettuce总体的性能比jedis高；
2. 通过对比spring-data-redis版本lettuce单连接与连接池比较，连接池版本总体性能较好，但差距不大；
3. 通过对比原生lettuce版本单连接和连接池比较，连接池版本比单连接版本性能好，但差距不大；
4. 通过对比spring-data-redis版本与lettuce版本比较，spring-data-redis封装的lettuce总体的性能比原生lettuce低；
5. 通过对比jedis与spring-data-redis单连接版本，单连接的版本性能也比jedis连接池的版本性能高出许多；

## 4. 结论
1. 平台redis框架包最终默认采用spring-data-lettuce的单连接版本，总体性能比jedis高，占用连接数比较少（平时只占用1个连接，只要在阻塞或者事物的情况下，才会新建连接），符合大部分高并发业务性能需求；
2. 平台redis框架包shareNativeConnection参数配置，如果遇到高并发大流量场景，采用连接池版本可以提高性能；