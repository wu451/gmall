package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.aspect.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.index.tools.DistributedLock;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import jdk.nashorn.internal.ir.IfNode;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class IndexService {

    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private DistributedLock distributedLock;
    @Autowired
    private RedissonClient redissonClient;

    private static final String KEY_PREFIX = "index:cates";

    //查询一级分类
    public List<CategoryEntity> queryLvOneCategoriesById() {
        ResponseVo<List<CategoryEntity>> responseVo = this.pmsClient.queryCategory(0l);
        return responseVo.getData();
    }

    @GmallCache(prefix = KEY_PREFIX, timeOut = 43200, random = 4320, lock = "index:lock:")
    public List<CategoryEntity> queryLvTwoCategoryWithSubByPid(Long pid) {

        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategoriesWithSubsByPid(pid);
        List<CategoryEntity> data = listResponseVo.getData();

        return data;
    }


    public List<CategoryEntity> queryLvTwoCategoryWithSubByPid2(Long pid) {
        //查询缓存
        String json = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(json) && !StringUtils.equals("null", json)) {
            return JSON.parseArray(json, CategoryEntity.class);
        } else if (StringUtils.equals("null", json)) {
            return null;
        }
        RLock lock = this.redissonClient.getLock("index:lock" + pid);
        lock.lock();
        //获取锁过程中可能有其他请求,已经提前获取到锁,并把数据放入到缓存中,再查一遍
        String json2 = this.redisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if (StringUtils.isNotBlank(json2) && !StringUtils.equals("null", json)) {
            return JSON.parseArray(json2, CategoryEntity.class);
        } else if (StringUtils.equals("null", json2)) {
            return null;
        }

        ResponseVo<List<CategoryEntity>> listResponseVo = this.pmsClient.queryCategoriesWithSubsByPid(pid);
        List<CategoryEntity> data = listResponseVo.getData();
        //缓存里没有,则放入缓存(解决缓存穿透,)
        if (CollectionUtils.isEmpty(data)) {
            this.redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(data), 3, TimeUnit.MINUTES);
        } else {

            //为解决缓存雪崩,给时间加入随机值
            this.redisTemplate.opsForValue().set(KEY_PREFIX + pid, JSON.toJSONString(data), 30 + new Random().nextInt(10), TimeUnit.DAYS);
        }


        return data;
    }

    public void testLock1() {
        //获取锁(设置过期时间防止服务器宕机带来的死锁)
        String uuid = UUID.randomUUID().toString();
        Boolean flag = this.redisTemplate.opsForValue().setIfAbsent("lock", uuid, 3, TimeUnit.SECONDS);
        if (!flag) {
            //获取锁失败,重试
            try {
                Thread.sleep(100);
                testLock1();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {

            String number = this.redisTemplate.opsForValue().get("number");
            if (StringUtils.isBlank(number)) {
                return;
            }
            int num = Integer.parseInt(number);
            this.redisTemplate.opsForValue().set("number", String.valueOf(++num));
            //释放锁
            String script = "if(redis.call('get',KEYS[1])==ARGV[1]) then return redis.call('del',KEYS[2]) else return 0 end";
            this.redisTemplate.execute(new DefaultRedisScript<>(script, Boolean.class), Arrays.asList("lock", "lock"), uuid);
//            if (StringUtils.equals(redisTemplate.opsForValue().get("lock"), uuid)) {
//
//                this.redisTemplate.delete("lock");
//            }
        }
    }

    public void testLock() {
        //获取锁(设置过期时间防止服务器宕机带来的死锁)
        String uuid = UUID.randomUUID().toString();
        boolean flag = this.distributedLock.tryLock("lock", uuid, 30);
        if (flag) {

            String number = this.redisTemplate.opsForValue().get("number");
            if (StringUtils.isBlank(number)) {
                return;
            }
            int num = Integer.parseInt(number);

            this.redisTemplate.opsForValue().set("number", String.valueOf(++num));
            this.testSubLock(uuid);
            this.distributedLock.unLock("lock", uuid);
        }

    }

    public void testSubLock(String uuid) {
        this.distributedLock.tryLock("lock", uuid, 30);
        System.out.println("测试可重入锁");
        this.distributedLock.unLock("lock", uuid);

    }

}
