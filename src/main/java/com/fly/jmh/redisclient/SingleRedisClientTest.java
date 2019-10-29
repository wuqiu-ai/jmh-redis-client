package com.fly.jmh.redisclient;

import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import java.util.Random;
import java.util.concurrent.TimeUnit;
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
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

/**
 * @author: peijiepang
 * @date 2019-10-25
 * @Description:
 */
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 1)//预热1次
@State(Scope.Benchmark)
@Measurement(iterations = 10)//正式做10次
@Fork(value = 1)//做1轮
@OutputTimeUnit(TimeUnit.SECONDS)
public class SingleRedisClientTest {

  private final static int BATCH_SIZE = 100;

  private RedisClient redisClient;

  private StatefulRedisConnection statefulRedisConnection;

  private  RedisCommands<String, String> syncCommands;

  @Setup
  public void init(){
    redisClient = RedisClient.create(
        RedisURI.Builder.redis("192.168.202.196", 6379).withPassword("zhaojun@2019").build());
    statefulRedisConnection = redisClient.connect();
    syncCommands = statefulRedisConnection.sync();
  }

  @Benchmark()
  @Threads(100)
  @OperationsPerInvocation(value = BATCH_SIZE)//单次循环
  @BenchmarkMode({Mode.Throughput})
  public void get(){
    for(int i=0;i<BATCH_SIZE;i++){
      String key = "benchmark";
      syncCommands.get(key);
    }
  }

  @Benchmark()
  @Threads(100)
  @OperationsPerInvocation(value = BATCH_SIZE)//单次循环
  @BenchmarkMode({Mode.Throughput})
  public void set(){
      for(int i=0;i<BATCH_SIZE;i++){
        String key = "jedis:test:"+new Random().nextInt();
        syncCommands.set(key,"", SetArgs.Builder.ex(60));
      }
  }

  @TearDown
  public void close(){
    statefulRedisConnection.close();
    redisClient.shutdown();
  }

  public static void main(String[] args) throws RunnerException {
    Options options = new OptionsBuilder().include(SingleRedisClientTest.class.getSimpleName()).build();
    new Runner(options).run();
  }

}
