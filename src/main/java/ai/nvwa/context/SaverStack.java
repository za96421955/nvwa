//package ai.nvwa.context;
//
//import com.alibaba.cloud.ai.graph.checkpoint.BaseCheckpointSaver;
//import org.springframework.util.CollectionUtils;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
///**
// * 记忆栈
// * <p> <功能详细描述> </p>
// *
// * @author 陈晨
// * @version 1.0
// * @date 2026/2/28
// */
//public abstract class SaverStack {
//    private static final Map<String, BaseCheckpointSaver> SAVERS;
//    static {
//        SAVERS = new HashMap<>();
//    }
//
//    private SaverStack() {}
//
//    public static void add(BaseCheckpointSaver saver) {
//        SAVERS.add(saver);
//    }
//
//    public static BaseCheckpointSaver get() {
//        if (SAVERS.isEmpty()) {
//            return null;
//        }
//        return SAVERS.get(SAVER_LIST.size() - 1);
//    }
//
//    public static void clear() {
//        if (SAVERS.isEmpty()) {
//            return;
//        }
//        SAVERS.remove(SAVER_LIST.size() - 1);
//        SAVERS.add(null);
//    }
//
//}
//
//
