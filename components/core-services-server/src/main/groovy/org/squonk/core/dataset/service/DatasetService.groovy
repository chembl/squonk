package org.squonk.core.dataset.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.im.lac.dataset.DataItem
import com.im.lac.dataset.Metadata
import groovy.sql.Sql
import groovy.util.logging.Log

import java.util.function.Consumer
import java.util.function.Function
import javax.sql.DataSource
import org.postgresql.largeobject.LargeObject
import org.postgresql.largeobject.LargeObjectManager

/**
 *
 * @author timbo
 */
@Log
class DatasetService {
    
    static public final String DEFAULT_TABLE_NAME = 'users.datasets'
    
    protected DataSource dataSource
    protected final String tableName;
    protected final ObjectMapper objectMapper;
    
    
    DatasetService(DataSource dataSource) {
        this.dataSource = dataSource
        this.tableName = DEFAULT_TABLE_NAME
        this.objectMapper = new ObjectMapper()
    }  
    
    /** Deletes all data and sets up some test data. FOR TESTING PURPOSES ONLY.
     * 
     */
    protected List<Long> createTestData(String username) {

        def ids = []
        doInTransaction { db ->
            ids << addDataItem(db, username, new DataItem(name: 'test0', ownerUsername: username, metadata:new Metadata(type:Metadata.Type.TEXT,size:1,className:"java.lang.String")), new ByteArrayInputStream('World'.bytes)).id
            ids << addDataItem(db, username, new DataItem(name: 'test1', ownerUsername: username, metadata:new Metadata(type:Metadata.Type.STREAM,size:3,className:"java.lang.String")), new ByteArrayInputStream('''["one", "two", "three"]'''.bytes)).id
            ids << addDataItem(db, username, new DataItem(name: 'test2', ownerUsername: username, metadata:new Metadata(type:Metadata.Type.STREAM,size:4,className:"java.lang.String")), new ByteArrayInputStream('''["red", "yellow", "green", "blue"]'''.bytes)).id
            ids << addDataItem(db, username, new DataItem(name: 'test3', ownerUsername: username, metadata:new Metadata(type:Metadata.Type.STREAM,size:5,className:"java.lang.String")), new ByteArrayInputStream('''["banana", "pineapple", "orange", "apple", "pear"]'''.bytes)).id
        }
        return ids
    }
    
    //    void createTables() {
    //        Sql db = new Sql(dataSource)
    //        try {
    //            db.firstRow('select count(*) from ' + tableName)
    //            log.warning("Table " + tableName + " exsits - will not submit")
    //        } catch (SQLException se) {
    //            createTable(db)
    //        }
    //        db.close()
    //    }
    //        
    //    private void createTable(Sql db) {
    //        log.info("Creating table")
    //        db.execute """\
    //                |CREATE TABLE $tableName (
    //                |  id SERIAL PRIMARY KEY,
    //                |  name TEXT NOT NULL,
    //                |  time_created TIMESTAMP NOT NULL DEFAULT NOW(),
    //                |  last_updated TIMESTAMP NOT NULL DEFAULT NOW(),
    //                |  metadata JSONB,
    //                |  loid BIGINT NOT NULL
    //                |)""".stripMargin()
    //    }
    //        
    //    void dropTables() {
    //        Sql db = new Sql(dataSource.connection)
    //        try {
    //            doDeleteAllLobs(db)
    //            db.execute("DROP TABLE " + tableName)
    //        } catch (Exception ex) {
    //            log.warning("Failed to drop tables")
    //        } finally {
    //            db.close()
    //        }
    //    }
        
    protected void deleteDataForUser(String username) {
        doInTransaction { db ->
            deleteLobsForUser(db, username)
            db.execute('DELETE FROM ' + tableName +
            ' WHERE owner_id IN (SELECT id FROM users.users WHERE username = ?)', 
                [username])
        }
    }
    
