package ai.nvwa.agent.components.util;

import ai.nvwa.agent.components.Result;
import org.apache.commons.lang3.ArrayUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @description MD5工具
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
public abstract class MD5Util {

    /**
     * @description 生成MD5签名
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public static Result<String> generateByKey(String key, String... contents) {
        if (ArrayUtils.isEmpty(contents)) {
            return null;
        }
        StringBuilder sb = new StringBuilder(key);
        for (String content : contents) {
            sb.append("&").append(content);
        }
        try {
            return Result.success(md5_32(sb.toString().toUpperCase()));
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * @description 生成MD5签名
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public static Result<String> generate(String... contents) {
        if (ArrayUtils.isEmpty(contents)) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String content : contents) {
            sb.append("&").append(content);
        }
        try {
            return Result.success(md5_32(sb.substring(1).toUpperCase()));
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    private static String md5_32(String sourceStr) throws Exception {
        String result = "";
        try {
            byte[] b = md5(sourceStr.getBytes(StandardCharsets.UTF_8));
            StringBuilder buf = new StringBuilder("");
            for (int j : b) {
                int i = j;
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            result = buf.toString();
        } catch (NoSuchAlgorithmException var6) {
            var6.printStackTrace();
        }
        return result;
    }

    private static byte[] md5(byte[] bytes) throws Exception {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(bytes);
        return md.digest();
    }

}


