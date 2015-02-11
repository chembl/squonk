package com.im.lac.camel.dataformat;

import com.im.lac.types.MoleculeObject;
import com.im.lac.types.MoleculeObjectIterable;
import com.im.lac.util.CloseableMoleculeObjectQueue;
import com.im.lac.util.CloseableQueue;
import java.io.Closeable;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.camel.Exchange;
import org.apache.camel.spi.DataFormat;
import org.apache.camel.util.IOHelper;

/**
 * WARNING - this class is incomplete. Do not use.
 *
 * TODO - move this out of the chemaxon-camel module as it has nothing to do
 * with ChemAxon, but currently doesn't have a better place to live.
 *
 * @author timbo
 */
public class MoleculeObjectDataFormat implements DataFormat {

    private static final Logger LOG = Logger.getLogger(MoleculeObjectDataFormat.class.getName());

    private int marshalCount = 0;

    public int getMarshalCount() {
        return marshalCount;
    }

    private int unmarshalCount = 0;

    public int getUnmarshalCount() {
        return unmarshalCount;
    }

    @Override
    public void marshal(Exchange exchange, Object o, OutputStream out) throws IOException {
        marshal(o, out);
    }

    public void marshal(Object o, OutputStream out) throws IOException {

        Iterator<MoleculeObject> mols = null;
        if (o instanceof MoleculeObjectIterable) {
            mols = ((MoleculeObjectIterable) o).iterator();
        } else if (o instanceof Iterator) {
            mols = (Iterator<MoleculeObject>) o;
        } else if (o instanceof Iterable) {
            mols = ((Iterable) o).iterator();
        } else if (o instanceof MoleculeObject) {
            mols = Collections.singletonList((MoleculeObject) o).iterator();
        } else {
            throw new IllegalArgumentException("Bad format. Can't handle " + o.getClass().getName());
        }

        ObjectOutputStream oos = new ObjectOutputStream(out);
        try {
            while (mols.hasNext()) {
                MoleculeObject mo = mols.next();
                oos.writeObject(mo);
                marshalCount++;
            }
        } finally {
            IOHelper.close(oos);
            if (mols instanceof Closeable) {
                try {
                    ((Closeable) mols).close();
                } catch (IOException ioe) {
                    LOG.log(Level.WARNING, "Failed to close iterator", ioe);
                }
            }
        }
    }

    @Override
    public Object unmarshal(Exchange exchange, InputStream is) throws IOException {
        return unmarshal(is);
    }

    public Object unmarshal(InputStream is) throws IOException {

        final ObjectInputStream ois = new ObjectInputStream(is);

        final CloseableQueue<MoleculeObject> q = new CloseableMoleculeObjectQueue(50);
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    MoleculeObject mo = (MoleculeObject) ois.readObject();
                    q.add(mo);
                    unmarshalCount++;
                } catch (EOFException e) {
                    break;
                } catch (IOException | ClassNotFoundException ex) {
                    throw new RuntimeException("Error during deserialization of MoleculeObjects", ex);
                }
            }

            IOHelper.close(is);
            q.close();
        });
        t.start();

        return q;
    }

}