    /** for use in testing only */
    protected void deleteLobsForUser(Sql db, String username) {
        db.eachRow("SELECT d.loid FROM " + tableName + " d JOIN users.users u ON u.id = d.owner_id WHERE u.username = ?", [username]) {
            try {
                ((org.postgresql.PGConnection)db.connection).getLargeObjectAPI().delete(it[0])
                println "deleted lob ${it[0]}" 
            } catch (Exception e) {
                println "loid ${it[0]} does not seem to exist"
            }
        }
    }
    
    /** This is the entry point for getting a Sql instance
     */
    public <R> R doInTransactionWithResult(Class<R> type, Function<Sql,R> executable) throws Exception {
        Sql db = new Sql(dataSource.connection)
        try {
            R result
            db.withTransaction {
                result = executable.apply(db)
            }
            return result
        } finally {
            db.close()
        }  
    }
    
    public void doInTransaction(Consumer<Sql> executable) throws Exception {
        Sql db = new Sql(dataSource.connection)
        try {
            db.withTransaction {
                executable.accept(db)
            } 
        } finally {
            db.close()
        }  
    }
    
    private Long fetchUserId(Sql db, String username) {
        def row = db.firstRow("SELECT id FROM users.users WHERE username = ?".toString(), [username])
        if (row == null) {
            throw new IllegalStateException("Username $username not present")
        }
        return row[0]
    }
    
    DataItem addDataItem(final String username, final DataItem data, final InputStream is) throws Exception {
        return doInTransactionWithResult(DataItem.class) { 
            addDataItem(it, username, data, is) 
        }
    }
    
    /**
     * Add a new data item with content.
     */
    DataItem addDataItem(final Sql db, final String username, final DataItem data, final InputStream is) throws Exception {
        if (data.getOwnerUsername() != null && data.getOwnerUsername() != username) {
            throw new IllegalStateException("Username does not match that of the DataItem")
        }
        Long userid = fetchUserId(db, username)
        if (data.getOwnerId() != null && userid != data.getOwnerId()) {
            throw new IllegalStateException("Username does not correspond to userid")
        }
        
        log.info("Adding data item for $data")
        
        Long loid = createLargeObject(db, is)
        String metaJson = marshalMetadata(data.metadata);
        def gen = db.executeInsert("""\
            |INSERT INTO $tableName (name, owner_id, metadata, loid)
            |  VALUES (?,?,?::jsonb,?)""".stripMargin(), [data.name, userid, metaJson, loid]) 
        Long id = gen[0][0]
            
        log.info("added data item with id $id using loid $loid")
        return getDataItem(db, username, id)
    }
    
    DataItem updateDataItem(final String username, final DataItem data) throws Exception {
        return doInTransactionWithResult(DataItem.class) { updateDataItem(it, username, data) }
    }
    
    DataItem updateDataItem(final Sql db, final String username, final DataItem data) throws Exception {     
        Long id = data.id
        String metaJson = marshalMetadata(data.metadata)
        db.executeUpdate("""\
            |UPDATE $tableName set name = ?, metadata = ?::jsonb, last_updated = NOW()
            |  WHERE id = ?""".stripMargin(), [data.name, metaJson, id]) 

        log.info("Updated data item with id $id")
        return getDataItem(db, username, id)
    }
    
    DataItem updateDataItem(final String username, final DataItem data, final InputStream is) throws Exception {
        return doInTransactionWithResult(DataItem.class) { updateDataItem(it, username, data, is, ) }
    }
    
    DataItem updateDataItem(final Sql db, final String username, final DataItem data, final InputStream is) throws Exception {
        Long id = data.id
        deleteLargeObject(db, data.loid)
        Long loid = createLargeObject(db, is)
        String metaJson = marshalMetadata(data.metadata);
        db.executeUpdate("""\
            |UPDATE $tableName set loid = ?, last_updated = NOW(), metadata = ?::jsonb
            |  WHERE id = ?""".stripMargin(), [loid, metaJson, id]) 
            
        log.info("Created data item with id $id using loid $loid")
        return getDataItem(db, username, data.id)
    }
    
    DataItem getDataItem(final String username, final Long id) throws Exception { 
        return doInTransactionWithResult(DataItem.class) { getDataItem(it, username, id) }
    }
    
