package com.squonk.notebook.execution.steps

import com.squonk.notebook.execution.variable.*
import com.im.lac.types.MoleculeObject
import com.squonk.dataset.*
import com.squonk.dataset.transform.TransformDefintions
import java.util.stream.Stream
import org.apache.camel.*
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.impl.DefaultCamelContext
import spock.lang.Specification

/**
 *
 * @author timbo
 */
class MoleculeServiceThinExecutorStepSpec extends Specification {
    
    void "test write mols"() {
        
        DefaultCamelContext context = new DefaultCamelContext()
        context.start()
        
        def mols = [
            new MoleculeObject("C", "smiles", [num:"1",hello:'world']),
            new MoleculeObject("CC", "smiles", [num:"99",hello:'mars']),
            new MoleculeObject("CCC", "smiles", [num:"100",hello:'mum'])
        ]
        Dataset ds = new Dataset(MoleculeObject.class, mols)
        
        
        VariableManager varman = new VariableManager(new MemoryVariableLoader());
        
        Variable dsvar = varman.createVariable(
            MoleculeServiceThinExecutorStep.VAR_INPUT_DATASET, 
            Dataset.class, 
            ds,
            Variable.PersistenceType.NONE)
        
        MoleculeServiceThinExecutorStep step = new MoleculeServiceThinExecutorStep()
        step.configure([(MoleculeServiceThinExecutorStep.OPTION_SERVICE_ENDPOINT):'http://demos.informaticsmatters.com:9080/chem-services-cdk-basic/rest/v1/calculators/logp'], [:])
        
        when:
        step.execute(varman, context)
        def var = varman.lookupVariable(MoleculeServiceThinExecutorStep.VAR_OUTPUT_DATASET)
        
        then:
        var != null
        def result = varman.getValue(var)
        result.items.size() == 3
        result.items[0].values.size() == 6
        result.items[0].values.containsKey('CDK_ALogP') // new
        result.items[0].values.containsKey('num')       // original
        
        cleanup:
        context.stop();
    }

}