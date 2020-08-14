# jetcache-extend

扩展jetcache，支持：
* 支持使用fastjson、gson、jackson的序列化
* 简化封装jetcache的锁操作，见`JetCacheLockService`
* 增加通过name操作缓存的util单元
* TODO：考虑支持批量cache操作

## 引入
```xml
    <dependency>
        <groupId>com.edhn.cache</groupId>
        <artifactId>jetcache-extend-starter</artifactId>
        <version>1.0.2</version>
    </dependency>
```

## 功能

### json序列化配置

```yml
jetcache:
  statIntervalMinutes: 60
  areaInCacheName: false
  local:
    default:
      type: caffeine
      keyConvertor: fastjson
  remote:
    default:
      type: redis
      keyConvertor: fastjson
      valueEncoder: fastjson # valueEncoder和valueDecoder可以使用fastjson
      valueDecoder: fastjson
# ...
```

## CHANGELOGS
### 1.0.2
* 支持fastjson、jackson、gson序列化（gson序列化需主动引入相关包）
* 提供JetCacheLockService简化锁操作