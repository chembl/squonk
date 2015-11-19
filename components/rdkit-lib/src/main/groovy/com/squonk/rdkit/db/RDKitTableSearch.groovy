package com.squonk.rdkit.db

import com.im.lac.types.MoleculeObject
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.util.logging.Log
import java.util.stream.Stream
import javax.sql.DataSource

/**
 *
 * @author timbo
 */
@Log
class RDKitTableSearch extends RDKitTable {
    
    RDKitTableSearch(DataSource dataSource, String schema, String baseTable, MolSourceType molSourceType, Map<String,String> extraColumnDefs) {
        super(dataSource, schema, baseTable, molSourceType, extraColumnDefs)
    }
    
    List<MoleculeObject> similaritySearch(String smiles, double threshold, FingerprintType type, Metric metric) {
        return similaritySearch(smiles, threshold, type, metric, null)
    }
    
    List<MoleculeObject> similaritySearch(String smiles, double threshold, FingerprintType type, Metric metric, Integer limit) {
        
        String fn = getSimSearchHelperFunctionName(type, metric)
        String sql1 = 'SET ' + metric.simThresholdProp + ' = ' + threshold
        String sql2 = 'SELECT * FROM ' + fn + '(?)' + (limit == null || limit < 1 ? '' : ' LIMIT ' + limit)
            
        log.fine "SQL: $sql1"
        log.fine "SQL: $sql2"
        return executeSql { db ->
            db.execute(sql1)
            return executeBuildMols(db, sql2, [smiles])
        }
    }

    List<MoleculeObject> substructureSearch(String smarts, boolean chiral) {
        substructureSearch(smarts, chiral, null)
    }
    
    List<MoleculeObject> substructureSearch(String smarts, boolean chiral, Integer limit) {
        String sql1 = 'SET rdkit.do_chiral_sss=' + chiral
        String sql2 = 'SELECT b.*, m.m FROM ' +  baseSchemaPlusTable() + ' b JOIN ' + 
        molfpsSchemaPlusTable() + ' m ON b.id = m.id WHERE m.m @> ?::qmol' + 
        (limit == null || limit < 1 ? '' : ' LIMIT ' + limit)
        log.fine "SQL: $sql1"
        log.fine "SQL: $sql2"
        
        return executeSql { db ->
            db.execute(sql1)
            return executeBuildMols(db, sql2, [smarts])
        }
    }
    
    List<MoleculeObject> exactSearch(String smiles, boolean chiral) {
        String sql1 = 'SET rdkit.do_chiral_sss=' + chiral
        String sql2 = 'SELECT b.*, m.m FROM ' +  baseSchemaPlusTable() + ' b JOIN ' + 
        molfpsSchemaPlusTable() + ' m ON b.id = m.id WHERE m.m = ?::mol'
        log.fine "SQL: $sql1"
        log.fine "SQL: $sql2"
        
        return executeSql { db ->
            db.execute(sql1)
            return executeBuildMols(db, sql2, [smiles])
        }
    }
    
    private List<MoleculeObject> executeBuildMols(Sql db, String sql, List params) {
        List<MoleculeObject> mols = []
        db.eachRow(sql, params) { row ->
            mols << buildMoleculeObject(row)
        }
        return mols
    }
    
    private MoleculeObject buildMoleculeObject(def row) {
        MoleculeObject mo = new MoleculeObject(row.structure, molSourceType == MolSourceType.CTAB ? 'mol' : 'smiles')
        mo.putValue('id', row.id)
        mo.putValue(RDKIT_SMILES, row.m)
        extraColumnDefs.keySet().each { k ->
            def o = row[k]
            if (o != null) {
                mo.putValue(k, o)
            }
        }
        return mo
    }
	
}

