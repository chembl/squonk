package org.squonk.openchemlib.molecule;

import com.actelion.research.chem.MolfileParser;
import com.actelion.research.chem.SmilesParser;
import com.actelion.research.chem.StereoMolecule;
import com.im.lac.types.MoleculeObject;

import java.util.logging.Logger;

/**
 * Created by timbo on 05/04/16.
 */
public class OCLMoleculeUtils {

    private static final Logger LOG = Logger.getLogger(OCLMoleculeUtils.class.getName());
    public static final SmilesParser SMILES_PARSER = new SmilesParser();
    public static final MolfileParser MOLFILE_PARSER = new MolfileParser();


    public static StereoMolecule importString(String molecule, String format) {
        if (format != null)
            try {
                if (format.toLowerCase().equals("smiles") || format.toLowerCase().startsWith("smiles:")) {
                    return importSmiles(molecule);
                } else if (format.toLowerCase().equals("mol") || format.toLowerCase().startsWith("mol:")) {
                    return importMolfile(molecule);
                }
            } catch (Exception ex) {
                LOG.info("Failed to parse molecule " + ex.getMessage());
                return null;
            }
        try {
            return importMolfile(molecule);
        } catch (Exception ex1) {
            try {
                return importSmiles(molecule);
            } catch (Exception ex2) {
                LOG.info("Failed to parse molecule as mol or smiles");
                return null;
            }
        }
    }

    public static StereoMolecule importSmiles(String smiles) throws Exception {
        StereoMolecule mol = new StereoMolecule();
        SMILES_PARSER.parse(mol, smiles);
        return mol;
    }

    public static StereoMolecule importMolfile(String molfile) throws Exception {
        StereoMolecule mol = new StereoMolecule();
        MOLFILE_PARSER.parse(mol, molfile);
        return mol;
    }

    public static StereoMolecule fetchMolecule(MoleculeObject mo, boolean store) {
        StereoMolecule mol = importString(mo.getSource(), mo.getFormat());
        if (store) {
            mo.putRepresentation(StereoMolecule.class.getName(), mol);
        }
        return mol;
    }

}