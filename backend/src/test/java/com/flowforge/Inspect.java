package com.flowforge;
import io.github.bucket4j.redis.redisson.cas.RedissonBasedProxyManager;
import java.lang.reflect.Method;
public class Inspect {
    public static void main(String[] args) {
        for (Method m : RedissonBasedProxyManager.class.getMethods()) {
            if (m.getName().startsWith("builderFor")) {
                System.out.println(m.toString());
            }
        }
    }
}
