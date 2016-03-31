package org.squonk.api;

import org.squonk.types.DatasetHandler;
import org.squonk.types.InputStreamHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by timbo on 31/03/16.
 */
public class VariableHandlerRegistry {

    public static final VariableHandlerRegistry INSTANCE = new VariableHandlerRegistry();

    private final Map<Class,VariableHandler> handlers = new HashMap<>();


    public VariableHandlerRegistry() {
        register(new DatasetHandler());
        register(new InputStreamHandler());
    }

    public void register(VariableHandler handler) {
        handlers.put(handler.getType(), handler);
    }

    public <T> VariableHandler<T> lookup(Class<T> type) {
        return handlers.get(type);
    }

}