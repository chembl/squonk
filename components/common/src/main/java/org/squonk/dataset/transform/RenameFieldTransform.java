package org.squonk.dataset.transform;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 *
 * @author timbo
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class RenameFieldTransform extends AbstractTransform {

    private final String fieldName;
    private final String newName;

    protected RenameFieldTransform(@JsonProperty("fieldName")String fieldName, @JsonProperty("newName")String newName) {
        this.fieldName = fieldName;
        this.newName = newName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getNewName() {
        return newName;
    }

}
