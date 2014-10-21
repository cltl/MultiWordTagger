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
public class MultiWordLexiconSkos extends DefaultHandler {

    public HashMap<String, ArrayList<MultiWordEntry>> entryMap;
    private String value = "";
    MultiWordEntry mwEntry;
    boolean mw = false;

    void init() {
        entryMap = new HashMap<String, ArrayList<MultiWordEntry>>();
        mwEntry = new MultiWordEntry();
    }

    public void parseSkosFile(File file)
    {
    	try {
            init();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            SAXParser parser = factory.newSAXParser();
            InputSource inp = new InputSource (new FileReader(file));
            parser.parse(inp, this);
        } catch (FactoryConfigurationError factoryConfigurationError) {
            factoryConfigurationError.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
        //System.out.println("qName = " + qName);
       value = "";
       if (qName.equalsIgnoreCase("Lemma")) {
           for (int i = 0; i < attributes.getLength(); i++) {
               String name = attributes.getQName(i);
               if (name.equals("writtenForm")) {
                   String attValue = attributes.getValue(i).trim();
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
                       mw = false;
                   }
               }
               else if (name.equals("partOfSpeech")) {
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
            if (qName.equals("LexicalEntry")) {
               if (mw) {
                   String indexKey = mwEntry.getWordlist().get(0);
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
}