    DataItem getDataItem(final Sql db, final String username, final Long id) {
        log.fine("getDataItem($id)")
        def row = db.firstRow('SELECT d.id, d.name, d.time_created, d.last_updated, d.metadata::text, d.loid, u.id AS owner_id, u.username FROM ' 
            + tableName + ' d JOIN users.users u ON u.id = d.owner_id WHERE d.id = ? AND u.username = ?', [id, username])
        if (!row) {
            throw new IllegalArgumentException("Item with ID $id not found")
        }
        DataItem data = buildDataItem(row)
        return data
    }
    
    List<DataItem> getDataItems(final String username) throws Exception {
        return doInTransactionWithResult(List.class) { getDataItems(it, username) }
    }
    
    List<DataItem> getDataItems(final Sql db, final String username) throws Exception {
        log.fine("getDataItems()")
        List<DataItem> items = []
        db.eachRow('SELECT d.id, d.name, d.time_created, d.last_updated, d.metadata::text, d.loid, u.id AS owner_id, u.username FROM ' 
            + tableName + ' d JOIN users.users u ON u.id = d.owner_id WHERE u.username = ? ORDER BY d.id', [username]) { row ->
            items << buildDataItem(row)
        }
        log.fine("found ${items.size()} items")
        return items
    }
    
    private DataItem buildDataItem(def row) {
        
        DataItem data = new DataItem(
            row.id, row.name, row.owner_id, row.username,
            unmarshalMetadata(row.metadata),
            row.time_created, row.last_updated, row.loid)
        return data
    }
    
    void deleteDataItem(final DataItem data) throws Exception {
        doInTransaction() { deleteDataItem(it, data) }
    }
    
    /** Delete data item within a new transaction
     */
    void deleteDataItem(final Sql db, final DataItem data) throws Exception {
        deleteLargeObject(db, data.loid)
        db.executeUpdate("DELETE FROM " + tableName + " WHERE id = ?", [data.id])    
    }
    
    private long createLargeObject(final Sql db, final InputStream is) {
        
        // Get the Large Object Manager to perform operations with
        LargeObjectManager lobj = ((org.postgresql.PGConnection)db.connection).getLargeObjectAPI();
        // Create a new large object
        long oid = lobj.createLO(LargeObjectManager.READ | LargeObjectManager.WRITE)
        // Open the large object for writing
        LargeObject obj = lobj.open(oid, LargeObjectManager.WRITE)
        try {
            // Copy the data from the stream to the large object
            byte[] buf = new byte[2048]
            int s, tl = 0;
            while ((s = is.read(buf, 0, 2048)) > 0) {
                obj.write(buf, 0, s)
                tl += s
            }
        } finally {
            obj.close()
        }
        log.info("Created large object with id $oid")
        return oid
    }
    
    void deleteLargeObject(final Long loid) {
        doInTransaction() { deleteLargeObject(it, loid) }
    }
    
    void deleteLargeObject(final Sql db, final Long loid) {
        
        // Get the Large Object Manager to perform operations with
        final LargeObjectManager lobj = ((org.postgresql.PGConnection)db.connection).getLargeObjectAPI();
        lobj.delete(loid)
    }
    
    //    InputStream createLargeObjectReader(final long loid) {
    //        return doInTransactionWithResult(List.class) { createLargeObjectReader(it, loid) }
    //    }
    
    /**
     * Create an InputStream that reads the large object. The connection must be 
     * in a transaction (autoCommit = false) and the InputStream MUST be closed when 
     * finished with
     */
    InputStream createLargeObjectReader(final Sql db, final long loid) throws Exception {
        // Get the Large Object Manager to perform operations with
        LargeObjectManager lobj = ((org.postgresql.PGConnection)db.connection).getLargeObjectAPI()
        LargeObject obj = lobj.open(loid, LargeObjectManager.READ)
        return obj.getInputStream();        
    }
    
    private String marshalMetadata(Metadata meta) {
        return objectMapper.writeValueAsString(meta);
    }
    
    private Metadata unmarshalMetadata(String json) {
        if (json != null) {
            return objectMapper.readValue(json, Metadata.class);
        } else {
            return new Metadata();
        }
    }
    
}