@Configuration
@EnableCaching
public class CacheConfig {

  @Bean
  public JedisConnectionFactory redisConnectionFactory() {
    return new JedisConnectionFactory();
  }

  @Bean
  public RedisTemplate<String, String> redisTemplate(RedisConnectionFactory cf) {
    RedisTemplate<String, String> redisTemplate = new RedisTemplate<String, String>();
    redisTemplate.setConnectionFactory(cf);
    return redisTemplate;
  }

  @Bean
  public CacheManager cacheManager(RedisTemplate redisTemplate) {
    return new RedisCacheManager(redisTemplate);
  }

  @Bean
  public KeyGenerator keyGenerator() {
    return new KeyGenerator() {
      @Override
      public Object generate(Object o, Method method, Object... params) {
        StringBuilder sb = new StringBuilder();
        sb.append(o.getClass().getName());
        sb.append(method.getName());
        for (Object param : params) {
          sb.append(param.toString());
        }
        return sb.toString();
      }
    };
  }
}
