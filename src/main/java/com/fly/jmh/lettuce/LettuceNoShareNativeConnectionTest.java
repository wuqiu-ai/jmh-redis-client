package com.fly.jmh.lettuce;

/**
 * @author: peijiepang
 * @date 2019-10-25
 * @Description:
 */

import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OperationsPerInvocation;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration;
import org.springframework.data.redis.core.types.Expiration;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3)//预热3次
@State(Scope.Benchmark)
@Measurement(iterations = 10)//正式做10次
@Fork(value = 1)//做1轮
@OutputTimeUnit(TimeUnit.SECONDS)
public class LettuceNoShareNativeConnectionTest {
  private final static int BATCH_SIZE = 100;

  private LettuceConnectionFactory factory;

  @Setup
  public void init() {
    GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
    poolConfig.setMaxTotal(100);//最大连接数
    poolConfig.setMaxIdle(10);//最大空闲连接数
    poolConfig.setMinIdle(5);//初始化连接数
    poolConfig.setMaxWaitMillis(10000);//最大等待时间
    poolConfig.setTestOnBorrow(false);//对拿到的connection进行validateObject校验
    poolConfig.setTestOnReturn(false);//在进行returnObject对返回的connection进行validateObject校验
    poolConfig.setTestWhileIdle(false);//定时对线程池中空闲的链接进行validateObject校验
    LettucePoolingClientConfiguration.LettucePoolingClientConfigurationBuilder clientConfigurationBuilder = LettucePoolingClientConfiguration.builder().poolConfig(poolConfig);
    RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
    redisStandaloneConfiguration.setHostName("192.168.202.196");
    redisStandaloneConfiguration.setPort(6379);
    redisStandaloneConfiguration.setPassword(RedisPassword.of("zhaojun@2019"));
    factory = new LettuceConnectionFactory(redisStandaloneConfiguration,clientConfigurationBuilder.build());
    factory.setShareNativeConnection(false);//连接池模式
    factory.afterPropertiesSet();
  }

  @Benchmark()
  @Threads(100)
  @OperationsPerInvocation(value = BATCH_SIZE)//单次循环
  @BenchmarkMode({Mode.Throughput})
  public void get(){
    RedisConnection redisConnection = factory.getConnection();
    try{
      for(int i=0;i<BATCH_SIZE;i++){
        String key = "benchmark";
        redisConnection.get(key.getBytes());
      }
    }finally {
      redisConnection.close();
    }
  }

  @Benchmark()
  @Threads(100)
  @OperationsPerInvocation(value = BATCH_SIZE)//单次循环
  @BenchmarkMode({Mode.Throughput})
  public void set(){
    RedisConnection redisConnection = factory.getConnection();
    try{
      for(int i=0;i<BATCH_SIZE;i++){
        String key = "jedis:test:"+new Random().nextInt();
        redisConnection.set(key.getBytes(),"".getBytes(), Expiration.seconds(60), SetOption.SET_IF_ABSENT);
      }
    }finally {
      redisConnection.close();
    }
  }

  public static void main(String[] args) throws RunnerException {
    Options options = new OptionsBuilder().include(LettuceNoShareNativeConnectionTest.class.getSimpleName()).build();
    new Runner(options).run();
  }
}
