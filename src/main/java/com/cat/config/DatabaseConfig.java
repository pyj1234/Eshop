package com.cat.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static HikariDataSource dataSource;
    
    static {
        initializeDataSource();
    }
    
    private static void initializeDataSource() {
        try {
            // 加载数据库配置
            Properties props = loadDatabaseProperties();
            
            // 配置HikariCP连接池
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(props.getProperty("db.url", "jdbc:mysql://localhost:3306/eshop_db?useSSL=false&serverTimezone=UTC&characterEncoding=utf8"));
            config.setUsername(props.getProperty("db.username", "root"));
            config.setPassword(props.getProperty("db.password", ""));
            config.setDriverClassName(props.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));
            
            // 连接池配置
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.max-size", "20")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.min-idle", "5")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idle-timeout", "300000"))); // 5分钟
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connection-timeout", "20000"))); // 20秒
            config.setMaxLifetime(Long.parseLong(props.getProperty("db.pool.max-lifetime", "1800000"))); // 30分钟
            
            // 连接测试配置
            config.setConnectionTestQuery("SELECT 1");
            config.setValidationTimeout(5000);
            
            // 性能优化配置
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            
            // 设置连接池名称，便于监控
            config.setPoolName("EshopHikariPool");
            
            dataSource = new HikariDataSource(config);
            
            logger.info("数据库连接池初始化成功，连接池名称: {}", config.getPoolName());
            
        } catch (Exception e) {
            logger.error("数据库连接池初始化失败", e);
            throw new RuntimeException("数据库连接池初始化失败", e);
        }
    }
    
    private static Properties loadDatabaseProperties() {
        Properties props = new Properties();
        
        // 尝试从配置文件加载
        try (InputStream is = DatabaseConfig.class.getClassLoader().getResourceAsStream("database.properties")) {
            if (is != null) {
                props.load(is);
                logger.info("成功加载database.properties配置文件");
            } else {
                logger.warn("未找到database.properties配置文件，使用默认配置");
                setDefaultProperties(props);
            }
        } catch (IOException e) {
            logger.warn("加载database.properties失败，使用默认配置", e);
            setDefaultProperties(props);
        }
        
        return props;
    }
    
    private static void setDefaultProperties(Properties props) {
        props.setProperty("db.url", "jdbc:mysql://localhost:3306/eshop_db?useSSL=false&serverTimezone=UTC&characterEncoding=utf8");
        props.setProperty("db.username", "root");
        props.setProperty("db.password", "");
        props.setProperty("db.driver", "com.mysql.cj.jdbc.Driver");
        props.setProperty("db.pool.max-size", "20");
        props.setProperty("db.pool.min-idle", "5");
        props.setProperty("db.pool.idle-timeout", "300000");
        props.setProperty("db.pool.connection-timeout", "20000");
        props.setProperty("db.pool.max-lifetime", "1800000");
    }
    
    public static DataSource getDataSource() {
        if (dataSource == null || dataSource.isClosed()) {
            synchronized (DatabaseConfig.class) {
                if (dataSource == null || dataSource.isClosed()) {
                    initializeDataSource();
                }
            }
        }
        return dataSource;
    }
    
    public static void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("数据库连接池已关闭");
        }
    }
    
    public static boolean isDataSourceRunning() {
        return dataSource != null && !dataSource.isClosed();
    }
}