package ai.nvwa.agent.components.util.runtime;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class RuntimeContext implements Context {
    private final Map<String, Object> contextMap = new ConcurrentHashMap<>();

    @Override
    public void put(String name, Object obj) {
        if (null == obj) {
            return;
        }
        contextMap.put(name, obj);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Optional<T> get(String name, Class<T> type) {
        Object o = contextMap.get(name);
        if (o != null && o.getClass() == type) {
            return (Optional<T>) Optional.of(o);
        }
        return Optional.empty();
    }

    @Override
    public <T> T get(String name, Class<T> type, Function<String, T> function) {
        return get(name,type).orElseGet(() -> {
            T apply = function.apply(name);
            contextMap.put(name,apply);
            return apply;
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String name) {
        return (T) contextMap.get(name);
    }

    @Override
    public void remove(String name) {
        contextMap.remove(name);
    }

    @Override
    public void clear() {
        contextMap.clear();
    }

}


