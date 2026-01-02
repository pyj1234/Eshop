package com.cat.util;

import com.cat.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseUtil.class);
    
    /**
     * 获取数据库连接
     * @return 数据库连接
     * @throws SQLException 数据库异常
     */
    public static Connection getConnection() throws SQLException {
        return DatabaseConfig.getDataSource().getConnection();
    }
    
    /**
     * 关闭数据库连接
     * @param connection 数据库连接
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                if (!connection.isClosed()) {
                    connection.close();
                }
            } catch (SQLException e) {
                logger.error("关闭数据库连接失败", e);
            }
        }
    }
    
    /**
     * 关闭Statement
     * @param statement Statement对象
     */
    public static void closeStatement(Statement statement) {
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                logger.error("关闭Statement失败", e);
            }
        }
    }
    
    /**
     * 关闭ResultSet
     * @param resultSet ResultSet对象
     */
    public static void closeResultSet(ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                logger.error("关闭ResultSet失败", e);
            }
        }
    }
    
    /**
     * 关闭数据库资源
     * @param connection 数据库连接
     * @param statement Statement对象
     * @param resultSet ResultSet对象
     */
    public static void closeResources(Connection connection, Statement statement, ResultSet resultSet) {
        closeResultSet(resultSet);
        closeStatement(statement);
        closeConnection(connection);
    }
    
    /**
     * 执行查询并返回单个结果
     * @param sql SQL语句
     * @param params 参数
     * @return 单个结果，如果没有返回null
     */
    public static <T> T querySingle(String sql, ResultSetHandler<T> handler, Object... params) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            
            // 设置参数
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return handler.handle(resultSet);
            }
            
            return null;
            
        } catch (SQLException e) {
            logger.error("执行查询失败: {}", sql, e);
            throw new RuntimeException("执行查询失败", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }
    
    /**
     * 执行查询并返回结果列表
     * @param sql SQL语句
     * @param params 参数
     * @return 结果列表
     */
    public static <T> List<T> queryList(String sql, ResultSetHandler<T> handler, Object... params) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        List<T> result = new ArrayList<>();
        
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            
            // 设置参数
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            
            resultSet = statement.executeQuery();
            
            while (resultSet.next()) {
                result.add(handler.handle(resultSet));
            }
            
            return result;
            
        } catch (SQLException e) {
            logger.error("执行查询失败: {}", sql, e);
            throw new RuntimeException("执行查询失败", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }
    
    /**
     * 执行更新操作（INSERT, UPDATE, DELETE）
     * @param sql SQL语句
     * @param params 参数
     * @return 受影响的行数
     */
    public static int executeUpdate(String sql, Object... params) {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            
            // 设置参数
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            
            return statement.executeUpdate();
            
        } catch (SQLException e) {
            logger.error("执行更新失败: {}", sql, e);
            throw new RuntimeException("执行更新失败", e);
        } finally {
            closeResources(connection, statement, null);
        }
    }
    
    /**
     * 执行插入操作并返回生成的主键
     * @param sql SQL语句
     * @param params 参数
     * @return 生成的主键
     */
    public static long executeInsert(String sql, Object... params) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            // 设置参数
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            
            int affectedRows = statement.executeUpdate();
            
            if (affectedRows == 0) {
                throw new SQLException("插入操作失败，没有影响任何行");
            }
            
            resultSet = statement.getGeneratedKeys();
            if (resultSet.next()) {
                return resultSet.getLong(1);
            } else {
                throw new SQLException("插入操作失败，无法获取生成的主键");
            }
            
        } catch (SQLException e) {
            logger.error("执行插入失败: {}", sql, e);
            throw new RuntimeException("执行插入失败", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }
    
    /**
     * 执行批量更新
     * @param sql SQL语句
     * @param paramsList 参数列表
     * @return 每个操作影响的行数数组
     */
    public static int[] executeBatch(String sql, List<Object[]> paramsList) {
        Connection connection = null;
        PreparedStatement statement = null;
        
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            statement = connection.prepareStatement(sql);
            
            // 批量设置参数
            for (Object[] params : paramsList) {
                for (int i = 0; i < params.length; i++) {
                    statement.setObject(i + 1, params[i]);
                }
                statement.addBatch();
            }
            
            int[] results = statement.executeBatch();
            connection.commit();
            
            return results;
            
        } catch (SQLException e) {
            logger.error("执行批量更新失败: {}", sql, e);
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    logger.error("回滚事务失败", rollbackEx);
                }
            }
            throw new RuntimeException("执行批量更新失败", e);
        } finally {
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                } catch (SQLException e) {
                    logger.error("恢复自动提交模式失败", e);
                }
            }
            closeResources(connection, statement, null);
        }
    }
    
    /**
     * 检查是否存在记录
     * @param sql SQL语句
     * @param params 参数
     * @return 是否存在记录
     */
    public static boolean exists(String sql, Object... params) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            
            // 设置参数
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            
            resultSet = statement.executeQuery();
            return resultSet.next();
            
        } catch (SQLException e) {
            logger.error("检查记录存在性失败: {}", sql, e);
            throw new RuntimeException("检查记录存在性失败", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }
    
    /**
     * 获取记录总数
     * @param sql SQL语句
     * @param params 参数
     * @return 记录总数
     */
    public static long count(String sql, Object... params) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        
        try {
            connection = getConnection();
            statement = connection.prepareStatement(sql);
            
            // 设置参数
            for (int i = 0; i < params.length; i++) {
                statement.setObject(i + 1, params[i]);
            }
            
            resultSet = statement.executeQuery();
            
            if (resultSet.next()) {
                return resultSet.getLong(1);
            }
            
            return 0;
            
        } catch (SQLException e) {
            logger.error("获取记录总数失败: {}", sql, e);
            throw new RuntimeException("获取记录总数失败", e);
        } finally {
            closeResources(connection, statement, resultSet);
        }
    }
    
    /**
     * ResultSet处理接口
     * @param <T> 返回类型
     */
    @FunctionalInterface
    public interface ResultSetHandler<T> {
        T handle(ResultSet resultSet) throws SQLException;
    }
}