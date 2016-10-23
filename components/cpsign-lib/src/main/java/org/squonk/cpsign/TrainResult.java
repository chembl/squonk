package org.squonk.cpsign;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.genettasoft.modeling.CVResult;

/**
 * Created by timbo on 21/10/2016.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class TrainResult {

    public enum Library {LibLinear, LibSVM}

    public enum Method {CCP, TCP, ACP}

    public enum Type {Regression, Classification}

    private final Library library;
    private final Type type;
    private final Method method;
    private final int signatureStartHeight;
    private final int signatureEndHeight;
    private final int cvFolds;
    private final String path;
    private final Double efficiency;
    private final Double validity;
    private final Double rmse;


    TrainResult(@JsonProperty("method") Method method,
                @JsonProperty("type") Type type,
                @JsonProperty("library") Library library,
                @JsonProperty("signatureStartHeight") int signatureStartHeight,
                @JsonProperty("signatureEndHeight") int signatureEndHeight,
                @JsonProperty("cvFolds") int cvFolds,
                @JsonProperty("efficiency") Double efficiency,
                @JsonProperty("validity") Double validity,
                @JsonProperty("rmse") Double rmse,
                @JsonProperty("path") String path) {
        this.method = method;
        this.type = type;
        this.library = library;
        this.signatureStartHeight = signatureStartHeight;
        this.signatureEndHeight = signatureEndHeight;
        this.cvFolds = cvFolds;
        this.efficiency = efficiency;
        this.validity = validity;
        this.rmse = rmse;
        this.path = path;
    }

    public Method getMethod() {
        return method;
    }

    public Type getType() {
        return type;
    }

    public Library getLibrary() {
        return library;
    }

    public int getSignatureStartHeight() {
        return signatureStartHeight;
    }

    public int getSignatureEndHeight() {
        return signatureEndHeight;
    }

    public int getCvFolds() {
        return cvFolds;
    }

    public Double getValidity() {
        return validity;
    }

    public Double getEfficiency() {
        return efficiency;
    }

    public Double getRmse() {
        return rmse;
    }

    public String getPath() {
        return path;
    }
}
