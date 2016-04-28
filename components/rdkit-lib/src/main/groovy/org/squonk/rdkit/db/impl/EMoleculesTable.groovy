package org.squonk.rdkit.db.impl

import org.squonk.rdkit.db.FingerprintType
import org.squonk.rdkit.db.MolSourceType
import org.squonk.rdkit.db.RDKitTable

/**
 * Created by timbo on 16/12/2015.
 */
class EMoleculesTable extends RDKitTable {

    EMoleculesTable(String schema, String baseTableName, MolSourceType molSourceType) {
        super(schema, baseTableName, molSourceType, [
                FingerprintType.RDKIT,
                FingerprintType.MORGAN_CONNECTIVITY_2,
                FingerprintType.MORGAN_FEATURE_2])
        addColumn("version_id", "INTEGER", "INTEGER NOT NULL")
        addColumn("parent_id", "INTEGER", "INTEGER NOT NULL")
    }
}