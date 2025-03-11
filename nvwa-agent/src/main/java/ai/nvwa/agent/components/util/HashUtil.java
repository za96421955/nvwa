package ai.nvwa.agent.components.util;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * @version v1.0
 * @Description: Hash工具
 * <p>〈功能详细描述〉</p>
 *
 * @ClassName HashUtil
 * @author 陈晨
 */
public abstract class HashUtil {
    private static final String ASSIGN_HASH_KEY = "@AssignHashKey@";

    private static final int NUM0 = 0;
    private static final int NUM1 = 1;
    private static final int NUM2 = 2;
    private static final int NUM3 = 3;
    private static final int NUM4 = 4;
    private static final int NUM8 = 8;
    private static final int NUM16 = 16;
    private static final int NUM24 = 24;
    private static final int NUM_0XFF = 0xFF;

    private HashUtil() {}

    private static long hash(byte[] digest, int nTime) {
        long rv = ((long) (digest[NUM3 + nTime * NUM4] & NUM_0XFF) << NUM24)
                | ((long) (digest[NUM2 + nTime * NUM4] & NUM_0XFF) << NUM16)
                | ((long) (digest[NUM1 + nTime * NUM4] & NUM_0XFF) << NUM8)
                | (digest[NUM0 + nTime * NUM4] & NUM_0XFF);
        return rv & 0xffffffffL; /* Truncate to 32-bits */
    }

    /**
     * DalClient 内部Hash算法
     * <p>提取源码, 添加捕获异常</p>
     *
     * @param k
     * @param n
     * @return
     * @author 陈晨
     */
    public static long hash(String k, int n) {
        if (StringUtils.isBlank(k) || n <= 0) {
            return 0;
        }

        // 指定Hash
        if (k.contains(ASSIGN_HASH_KEY)) {
            return Long.parseLong(k.replaceAll(ASSIGN_HASH_KEY, ""));
        }

        // 正常Hash
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.reset();
            md5.update(k.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md5.digest();
            return hash(digest, 0) % n;
        } catch (Exception e) {
//            logger.error("MD5 exception, {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
    }

    /**
     * @description 指定Hash
     * <p>〈功能详细描述〉</p>
     *
     * @auther  陈晨
     */
    public static String getAssignHashKey(long index) {
        return ASSIGN_HASH_KEY + index;
    }

    /**
     * @description 获取32位Hash字符
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public static String hash32(String str) {
        char[] hexDigits = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.reset();
            md5.update(str.getBytes(StandardCharsets.UTF_8));
            // 获得密文
            byte[] md = md5.digest();
            // 把密文转换成十六进制的字符串形式
            char[] chars = new char[md.length * 2];
            int k = 0;
            for (byte b : md) {
                chars[k++] = hexDigits[b >>> 4 & 0xf];
                chars[k++] = hexDigits[b & 0xf];
            }
            return new String(chars);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}


