package ai.nvwa.components.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * JSON工具
 * <p> <功能详细描述> </p>
 *
 * @author 陈晨
 */
public abstract class JSONUtil {

    private JSONUtil() {}

    /**
     * @description 对象转JSONObject
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public static JSONObject parseJSONObject(Object obj) {
        if (null == obj) {
            return null;
        }
        try {
            return JSONObject.parseObject(toJSONString(obj, true));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @description JSON转对象
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public static <T> T parseObject(String jsonString, Class<T> clazz) {
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }
        return JSONObject.parseObject(jsonString, clazz);
    }

    /**
     * @description 是否是JSONObject
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public static boolean isJSONObject(Object obj) {
        return null != parseJSONObject(obj);
    }

    /**
     * @description 获取JSONObject
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public static JSONObject getJSONObject(Object obj, String key) {
        if (!(obj instanceof JSONObject)) {
            return null;
        }
        JSONObject json = (JSONObject) obj;
        if (!json.containsKey(key)) {
            return null;
        }
        try {
            return json.getJSONObject(key);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @description 对象转JSONArray
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public static JSONArray parseJSONArray(Object obj) {
        if (null == obj) {
            return null;
        }
        try {
            return JSONArray.parseArray(toJSONString(obj, false));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * @description JSONArray转对象
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public static <T> List<T> parseArray(String jsonString, Class<T> clazz) {
        if (StringUtils.isBlank(jsonString)) {
            return null;
        }
        return JSONArray.parseArray(jsonString, clazz);
    }

    /**
     * @description 是否是JSONArray
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public static boolean isJSONArray(Object obj) {
        return null != parseJSONArray(obj);
    }

    /**
     * @description 获取JSONArray
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public static JSONArray getJSONArray(Object obj, String key) {
        if (!(obj instanceof JSONObject)) {
            return null;
        }
        JSONObject json = (JSONObject) obj;
        if (!json.containsKey(key)) {
            return null;
        }
        try {
            return json.getJSONArray(key);
        } catch (Exception e) {
            return null;
        }
    }

    public static void forEach(JSONArray array, ObjectCall call) {
        if (null == array) {
            return;
        }
        for (int i = 0; i < array.size(); i++) {
            call.call(i, array.get(i), array.get(i).getClass());
        }
    }
    public interface ObjectCall {
        void call(int i, Object obj, Class<?> clazz);
    }

    public static void forEach(JSONArray array, JSONObjectCall call) {
        if (null == array) {
            return;
        }
        for (int i = 0; i < array.size(); i++) {
            call.call(i, parseJSONObject(array.get(i)));
        }
    }
    public interface JSONObjectCall {
        void call(int i, JSONObject json);
    }

    /**
     * @description 对象转换JSON字符串
     * <p> <功能详细描述> </p>
     *
     * @author 陈晨
     */
    public static String toJSONString(Object obj, boolean... isObject) {
        if (null == obj) {
            return null;
        }
        if (ArrayUtils.isNotEmpty(isObject)) {
            if (isObject[0]) {
                JSONObject.toJSONString(obj, SerializerFeature.WriteMapNullValue);
            } else {
                JSONArray.toJSONString(obj, SerializerFeature.WriteMapNullValue);
            }
        }
        try {
            return JSONObject.toJSONString(obj, SerializerFeature.WriteMapNullValue);
        } catch (Exception ignored) {}
        try {
            return JSONArray.toJSONString(obj, SerializerFeature.WriteMapNullValue);
        } catch (Exception ignored) {}
        return null;
    }

}


