package com.fly.jmh.jedis;

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
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import redis.clients.jedis.Jedis;

/**
 * jedis 单线程操作
 * 注意：jedis不能多线程操作命令,因为线程不安全问题；解决方案:通过连接池即可解决该问题；
 * @author: peijiepang
 * @date 2019-10-28
 * @Description:
 */
@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 3)//预热3次
@State(Scope.Benchmark)
@Measurement(iterations = 10)//正式做10次
@Fork(value = 1)//做1轮
@OutputTimeUnit(TimeUnit.SECONDS)
public class SingleJedisTest {
  private final static int BATCH_SIZE = 100;

  private Jedis jedis = null;

  @Setup
  public void init(){
    jedis = new Jedis("192.168.202.196",6379);
    jedis.auth("zhaojun@2019");
  }

  @Benchmark()
  @Threads(1)
  @OperationsPerInvocation(value = BATCH_SIZE)//单次循环
  @BenchmarkMode({Mode.Throughput})
  public void get(){
    for(int i=0;i<BATCH_SIZE;i++){
      String key = "benchmark";
      jedis.get(key);
    }
  }

  public static void main(String[] args) throws RunnerException {
    Options options = new OptionsBuilder().include(SingleJedisTest.class.getSimpleName()).build();
    new Runner(options).run();
  }
}
