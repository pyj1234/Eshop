package com.cat.util;

import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtil {
    private static final Logger logger = LoggerFactory.getLogger(PasswordUtil.class);
    
    // BCrypt默认强度
    private static final int BCRYPT_ROUNDS = 12;
    
    // 生成随机盐值的字符集
    private static final String SALT_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    
    /**
     * 加密密码
     * @param plainPassword 明文密码
     * @return 加密后的密码
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("密码不能为空");
        }
        
        try {
            return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_ROUNDS));
        } catch (Exception e) {
            logger.error("密码加密失败", e);
            throw new RuntimeException("密码加密失败", e);
        }
    }
    
    /**
     * 验证密码
     * @param plainPassword 明文密码
     * @param hashedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (Exception e) {
            logger.error("密码验证失败", e);
            return false;
        }
    }
    
    /**
     * 生成随机密码
     * @param length 密码长度
     * @return 随机密码
     */
    public static String generateRandomPassword(int length) {
        if (length < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }
        
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();
        
        // 确保包含至少一个大写字母、小写字母和数字
        password.append(SALT_CHARS.charAt(random.nextInt(26))); // 大写字母
        password.append(SALT_CHARS.charAt(random.nextInt(26) + 26)); // 小写字母
        password.append(SALT_CHARS.charAt(random.nextInt(10) + 52)); // 数字
        
        // 填充剩余长度
        for (int i = 3; i < length; i++) {
            password.append(SALT_CHARS.charAt(random.nextInt(SALT_CHARS.length())));
        }
        
        // 打乱字符顺序
        return shuffleString(password.toString(), random);
    }
    
    /**
     * 生成默认长度的随机密码（8位）
     * @return 随机密码
     */
    public static String generateRandomPassword() {
        return generateRandomPassword(8);
    }
    
    /**
     * 生成随机令牌
     * @param length 令牌长度
     * @return 随机令牌
     */
    public static String generateToken(int length) {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    
    /**
     * 生成默认长度的随机令牌（32位）
     * @return 随机令牌
     */
    public static String generateToken() {
        return generateToken(32);
    }
    
    /**
     * 验证密码强度
     * @param password 密码
     * @return 密码强度等级（1-5，5为最强）
     */
    public static int checkPasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return 0;
        }
        
        int strength = 0;
        
        // 长度检查
        if (password.length() >= 8) {
            strength++;
        }
        if (password.length() >= 12) {
            strength++;
        }
        
        // 字符类型检查
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
        
        if (hasLower) strength++;
        if (hasUpper) strength++;
        if (hasDigit) strength++;
        if (hasSpecial) strength++;
        
        // 限制最大强度为5
        return Math.min(strength, 5);
    }
    
    /**
     * 获取密码强度描述
     * @param password 密码
     * @return 强度描述
     */
    public static String getPasswordStrengthDescription(String password) {
        int strength = checkPasswordStrength(password);
        
        switch (strength) {
            case 0:
                return "非常弱";
            case 1:
                return "弱";
            case 2:
                return "一般";
            case 3:
                return "较强";
            case 4:
                return "强";
            case 5:
                return "非常强";
            default:
                return "未知";
        }
    }
    
    /**
     * 检查密码是否符合基本要求
     * @param password 密码
     * @return 是否符合要求
     */
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        
        // 至少包含一个字母或数字
        return password.matches(".*[a-zA-Z0-9].*");
    }
    
    /**
     * 检查密码是否符合高级要求
     * @param password 密码
     * @return 是否符合要求
     */
    public static boolean isStrongPassword(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }
        
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        return hasLower && hasUpper && hasDigit;
    }
    
    /**
     * 打乱字符串
     * @param str 要打乱的字符串
     * @param random 随机数生成器
     * @return 打乱后的字符串
     */
    private static String shuffleString(String str, SecureRandom random) {
        char[] chars = str.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
    
    /**
     * 生成盐值（自定义实现，一般使用BCrypt自带的即可）
     * @param length 盐值长度
     * @return 盐值
     */
    public static String generateSalt(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder salt = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            salt.append(SALT_CHARS.charAt(random.nextInt(SALT_CHARS.length())));
        }
        
        return salt.toString();
    }
    
    /**
     * 使用SHA-256进行简单加密（仅用于非敏感数据）
     * @param input 输入字符串
     * @param salt 盐值
     * @return 加密后的字符串
     */
    public static String sha256(String input, String salt) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            String saltedInput = input + salt;
            byte[] hash = digest.digest(saltedInput.getBytes("UTF-8"));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            logger.error("SHA-256加密失败", e);
            throw new RuntimeException("SHA-256加密失败", e);
        }
    }
}