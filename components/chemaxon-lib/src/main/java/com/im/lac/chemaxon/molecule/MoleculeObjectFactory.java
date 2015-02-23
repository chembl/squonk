/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.im.lac.chemaxon.molecule;

import chemaxon.marvin.io.MPropHandler;
import chemaxon.marvin.io.MRecord;
import chemaxon.struc.MProp;
import chemaxon.struc.MPropertyContainer;
import com.im.lac.types.MoleculeObject;
import com.im.lac.types.MoleculeObjectIterable;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Iterator;

/**
 *
 * @author timbo
 */
class MoleculeObjectFactory implements MoleculeObjectIterable, Iterator<MoleculeObject>, Closeable {
    InputStream is;
    MRecordIterator iter;

    MoleculeObjectFactory(InputStream is) throws IOException {
        this.is = is;
        this.iter = new MRecordIterator(is);
    }

    @Override
    public boolean hasNext() {
        return iter.hasNext();
    }

    @Override
    public MoleculeObject next() {
        MRecord rec = iter.next();
        String mol = rec.getString();
        String format = rec.getInputFormat();
        if (format == null) {
            format = iter.getRecordReader().getRecognizedFormat();
        }
        if (format != null) {
            // this is a BIG HACK because Marvin does not always give you the right answer
            // TODO - work out something better
            if (format.startsWith("smiles")) {
                format = "smiles";
            } else if (format.startsWith("smarts")) {
                format = "smarts";
            } else if (format.startsWith("mol")) {
                format = "mol";
            } else if (format.startsWith("sdf")) {
                format = "mol"; // yes, this is supposed to be mol
            } else if (format.startsWith("inchi")) {
                format = "inchi";
            } else if (format.startsWith("mrv")) {
                format = "mrv";
            } else {
                format = null;
            }
        }
        String name = rec.getMoleculeName();
        MoleculeObject mo = new MoleculeObject(mol, format);
        if (name != null && name.length() > 0) {
            mo.putValue("name", name);
        }
        MPropertyContainer pc = rec.getPropertyContainer();
        String[] keys = pc.getKeys();
        for (int x = 0; x < keys.length; x++) {
            String key = keys[x];
            MProp prop = pc.get(key);
            Serializable ser = null;
            Object o = prop.getPropValue();
            if (o instanceof Serializable) {
                ser = (Serializable) o;
            } else {
                ser = MPropHandler.convertToString(prop, format);
            }
            mo.putValue(key, ser);
        }
        return mo;
    }

    @Override
    public Iterator<MoleculeObject> iterator() {
        return this;
    }

    @Override
    public void close() throws IOException {
        iter.close();
    }
    
}