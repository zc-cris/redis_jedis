package com.zc.cris.redis.jedis.test;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

// jedis 连接池工具类
public class JedisUtil {
	
	private static volatile JedisPool jedispool = null;
	
	private JedisUtil(){};
	
	public static JedisPool getJedisPoolInstance() {
		
		if(null == jedispool) {
			synchronized (JedisUtil.class) {
				if(null == jedispool) {
					JedisPoolConfig poolConfig = new JedisPoolConfig();
					// poolConfig.setMaxActive(100);
					poolConfig.setMaxTotal(100);
					poolConfig.setMaxIdle(10);
					poolConfig.setMaxWaitMillis(100*1000);
					poolConfig.setTestOnBorrow(true);
					
					jedispool = new JedisPool(poolConfig, "192.168.38.3", 6379);
				}
			}
		}
		return jedispool;
	}
	
	// 释放jedis连接
	public static void release(JedisPool jedisPool, Jedis jedis) {
		if(null != jedis && null != jedisPool) {
			jedisPool.returnResourceObject(jedis);
		}
	}

}
