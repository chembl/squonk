package com.im.lac.services.job.service.steps;

import com.im.lac.services.job.variable.Variable;
import com.im.lac.services.job.variable.VariableManager;
import com.squonk.dataset.Dataset;
import com.squonk.dataset.DatasetMetadata;
import com.squonk.dataset.DatasetProvider;
import com.squonk.types.io.JsonHandler;
import java.io.InputStream;
import java.util.stream.Stream;

/**
 *
 * @author timbo
 */
public class DatasetWriterStep extends AbstractStep {
    
    public static final String FIELD_SOURCE_DATASET = "SourceDataset";
    public static final String FIELD_OUTPUT_DATA = "OutputData";
    public static final String FIELD_OUTPUT_METADATA = "OutputMetadata";

    @Override
    public void execute(VariableManager varman) throws Exception {
        DatasetProvider p = fetchMappedValue(FIELD_SOURCE_DATASET, DatasetProvider.class, varman);
        Dataset ds = p.getDataset();
        Stream s = ds.createMetadataGeneratingStream(ds.getStream());
        ds.replaceStream(s);
        try (InputStream is = ds.getInputStream(false)) {
            Variable d = varman.writeVariable(FIELD_OUTPUT_DATA, Dataset.class, is, true);
            System.out.println("JSON DA: " + varman.getValue(d));
        }
        DatasetMetadata md = ds.getMetadata();
        String json = JsonHandler.getInstance().objectToJson(md);
        System.out.println("JSON MD: " + json);
        varman.writeVariable(FIELD_OUTPUT_METADATA, DatasetMetadata.class, json, true);
    }
    
}
