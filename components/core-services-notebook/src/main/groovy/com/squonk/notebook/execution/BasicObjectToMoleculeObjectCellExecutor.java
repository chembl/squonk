package com.squonk.notebook.execution;

import com.squonk.execution.steps.StepDefinition;
import static com.squonk.execution.steps.StepDefinitionConstants.*;
import com.squonk.notebook.api.CellDTO;
import com.squonk.execution.steps.impl.BasicObjectToMoleculeObjectStep;

/**
 * Converts a Dataset of BasicObjects to a Dataset of MoleculeObjects using the value
 * specified by the XXX option for the structure.
 * Note: the molecules are not validated in any way, and if they are invalid will 
 * result in errors downstream. 
 * 
 *
 * Created by timbo on 10/11/15.
 */
public class BasicObjectToMoleculeObjectCellExecutor extends AbstractStepExecutor {

    public static final String CELL_TYPE_BASICOBJECT_TO_MOLECULEOBJECT = "BasicObjectToMolecuelObject";
    
    public BasicObjectToMoleculeObjectCellExecutor() {
        super(CELL_TYPE_BASICOBJECT_TO_MOLECULEOBJECT);
    }

    @Override
    protected StepDefinition[] getStepDefintions(CellDTO cell) {

        StepDefinition step = new StepDefinition(STEP_BASICOBJECT_TO_MOLECULEOBJECT)
                .withFieldMapping(VARIABLE_INPUT_DATASET, "Input")
                .withFieldMapping(VARIABLE_OUTPUT_DATASET, "Results");

        step = configureOption(step, cell, BasicObjectToMoleculeObjectStep.OPTION_STRUCTURE_FIELD_NAME);
        step = configureOption(step, cell, BasicObjectToMoleculeObjectStep.OPTION_STRUCTURE_FORMAT);
        step = configureOption(step, cell, BasicObjectToMoleculeObjectStep.OPTION_PRESERVE_UUID);
        
        return new StepDefinition[]{step};

    }

}