package com.im.lac.services.job.service.steps;

import com.im.lac.services.job.variable.Variable;
import com.im.lac.services.job.variable.VariableManager;
import java.io.IOException;
import java.util.Map;

/**
 *
 * @author timbo
 */
public abstract class AbstractStep implements Step {

    protected Map<String, Object> options;
    protected Map<String, String> variableMappings;

    @Override
    public void configure(Map<String, Object> options, Map<String, String> variableMappings) {
        this.options = options;
        this.variableMappings = variableMappings;
    }

    protected String mapVariableName(String name) {
        String mapped = null;
        if (variableMappings != null) {
            mapped = variableMappings.get(name);
        }
        return (mapped == null ? name : mapped);
    }

    /**
     * Map the variable name using the variable mappings and fetch the
     * corresponding value.
     *
     * @param <T>
     * @param internalName
     * @param type
     * @param varman
     * @return
     * @throws IOException
     */
    protected <T> T fetchMappedValue(String internalName, Class<T> type, VariableManager varman) throws IOException {
        String mappedVarName = mapVariableName(internalName);
        return fetchValue(mappedVarName, type, varman);
    }

    /**
     * Fetch the value with this name
     *
     * @param <T>
     * @param internalName
     * @param type
     * @param varman
     * @return
     * @throws IOException
     */
    protected <T> T fetchValue(String internalName, Class<T> type, VariableManager varman) throws IOException {
        Variable<T> var = varman.lookupVariable(internalName);
        if (var == null) {
            throw new IllegalStateException("Required variable " + internalName + " not present");
        }
        // TODO - use type convertor mechanism 
        T value = (T) varman.getValue(var);
        return value;
    }
    
    protected <T>Variable<T> createMappedVariable(
            String internalName, 
            Class<T> type, 
            T value, 
            Variable.PersistenceType persistenceType, 
            VariableManager varman) throws IOException {
        
        String mappedVarName = mapVariableName(internalName);
        return varman.createVariable(mappedVarName, type, value, persistenceType);
    }

    protected <T> T getOption(String name, Class<T> type) {
        if (options != null) {
            return (T) options.get(name);
        }
        return null;
    }
    
    protected <T> T getOption(String name, Class<T> type, T defaultValue) {
        T val = getOption(name, type);
        if (val == null) {
            return defaultValue;
        } else {
            return val;
        }
    }

}