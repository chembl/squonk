from com.im.lac.types import MoleculeObjectIterable
from com.im.lac.util import CloseableMoleculeObjectQueue
from com.im.lac.types import MoleculeObject
from java import lang
import sys
from java.util import ArrayList
from java.lang import Thread, InterruptedException

from java.lang import Class
lang.System.loadLibrary('GraphMolWrap')
# Pull it in as a stream of string
from org.RDKit import *

from mol_parsing.rdkit_parse import parse_mol_obj
# this gets the body converted to a MoleculeObjectIterable
def read_in():
    counter = 0
    while mols.hasNext():
        counter +=1
        molobj = mols.next()
        # Now get the molecule
        rdmol = parse_mol_obj(molobj)
        if not rdmol:
            continue 
        # Put this representation to the function
        molobj.putRepresentation("rdkit.mol", rdmol)
        molobj.putValue("me", counter)
        # Add to the queue
        out_mols_here.add(molobj)
    # Close the queue to stop the blocking 
    out_mols_here.close()

class ObjReadThread(Thread):
    def run(self):
        read_in()
        self.stop()

# Get the prvevious body and set the next one
mols = request.getBody(MoleculeObjectIterable)
out_mols_here = CloseableMoleculeObjectQueue(40)
request.setBody(out_mols_here)
#read_in()
my_thread = ObjReadThread()
my_thread.start()
#import threading
#t = threading.Thread(target=read_in,)
#t.start()
