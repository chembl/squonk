package org.squonk.dataset

import org.squonk.types.BasicObject
import org.squonk.types.MoleculeObject
import spock.lang.Specification

/**
 * Created by timbo on 22/02/17.
 */
class ThinDatasetWrapperSpec extends Specification {

    static Dataset fat = new Dataset(MoleculeObject.class, [
            new MoleculeObject("C", "smiles", [a:"x", b:1]),
            new MoleculeObject("CC", "smiles", [a:"y", b:2]),
            new MoleculeObject("CCC", "smiles", [a:"z", b:3]),
    ])

    static Dataset thinResp = new Dataset(BasicObject.class, [
            new BasicObject(fat.items[0].uuid, [c:"x"]),
            new BasicObject(fat.items[1].uuid, [c:"y"]),
            new BasicObject(fat.items[2].uuid, [c:"z"]),
    ])


    void "test prepare thin no mappings"() {
        ThinDatasetWrapper wrapper = new ThinDatasetWrapper(MoleculeObject.class, false, true)

        when:
        Dataset thin = wrapper.prepareInput(fat)
        def items = thin.items

        then:
        thin.getType() == MoleculeObject.class
        items.size() == 3
        items.each {
            it.values.size() == 0
        }
    }

    void "test respond thin no mappings"() {
        ThinDatasetWrapper wrapper = new ThinDatasetWrapper(MoleculeObject.class, false, true)

        when:
        Dataset thin = wrapper.prepareInput(fat)
        thin.items // force stream to be executed
        Dataset result = wrapper.generateOutput(thinResp)

        def items = result.items


        then:
        result.getType() == MoleculeObject.class
        items.size() == 3
        items.each {
            it.values.size() == 3
        }
    }

    void "test prepare thin with mappings fieldName only"() {
        ThinDatasetWrapper wrapper = new ThinDatasetWrapper(MoleculeObject.class, false, true, [new ThinFieldDescriptor("a", null)] as ThinFieldDescriptor[], [:])

        when:
        Dataset thin = wrapper.prepareInput(fat)
        def items = thin.items

        then:
        thin.getType() == MoleculeObject.class
        items.size() == 3
        items.each {
            it.values.size() == 1
            it.values["a"]
        }
    }

    void "test prepare thin with mappings optionName only"() {
        ThinDatasetWrapper wrapper = new ThinDatasetWrapper(MoleculeObject.class, false, true, [new ThinFieldDescriptor(null, "zzz")] as ThinFieldDescriptor[], [zzz:"a"])

        when:
        Dataset thin = wrapper.prepareInput(fat)
        def items = thin.items

        then:
        thin.getType() == MoleculeObject.class
        items.size() == 3
        items.each {
            it.values.size() == 1
            it.values["a"]
        }
    }

    void "test prepare thin with mappings fieldName and optionName"() {
        ThinDatasetWrapper wrapper = new ThinDatasetWrapper(MoleculeObject.class, false, true, [new ThinFieldDescriptor("yyy", "zzz")] as ThinFieldDescriptor[], [zzz:"a"])

        when:
        Dataset thin = wrapper.prepareInput(fat)
        def items = thin.items

        then:
        thin.getType() == MoleculeObject.class
        items.size() == 3
        items.each {
            it.values.size() == 1
            it.values["yyy"]
        }
    }


}
