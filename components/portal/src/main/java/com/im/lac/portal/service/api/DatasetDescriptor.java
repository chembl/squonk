package com.im.lac.portal.service.api;

import java.io.Serializable;
import java.util.List;

public class DatasetDescriptor implements Serializable {

    private Long id;
    private String description;
    private List<DatasetRowDescriptor> datasetRowDescriptorList;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
