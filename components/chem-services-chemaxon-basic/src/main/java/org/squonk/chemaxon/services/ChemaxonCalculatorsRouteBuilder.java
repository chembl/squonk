package org.squonk.chemaxon.services;

import org.squonk.camel.CamelCommonConstants;
import org.squonk.camel.chemaxon.processor.ChemAxonMoleculeProcessor;

import org.apache.camel.builder.RouteBuilder;
import org.squonk.camel.chemaxon.processor.ChemAxonVerifyStructureProcessor;
import org.squonk.chemaxon.molecule.ChemTermsEvaluator;

/**
 * These are routes that provide basic property calculation services. The input to the route is a
 * Stream&lt;MoleculeObject&gt; or better, a StreamProvider&lt;MoleculeObject&gt;, or anything that
 * can be converted to these by the Camel TypeConvertor mechanism. The output is a
 * StreamProvider&lt;MoleculeObject&gt; with the (possibly filtered) molecules having the
 * corresponding properties added.
 *
 *
 * @author timbo
 */
public class ChemaxonCalculatorsRouteBuilder extends RouteBuilder {

    public static final String CHEMAXON_STRUCTURE_VERIFY = "direct:structure_verify";
    public static final String CHEMAXON_LOGP = "direct:logp";
    public static final String CHEMAXON_ATOM_COUNT = "direct:atomcount";
    public static final String CHEMAXON_ATOM_BOND_COUNT = "direct:atomcount_bondcount";
    public static final String CHEMAXON_LIPINSKI = "direct:lipinski";
    public static final String CHEMAXON_DRUG_LIKE_FILTER = "direct:drug_like_filter";
    public static final String CHEMAXON_CHEMTERMS = "direct:chemterms";
    public static final String CHEMAXON_AROMATIZE = "direct:aromatize";

    @Override
    public void configure() throws Exception {

        from(CHEMAXON_STRUCTURE_VERIFY)
                .log("CHEMAXON_STRUCTURE_VERIFY starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonVerifyStructureProcessor())
                .log("CHEMAXON_STRUCTURE_VERIFY finished");


        // Simple route that calculates the LogP of molecules
        from(CHEMAXON_LOGP)
                .log("CHEMAXON_LOGP starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .calculate(ChemTermsEvaluator.LOGP, "logP()"))
                .log("CHEMAXON_LOGP finished");

        // Calculate atom count
        from(CHEMAXON_ATOM_COUNT)
                .log("CHEMAXON_ATOM_COUNT starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .calculate(ChemTermsEvaluator.ATOM_COUNT, "atomCount()"))
                .log("CHEMAXON_ATOM_COUNT finished");

        // Calculate LogP, atom count and bond count
        from(CHEMAXON_ATOM_BOND_COUNT)
                .log("CHEMAXON_ATOM_BOND_COUNT starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .calculate(ChemTermsEvaluator.ATOM_COUNT, "atomCount()")
                        .calculate(ChemTermsEvaluator.BOND_COUNT, "bondCount()")
                )
                .log("CHEMAXON_ATOM_BOND_COUNT finished");

        //Calculate the standard Lipinski properties
        from(CHEMAXON_LIPINSKI)
                .log("CHEMAXON_LIPINSKI starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .calculate(ChemTermsEvaluator.MOLECULAR_WEIGHT, "mass()")
                        .calculate(ChemTermsEvaluator.LOGP, "logP()")
                        .calculate(ChemTermsEvaluator.HBOND_DONOR_COUNT, "donorCount()")
                        .calculate(ChemTermsEvaluator.HBOND_ACCEPTOR_COUNT, "acceptorCount()")
                )
                .log("CHEMAXON_LIPINSKI finished");

        // Simple route that exemplifies filtering using a basic drug-like filter
        from(CHEMAXON_DRUG_LIKE_FILTER)
                .log("CHEMAXON_DRUG_LIKE_FILTER starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .filter("mass()<400")
                        .filter("ringCount()>0")
                        .filter("rotatableBondCount()<5")
                        .filter("donorCount()<=5")
                        .filter("acceptorCount()<=10")
                        .filter("logP()<5")
                )
                .log("CHEMAXON_DRUG_LIKE_FILTER finished");

        // Dynamic route that requires the chem terms configuration to be set using the
        // ChemAxonMoleculeProcessor.PROP_EVALUATORS_DEFINTION header property. 
        from(CHEMAXON_CHEMTERMS)
                .log("CHEMAXON_CHEMTERMS starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor())
                .log("CHEMAXON_CHEMTERMS finished");

        // Aromatize the molecules
        from(CHEMAXON_AROMATIZE)
                .log("CHEMAXON_AROMATIZE starting")
                .threads().executorServiceRef(CamelCommonConstants.CUSTOM_THREAD_POOL_NAME)
                .process(new ChemAxonMoleculeProcessor()
                        .standardize("aromatize")
                )
                .log("CHEMAXON_AROMATIZE finished");

    }
}
