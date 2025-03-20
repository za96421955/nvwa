package ai.nvwa.components.util;

import ai.nvwa.components.Result;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * @description AES工具
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
public abstract class AESUtil {
    // 密钥算法
	public static final String ALGORITHM = "AES";
	// 密钥长度(java默认只能处理128位以内的长度,如果需要处理大于128位可以使用JCE解除密钥长度限制)
	public static final int KEY_SIZE = 128;

	public static void main(String[] args) throws Exception {
		System.out.println(AESUtil.generateKey());
	}

	/**
	 * @description 生成密钥
	 * <p> <功能详细描述> </p>
	 *
	 * @author 陈晨
	 */
	public static byte[] generateKeyByte() throws Exception {
		// 实例化
		KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM);
		// 初始化密钥长度
		kg.init(KEY_SIZE);
		// 生成秘密密钥
		SecretKey secretKey = kg.generateKey();
		// 获得密钥的二进制编码形式
		return secretKey.getEncoded();
	}

	/**
	 * @description 生成密钥
	 * <p> <功能详细描述> </p>
	 *
	 * @author 陈晨
	 */
	public static String generateKey() throws Exception {
		return Base64.encodeBase64String(generateKeyByte());
	}

	/**
	 * @description 生成密钥, 16进制
	 * <p> <功能详细描述> </p>
	 *
	 * @author 陈晨
	 */
	public static String generateKey16() throws Exception {
		return new String(Hex.encodeHex(generateKeyByte()));
	}

	/**
	 * @description 转换秘钥
	 * <p> <功能详细描述> </p>
	 *
	 * @author 陈晨
	 */
	private static byte[] key2Byte(String key) {
		return Base64.decodeBase64(key);
	}

	/**
	 * @description 获取密钥
	 * <p> <功能详细描述> </p>
	 *
	 * @author 陈晨
	 */
	private static Key getKey(byte[] key) {
		return new SecretKeySpec(key, ALGORITHM);
	}

	/**
	 * @description 解密
	 * <p> <功能详细描述> </p>
	 *
	 * @author 陈晨
	 */
	public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
		// 还原密钥
		Key k = getKey(key);
		// 实例化
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		// 初始化,设置为解密模式
		cipher.init(Cipher.DECRYPT_MODE, k);
		// 执行操作
		return cipher.doFinal(data);
	}

	/**
	 * @description 解密
	 * <p> <功能详细描述> </p>
	 *
	 * @author 陈晨
	 */
	public static byte[] decrypt(byte[] data, String key) throws Exception {
		return decrypt(data, key2Byte(key));
	}

	/**
	 * @description 解密
	 * <p> <功能详细描述> </p>
	 *
	 * @author 陈晨
	 */
	public static String decryptBase64(String base64data, String key) throws Exception {
		return new String(decrypt(Base64.decodeBase64(base64data), key2Byte(key)), StandardCharsets.UTF_8);
	}

	/**
	 * @description 解密
	 * <p> <功能详细描述> </p>
	 *
	 * @author 陈晨
	 */
	public static Result<String> decrypt(String base64data, String key) {
		try {
			return Result.success(decryptBase64(base64data, key));
		} catch (Exception e) {
			return Result.fail(e.getMessage());
		}
	}

	/**
	 * @description 加密
	 * <p> <功能详细描述> </p>
	 *
	 * @author 陈晨
	 */
	public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
		// 还原密钥
		Key k = getKey(key);
		// 实例化
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		// 初始化,设置为加密模式
		cipher.init(Cipher.ENCRYPT_MODE, k);
		// 执行操作
		return cipher.doFinal(data);
	}

	/**
	 * @description 加密
	 * <p> <功能详细描述> </p>
	 *
	 * @author 陈晨
	 */
	public static byte[] encrypt(byte[] data, String key) throws Exception {
		return encrypt(data, key2Byte(key));
	}

	/**
	 * @description 加密
	 * <p> <功能详细描述> </p>
	 *
	 * @author 陈晨
	 */
	public static String encryptBase64(String data, String key) throws Exception {
		return Base64.encodeBase64String(encrypt(data.getBytes(StandardCharsets.UTF_8), key));
	}

	/**
	 * @description 加密
	 * <p> <功能详细描述> </p>
	 *
	 * @author 陈晨
	 */
	public static Result<String> encrypt(String data, String key) {
		try {
			return Result.success(encryptBase64(data, key));
		} catch (Exception e) {
			return Result.fail(e.getMessage());
		}
	}

}
