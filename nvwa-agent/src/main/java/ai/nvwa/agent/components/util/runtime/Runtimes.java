package ai.nvwa.agent.components.util.runtime;

public abstract class Runtimes {
//    private static final TransmittableThreadLocal<Context> LOCAL = new TransmittableThreadLocal<>();
    private static final ThreadLocal<Context> LOCAL = new ThreadLocal<>();

    private static Context initialize() {
//        Context context = new RuntimeContext();
//        context.put("tlogTraceId", TLogContext.getTraceId());
        LOCAL.set(new RuntimeContext());
        return LOCAL.get();
    }

    private static Context getContext() {
        Context context = LOCAL.get();
        if (context == null) {
            return initialize();
        }
//        // 检查调用链
//        String currTraceId = TLogContext.getTraceId();
//        String contextTraceId = context.get("tlogTraceId");
//        if (StringUtils.isNotBlank(currTraceId)
//                && !currTraceId.equals(contextTraceId)) {
//            // 调用链变更、清除缓存
//            clear();
//            return initialize();
//        }
        return context;
    }

    public static void clear() {
        Context context = LOCAL.get();
        if (context != null) {
            context.clear();
        }
        LOCAL.remove();
    }

    public static void put(String name, Object obj) {
        getContext().put(name, obj);
    }

    public static <T> T get(String name) {
        return getContext().get(name);
    }

}


