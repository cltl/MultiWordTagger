package eu.kyotoproject.multiwordtagger;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Piek Vossen
 * Date: aug-2010
 * Time: 6:34:44
 * To change this template use File | Settings | File Templates.
 * This file is part of KafMultiWordtagger.

 KafMultiWordtagger is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 KafMultiWordtagger is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with KafMultiWordtagger.  If not, see <http://www.gnu.org/licenses/>.
 */
public class MultiWordLexiconWnLmf extends DefaultHandler {

    boolean debug;
    public HashMap<String, ArrayList<MultiWordEntry>> entryMap;
    private String value = "";
    MultiWordEntry mwEntry;
    boolean mw = false;
    FileOutputStream fos;
    boolean isGeneric;


    public MultiWordLexiconWnLmf () {
        init();
    }

    void init() {
        entryMap = new HashMap<String, ArrayList<MultiWordEntry>>();
        mwEntry = new MultiWordEntry();
        isGeneric = true;
        if (debug) {
            try {
                fos = new FileOutputStream ("mw.log");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void dumpLex () {
        Set keySet = entryMap.keySet();
        Iterator keys = keySet.iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            ArrayList<MultiWordEntry> mws = entryMap.get(key);
            System.out.println("key = " + key+":"+mws.size());
        }
    }
    public void parseWnLmfFile(File file, boolean generic)
    {
    	try {
            isGeneric = generic;
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            SAXParser parser = factory.newSAXParser();
            InputSource inp = new InputSource (new FileReader(file));
            //inp.setEncoding("UTF-8");
            parser.parse(inp, this);
            if (debug) {
                fos.close();
            }
           // System.out.println("entryMap = " + entryMap.size());
            //dumpLex();

        } catch (FactoryConfigurationError factoryConfigurationError) {
            factoryConfigurationError.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
            if (debug) {
                try {
                     fos.write(("last mw = " + mwEntry.getMultiWordLemma()+"\n").getBytes());
                } catch (IOException e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            if (debug) {
                try {
                     fos.write(("file.getAbsolutePath() = " + file.getAbsolutePath()+"\n").getBytes());
                } catch (IOException e1) {
                    e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
        if (debug) {
            try {
                 fos.write(("last mw = " + mwEntry.getMultiWordLemma()+"\n").getBytes());
            } catch (IOException e1) {
                e1.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }


/*        <LexicalEntry>
<Lemma writtenForm="crowberry family" partOfSpeech="n"/>
<Sense id="eng-30-12751402-n_1" synset="eng-30-12751402-n"/>
</LexicalEntry>
     */
        public void startElement(String uri, String localName,
                             String qName, Attributes attributes)
            throws SAXException {
      //  System.out.println("qName = " + qName);
       value = "";
       if (qName.equalsIgnoreCase("Lemma")) {
           for (int i = 0; i < attributes.getLength(); i++) {
               String name = attributes.getQName(i);
               if (name.equalsIgnoreCase("writtenForm")) {
                   String attValue = attributes.getValue(i).trim().toLowerCase();
                   String [] elements = attValue.split(" ");
                   if (elements.length>1) {
                       mw = true;
                       mwEntry = new MultiWordEntry();
                       for (int j = 0; j < elements.length; j++) {
                           String element = elements[j];
                           mwEntry.addWordlist(element);
                       }
                   }
                   else {
                       elements = attValue.split("_");
                       if (elements.length>1) {
                           mw = true;
                           mwEntry = new MultiWordEntry();
                           for (int j = 0; j < elements.length; j++) {
                               String element = elements[j];
                               mwEntry.addWordlist(element);
                           }
                       }
                       else {
                           mw = false;
                       }
                   }
               }
               else if (name.equalsIgnoreCase("partOfSpeech")) {
                   if (mw) {
                       mwEntry.setPos(attributes.getValue(i).trim());
                   }
               }
           }
       }
       else if (qName.equalsIgnoreCase("Sense")) {
           if (mw) {
               Sense sense = new Sense();
               for (int i = 0; i < attributes.getLength(); i++) {
                   String name = attributes.getQName(i);
                   if (name.equalsIgnoreCase("id")) {
                       sense.setSense_key(attributes.getValue(i).trim());
                   }
                   else if (name.equalsIgnoreCase("synset")) {
                       sense.setSynsetId(attributes.getValue(i).trim());
                   }
               }
               mwEntry.addSenseList(sense);
           }
        }
        else {
           ///////
        }
    }//--startElement


    public void endElement(String uri, String localName, String qName)
            throws SAXException {
            if (qName.equalsIgnoreCase("LexicalEntry")) {
               if (mw) {
                   mwEntry.setGeneric(isGeneric);
                   String indexKey = mwEntry.getWordlist().get(0);
                   //System.out.println("indexKey = " + indexKey);
                   String str = "";
                   for (int l = 0; l < mwEntry.getWordlist().size(); l++) {
                       String s = mwEntry.getWordlist().get(l);
                       if (l>0) {
                           str += "_"+s;
                       }
                       else {
                           str = s;
                       }
                   }
                   str += "\n";
                   if (debug) {
                       try {
                           fos.write(str.getBytes());
                       } catch (IOException e) {
                           e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                       }
                   }

                   if (entryMap.containsKey(indexKey)) {
                       ArrayList<MultiWordEntry> mwEntries = entryMap.get(indexKey);
                       mwEntries.add(mwEntry);
                       entryMap.put(indexKey, mwEntries);
                   }
                   else {
                       ArrayList<MultiWordEntry> mwEntries = new ArrayList<MultiWordEntry>();
                       mwEntries.add(mwEntry);
                       entryMap.put(indexKey, mwEntries);
                   }
               }
               else {
                  // System.out.println("mw = " + mw);
               }
            }
            else {
                /////
            }
    }
    /*
    @TODO
    - if multiword is inflected and lemmas are not?
    - index beginning of multiword or index all?
     */
    /*
    <LexicalEntry>
<Lemma writtenForm="crowberry family" partOfSpeech="n"/>
<Sense id="eng-30-12751402-n_1" synset="eng-30-12751402-n"/>
</LexicalEntry>
     */

    static public void main (String args[]) {
        String lexPath = args[0];
        MultiWordLexiconWnLmf parser = new MultiWordLexiconWnLmf();
        parser.setDebug(true);
        parser.init();
        parser.parseWnLmfFile(new File(lexPath), true);
     //   parser.dumpLex();
    }
}
