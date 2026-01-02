package com.cat.listener;

import com.cat.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ContextListener implements ServletContextListener {
    
    private static final Logger logger = LoggerFactory.getLogger(ContextListener.class);
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("CAT201电商网站应用启动中...");
        
        try {
            // 初始化数据库连接池
            DatabaseConfig.getDataSource();
            logger.info("数据库连接池初始化成功");
            
            // 设置系统属性
            System.setProperty("file.encoding", "UTF-8");
            
            logger.info("CAT201电商网站应用启动完成");
            
        } catch (Exception e) {
            logger.error("CAT201电商网站应用启动失败", e);
            throw new RuntimeException("应用启动失败", e);
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("CAT201电商网站应用关闭中...");
        
        try {
            // 关闭数据库连接池
            DatabaseConfig.closeDataSource();
            logger.info("数据库连接池已关闭");
            
            logger.info("CAT201电商网站应用关闭完成");
            
        } catch (Exception e) {
            logger.error("CAT201电商网站应用关闭时发生错误", e);
        }
    }
}