package com.im.lac.portal.service;

import java.io.InputStream;
import java.util.List;

public class FirstPassServiceImplementation implements PrototypeService {

    @Override
    public DatasetDescriptor createDataset(DatamartSearch dataMartSearch) {
        return null;
    }

    @Override
    public DatasetDescriptor createDataset(String format, InputStream sdfInputStream) {
        return null;
    }

    @Override
    public List<Long> listDatasetRowId(DatasetDescriptor datasetDescriptor) {
        return null;
    }

    @Override
    public List<DatasetDescriptor> listDatasetDescriptor() {
        return null;
    }

    @Override
    public List<PropertyDefinition> listPropertyDefinition(ListPropertyDefinitionFilter filter) {
        return null;
    }

    @Override
    public List<DatasetRow> listDatasetRow(ListDatasetRowFilter filter) {
        return null;
    }

}
