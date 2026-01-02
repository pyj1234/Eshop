package com.cat.listener;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class SessionListener implements HttpSessionListener {
    
    private static final Logger logger = LoggerFactory.getLogger(SessionListener.class);
    private static int activeSessionCount = 0;
    
    @Override
    public void sessionCreated(HttpSessionEvent se) {
        activeSessionCount++;
        logger.info("Session创建成功，SessionID: {}, 当前活跃Session数: {}", 
                    se.getSession().getId(), activeSessionCount);
    }
    
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        activeSessionCount--;
        logger.info("Session销毁成功，SessionID: {}, 当前活跃Session数: {}", 
                    se.getSession().getId(), activeSessionCount);
    }
    
    /**
     * 获取当前活跃Session数量
     * @return 活跃Session数量
     */
    public static int getActiveSessionCount() {
        return activeSessionCount;
    }
}