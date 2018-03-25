package com.zc.cris.redis.jedis.test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tomcat.util.collections.SynchronizedStack;
import org.junit.jupiter.api.Test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Response;
import redis.clients.jedis.Transaction;

class TestCase {

	Jedis jedis = new Jedis("192.168.38.3", 6379);

	/*
	 * 测试 jedisPool 连接池
	 */
	@Test
	void testJedisPool() {
		JedisPool jedisPool = JedisUtil.getJedisPoolInstance();

		// JedisPool jedisPool2 = JedisUtil.getJedisPoolInstance();
		// System.out.println(jedisPool == jedisPool2);

		Jedis jedis = null;

		try {
			jedis = jedisPool.getResource();
			jedis.set("ab", "cd");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JedisUtil.release(jedisPool, jedis);
		}

	}

	/*
	 * 测试主从复制(主从复制和sentinel模式 都是在运维配置好了，java端只需要知道读写分离关系即可)
	 */
	@Test
	void testMasterSlaver() {

		Jedis jedis2 = new Jedis("192.168.38.3", 6380);
		jedis2.slaveof("192.168.38.3", 6379);
		jedis.set("k1", "v2");

		System.out.println(jedis2.get("k1"));
	}

	/*
	 * 模拟redis 中事务的加锁机制
	 */
	@Test
	void testRedisTXLock() {

		Integer balance = null;
		Integer dept = null;
		Integer cusume = 60;

		// 监控余额
		jedis.watch("balance");
		balance = Integer.valueOf(jedis.get("balance"));
		// 如果消费大于余额
		if (cusume > balance) {
			jedis.unwatch();
			System.out.println("余额不足！");
			return;
		}
		// jedis.set("balance", "10");
		// 开启事务
		Transaction transaction = jedis.multi();
		transaction.decrBy("balance", cusume);
		transaction.incrBy("dept", cusume);
		// 提交事务
		transaction.exec();
		System.out.println("余额还有:" + jedis.get("balance"));
		System.out.println("债务还有:" + jedis.get("dept"));

	}

	/*
	 * 测试redis的正常事务
	 */
	@Test
	void testRedisTX() {
		Transaction transaction = jedis.multi();
		transaction.set("k9", "v9");
		transaction.set("k10", "v10");
		transaction.exec();
		// transaction.discard();
	}

	/*
	 * 测试redis 的Zset数据类型
	 */
	@Test
	void testRedisZset() {
		jedis.zadd("zset01", 10d, "z1");
		jedis.zadd("zset01", 20d, "z2");
		jedis.zadd("zset01", 30d, "z3");
		jedis.zadd("zset01", 40d, "z4");

		Set<String> set = jedis.zrange("zset01", 0, -1);
		Iterator<String> iterator = set.iterator();
		while (iterator.hasNext()) {
			System.out.println(iterator.next());
		}

	}

	/*
	 * 测试redis 的hash数据类型
	 */
	@Test
	void testRedisHash() {
		jedis.hset("hash01", "username", "詹姆斯");
		System.out.println(jedis.hget("hash01", "username"));
		Map<String, String> map = new HashMap<>();
		map.put("password", "1234");
		map.put("telephone", "2323");
		map.put("email", "9904@qq.com");

		jedis.hmset("hash02", map);
		List<String> list = jedis.hmget("hash02", "password", "telephone");
		for (String string : list) {
			System.out.println(string);
		}

	}

	/*
	 * 测试redis的 set数据类型
	 */
	@Test
	void testRedisSet() {

		jedis.sadd("set01", "a");
		jedis.sadd("set01", "b");
		jedis.sadd("set01", "c");

		Set<String> set = jedis.smembers("set01");
		Iterator<String> iterator = set.iterator();
		while (iterator.hasNext()) {
			System.out.println(iterator.next());
		}

		jedis.srem("set01", "a");
		System.out.println(jedis.smembers("set01").size());

	}

	/*
	 * 测试 redis的list数据类型
	 */
	@Test
	void testRedisList() {

		jedis.lpush("mylist", "v1", "v2", "v3", "v4");
		List<String> list = jedis.lrange("mylist", 0, -1);
		for (String string : list) {
			System.out.println(string);
		}

	}

	/*
	 * 测试key类型为String 的常用api
	 */
	@Test
	void testRedisString() {

		// 往key 为k1 的value中添加字符串
		jedis.append("k1", "思密达");
		System.out.println(jedis.get("k1"));

		// 一次性设置多个键值对
		jedis.mset("str1", "1", "str2", "2", "str3", "3");
		// 一次性取出多个键值对
		System.out.println(jedis.mget("str1", "str2", "str3"));

	}

	/*
	 * 通过java代码测试 redis 的key
	 */
	@Test
	void testRedisAPI1() {

		jedis.set("k1", "v1");
		jedis.set("k2", "v2");
		jedis.set("k3", "v3");
		String v1 = jedis.get("k1");
		System.out.println(v1);

		// 取出所有键的集合
		Set<String> keys = jedis.keys("*");
		System.out.println(keys.size());
		Iterator<String> iterator = keys.iterator();
		while (iterator.hasNext()) {
			System.out.println(iterator.next());
		}

		System.out.println(jedis.exists("k1"));
		// 测试key 的存活时间
		System.out.println(jedis.ttl("k2"));
	}

	@Test
	void testRedisConnection() {

		System.out.println(jedis.ping());

	}

}
