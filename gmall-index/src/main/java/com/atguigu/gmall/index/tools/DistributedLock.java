package com.atguigu.gmall.index.tools;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;


@Component
public class DistributedLock {
    @Autowired
    private StringRedisTemplate redisTemplate;

    private Timer timer;
/*可重入锁:
			加锁时：
				1.判断lock是否存在: exists lock，不存在则直接获取锁：hset lock 3323-232-34sjwr-23432 1
				2.如果存在，则判断是否自己的锁：hexists lock 3323-232-34sjwr-23432
					如果是自己的锁：HINCRBY lock 3323-232-34sjwr-23432 1
				3.如果不是自己的锁：return 0*/
    public boolean tryLock(String lockName, String uuid, Integer expire) {
        String script = "if(redis.call('exists', KEYS[1]) == 0 or redis.call('hexists', KEYS[1], ARGV[1]) == 1)" +
                " then " +
                "redis.call('hincrby', KEYS[1], ARGV[1], 1) " +
                "redis.call('expire', KEYS[1], ARGV[2]) " +
                "return 1 " +
                "else return 0 " +
                "end";
        Boolean flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList(lockName), uuid, expire.toString());
        if (!flag) {
            try {
                Thread.sleep(100);
                tryLock(lockName, uuid, expire);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }else {
            renewExpire(lockName, uuid, expire);
        }
        return true;
    }

    public void unLock(String lockName, String uuid) {
        String script = "if(redis.call('hexists', KEYS[1], ARGV[1]) == 0) " +
                "then " +
                "return nil" +
                " elseif(redis.call('HINCRBY', KEYS[1], ARGV[1], -1) == 0)" +
                " then " +
                "return redis.call('del', KEYS[1]) " +
                "else " +
                "return 0 " +
                "end";
        Long flag = this.redisTemplate.execute(new DefaultRedisScript<>(script, Long.class), Arrays.asList(lockName), uuid);
        if (flag == null) {
            throw new RuntimeException("在尝试解别的锁,或者锁不存在");
        }else if (flag==1){
            timer.cancel();
        }
    }
        //自动续期
    private void renewExpire(String lockName, String uuid, Integer expire) {
        String script = "if(redis.call('hexists', KEYS[1], ARGV[1]) == 1) then return redis.call('expire', KEYS[1], ARGV[2]) else return 0 end";
        this .timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                redisTemplate.execute(new DefaultRedisScript<>(script,Boolean.class),Arrays.asList(lockName),uuid,expire.toString());
            }
        }, expire * 1000 / 3, expire * 1000 / 3);
    }

    //定时任务
//    public static void main(String[] args) {
//
//        new Timer().schedule(new TimerTask() {
//            @Override
//            public void run() {
//                System.out.println(System.currentTimeMillis());
//            }
//        }, 10000,10000);
////        while (true){
////
////            new Thread(()->{
////                try {
////                    TimeUnit.SECONDS.sleep(2);
////                    System.out.println(System.currentTimeMillis());
////                } catch (InterruptedException e) {
////                    e.printStackTrace();
////                }
////
////            },"wuwu").start();
////        }
//    }
}
