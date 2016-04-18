package org.squonk.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.postgresql.ds.PGPoolingDataSource;
import org.squonk.core.CommonConstants;
import org.squonk.util.IOUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author timbo
 */
public class Utils {

    private static final Logger LOG = Logger.getLogger(Utils.class.getName());

    private static ObjectMapper mapper = new ObjectMapper();

    public static DataSource createDataSource() {
        DataSource ds = createDataSource(null, null, null, null, null);
        return ds;
    }

    public static DataSource createDataSource(String server, String port, String dbName, String user, String password) {

        String s = (server != null ? server : IOUtils.getConfiguration("SQUONK_DB_SERVER", "localhost"));
        String po = (port != null ? port : IOUtils.getConfiguration("SQUONK_DB_PORT", "5432"));
        String d = "squonk";
        String u = "squonk";
        String pw = (password != null ? password :  IOUtils.getConfiguration("POSTGRES_SQUONK_PASS", "squonk"));

        PGPoolingDataSource ds = new PGPoolingDataSource();
        ds.setServerName(s);
        ds.setPortNumber(new Integer(po));
        ds.setDatabaseName(d);
        ds.setUser(u);
        ds.setPassword(pw);

        LOG.log(Level.INFO, "Using datasource for server {0}@{1}:{2}/{3}", new Object[]{u, s, po, d});
        //LOG.log(Level.INFO, "Connecting as {0}/{1}", new Object[]{u, pw});

        return ds;
    }

    public static String toJson(Object o) throws JsonProcessingException {
        return mapper.writeValueAsString(o);
    }

    public static <T> T fromJson(InputStream is, Class<T> type) throws IOException {
        return mapper.readValue(is, type);
    }

    public static <T> T fromJson(String s, Class<T> type) throws IOException {
        return mapper.readValue(s, type);
    }

    public static String fetchUsername(Exchange exchange) {
        String username = exchange.getIn().getHeader(CommonConstants.HEADER_SQUONK_USERNAME, String.class);
        if (username == null) {
            throw new IllegalStateException("Validated username not specified");
        }
        return username;
    }

}
