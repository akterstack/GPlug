import com.jroadie.gplug.util.PropertyReader

dataSource {
    pooled = true
    url = PropertyReader.getProperty("db.url", "jdbc:mysql://localhost/g_plug?characterEncoding=utf8&autoReconnect=true")
    driverClassName = PropertyReader.getProperty("db.driver", "com.mysql.jdbc.Driver")
    username = PropertyReader.getProperty("db.user", "root")
    password = PropertyReader.getProperty("db.password", "")
    dialect = PropertyReader.getProperty("db.dialect", "org.hibernate.dialect.MySQL5InnoDBDialect")
    dbCreate = "update"
    properties {
        maxActive = 1000
        maxIdle = 100
        minIdle = 50
        initialSize = 1
        minEvictableIdleTimeMillis = 60000
        timeBetweenEvictionRunsMillis = 60000
        numTestsPerEvictionRun = 3
        maxWait = 10000
        testOnBorrow = true
        testWhileIdle = true
        testOnReturn = true
        validationQuery = "SELECT 1"
    }
}
hibernate {
    cache.use_second_level_cache = false
    cache.use_query_cache = false
    cache.use_minimal_puts = true
    cache.region.factory_class = 'org.hibernate.cache.ehcache.SingletonEhCacheRegionFactory'
}
grails.cache.config = {
    cache {
        name 'org.hibernate.cache.UpdateTimestampsCache'
        eternal true
        maxEntriesLocalHeap 500
        persistence {
            strategy localTempSwap
        }
    }
}
environments {
    development {
        dataSource {
            loggingSql = true
            properties {
                minEvictableIdleTimeMillis = 1800000
                timeBetweenEvictionRunsMillis = 1800000
            }
        }
    }
    test {
        dataSource {
            loggingSql = true
        }
    }
    production {
        dataSource {
            loggingSql = false
        }
    }
}
