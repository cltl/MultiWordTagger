package eu.kyotoproject.multiwordtagger;


import eu.kyotoproject.kaf.*;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

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
public class MultiTagger {
    private int nGWN = 0;
    private int nDWN = 0;
    private int nTerms = 0;
    private boolean NAF = false;
    private boolean debug;
    private Config config;
    private MultiWordLexiconWnLmf lex;
    public FileOutputStream fos = null;

    public MultiTagger () {
        debug = false;
        config = null;
        lex = new MultiWordLexiconWnLmf();
    }

    public boolean isNAF() {
        return NAF;
    }

    public void setNAF(boolean NAF) {
        this.NAF = NAF;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return debug;
    }

    public int getnGWN() {
        return nGWN;
    }

    public int getnDWN() {
        return nDWN;
    }

    public int getnTerms() {
        return nTerms;
    }

    public void setnTerms(int nTerms) {
        this.nTerms = nTerms;
    }
/*
   Solution 2 seems to me drastic but I see the benefits for other modules that now do not have to be changed. Let me reconsider the changes for 2.
taking the following example:

       <term tid="t74" type="open" lemma="important" pos="G">
           <span>
               <target id="w83"/>
           </span>
              </term>
       <term tid="t75" type="open" lemma="development" pos="N">
           <span>
               <target id="w84"/>
           </span>
              </term>

This will then be changed to:

       <term tid="t75mw" type="open" lemma=�important_development" pos=�N">
           <span>
               <target id="w83"/>
               <target id="w84"/>
           </span>
                      <component id="t74" lemma=�important� pos=�G�/>
           <component id=�t75" lemma=�development� pos=�N"/></term>

              </term>

This presentation is compatible with the way compounds are now represented in the Dutch KAF and UKB will assign the synset ids to the multiword and to the components.
Next, we need to go to all chunks and deps in which t74 and t75 occur:

<deps>
       <dep from="t75" to="t80" rfunc="subj"/>
       <dep from="t75" to="t74" rfunc="mod"/>
</deps>

<chunks>
       <chunk cid="c63" head="t75" phrase="NP">
           <span>
               <target id="t74"/>
           </span>
           <span>
               <target id="t75"/>
           </span>
       </chunk>
</chunks>

and rewrite these to:

<deps>
       <dep from="t75mw" to="t80" rfunc="subj"/>
       <dep from="t75" to="t74" rfunc="mod"/>
</deps>

<chunks>
       <chunk cid="c63" head="t75mw" phrase="NP">
           <span>
               <target id="t75mw"/>
           </span>
       </chunk>
</chunks>
    */

    int findFirstNumeric (String id) {
        int idx = -1;
        final String num = "0123456789";
        for (int i = 0; i < id.toCharArray().length; i++) {
            char c = id.toCharArray()[i];
            if (num.indexOf(c)>-1) {
                return i;
            }
        }
        return idx;
    }

   /*
        static final String layer = "terms";
    static final String name = "vua-predicate-matrix-tagger";
    static final String version = "1.0";
     */

    public void tagMultiWords (String layer, String name, String version, InputStream in, OutputStream out) throws IOException {
        nDWN = 0;
        nGWN = 0;
        nTerms = 0;
        KafSaxParser parser = new KafSaxParser();

        Calendar date = Calendar.getInstance();
        String strBeginDate = eu.kyotoproject.util.DateUtil.createTimestamp();
        String strEndDate = null;

        parser.parseFile(in);

        strEndDate = eu.kyotoproject.util.DateUtil.createTimestamp();


        String host = "";
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        LP lp = new LP(name,version, strBeginDate, strBeginDate, strEndDate, host);
        if (NAF) {
            parser.writeNafToStream(out);
        }
        else {
            parser.writeKafToStream(out, false);
        }
    }

    public void tagMultiWords (InputStream in, OutputStream out) throws IOException {
        nDWN = 0;
        nGWN = 0;
        nTerms = 0;
        String version = "0.1";
        String name = "MultiwordTagger";
        String layer = "terms";
        KafSaxParser parser = new KafSaxParser();
       // Calendar date = Calendar.getInstance();
        String strBeginDate = eu.kyotoproject.util.DateUtil.createTimestamp();
        String strEndDate = null;
/*        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date.setTimeInMillis(System.currentTimeMillis());
        if (date != null) {
            strBeginDate = sdf.format(date.getTime());
        }*/
        parser.parseFile(in);
        doTagging(parser);
        strEndDate = eu.kyotoproject.util.DateUtil.createTimestamp();
        String host = "";
        try {
            host = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        LP lp = new LP(name,version, strBeginDate, strBeginDate, strEndDate, host);
        parser.getKafMetaData().addLayer(layer, lp);
        if (NAF) {
            parser.writeNafToStream(out);
        }
        else {
            parser.writeKafToStream(out, false);
        }
    }

    public void doTagging (KafSaxParser parser) throws IOException {
        //parser.parseFile(in, "UTF-8");
        ArrayList<MultiwordPattern> lgPatterns = new ArrayList<MultiwordPattern> ();
        //System.out.println("config.patterns.size() = " + config.patterns.size());
        //System.out.println("parser.getLanguage().toLowerCase() = " + parser.getLanguage().toLowerCase());
        if (config.patterns.containsKey(parser.getLanguage().toLowerCase())) {
            lgPatterns = config.patterns.get(parser.getLanguage().toLowerCase());
        }
        ArrayList<KafTerm> multiWordTermList = new ArrayList<KafTerm>();
        ArrayList<KafTerm> kafTermList = parser.kafTermList;
        if ((parser.kafTermList == null) || (parser.kafTermList.size()==0)) {
            return;
        }
        nTerms += parser.kafTermList.size();
        if ((parser.kafWordFormList == null) || (parser.kafWordFormList.size()==0)) {
            return;
        }
        // iterate over the kafTerms....except for the last term since it cannot be matched anymore
        for (int i = 0; i < kafTermList.size()-1; i++) {
            KafTerm kafTerm = kafTermList.get(i);
            KafTerm nextTerm = kafTermList.get(i + 1);
            ArrayList<MultiWordEntry> mwEntry = getMwEntries(lex, kafTerm, parser);
            if (mwEntry != null) {
                //System.out.println("kafTerm = " + kafTerm);
                /// We got the list of all mw expressions starting with this word
                for (int j = 0; j < mwEntry.size(); j++) {
                    MultiWordEntry multiWordEntry = mwEntry.get(j);
                    //// check if we have enough terms left to match against
                    if ((i + multiWordEntry.getWordlist().size() <= kafTermList.size())) {
                        ArrayList<String> newSpans = new ArrayList<String>();
                        ArrayList<TermComponent> components = new ArrayList<TermComponent>();
                        multiWordTermList = new ArrayList<KafTerm>();
                        ///// we get the second word from the multiword entry
                        ///// we use this to get an early break if the second word does not match
                        ///// this way we gain speed!!!! usually two is enough to exclude or completely match
                        String word = multiWordEntry.getWordlist().get(1).toLowerCase();
                        if (!matchWord(word, nextTerm, parser)) {
                            continue;
                        }
                        else {
                              if (fos!=null) fos.write(("TWO WORD MATCH FOR:"+kafTerm.getLemma()+"_"+word+"\n").getBytes());
                        }

                        String termId = "t.mw";
                        int numeric = findFirstNumeric(kafTerm.getTid());
                        if (numeric>-1) {
                            termId+= kafTerm.getTid().substring(numeric);
                        }


                        /// Now we know the first and second term matched with the multiWordEntry
                        TermComponent comp = new TermComponent();
                        /// add the first element
                        comp.setLemma(kafTerm.getLemma());
                        comp.setId(termId+".1");
                        //comp.setId(kafTerm.getTid());
                        comp.setPos(kafTerm.getPos());
                        components.add(comp);
                        multiWordTermList.add(kafTerm);
                        for (int s = 0; s < kafTerm.getSpans().size(); s++) {
                            String span = kafTerm.getSpans().get(s);
                            newSpans.add(span);
                        }
                        /// add the second element
                        comp = new TermComponent();
                        comp.setLemma(nextTerm.getLemma());
                        comp.setId(termId+".2");
                       // comp.setId(nextTerm.getTid());
                        comp.setPos(nextTerm.getPos());
                        components.add(comp);
                        multiWordTermList.add(nextTerm);
                        for (int s = 0; s < nextTerm.getSpans().size(); s++) {
                            String span = nextTerm.getSpans().get(s);
                            newSpans.add(span);
                        }
                        ///// The second word did match so we now check any other words if there are any.....
                        boolean match = true;
                        /// check if there enough terms left to match against the remainder...
                        /// iterate over the remaining multiword elements, skipping the first which was already matched
                        for (int k = 2; k < multiWordEntry.getWordlist().size(); k++) {
                            if (fos!=null) fos.write(("CONTINUE MATCHING:"+k+"\n").getBytes());
                            word = multiWordEntry.getWordlist().get(k).toLowerCase();
                            nextTerm = kafTermList.get(i + k);


                            String str = "";
                            for (int l = 0; l < multiWordEntry.getWordlist().size(); l++) {
                                String s = multiWordEntry.getWordlist().get(l);
                                if (l==0) {
                                    str = s;
                                }
                                else {
                                    str += "_"+s;
                                }
                            }
                            str += ":"+ word +":"+ nextTerm.getLemma()+"\n";
                              if (fos!=null) fos.write(str.getBytes());

                            if (!matchWord(word, nextTerm, parser)) {
                                match = false;
                                break;
                            } else {
                                for (int s = 0; s < nextTerm.getSpans().size(); s++) {
                                    String span = nextTerm.getSpans().get(s);
                                    newSpans.add(span);
                                }
                                comp = new TermComponent();
                                comp.setLemma(nextTerm.getLemma());
                                comp.setId(termId+".3");
                                comp.setId(nextTerm.getTid());
                                // comp.setPos(nextTerm.getPos());
                                components.add(comp);
                                multiWordTermList.add(nextTerm);
                                if (fos!=null) fos.write(("multiword component match nextTerm.getLemma() = " + nextTerm.getLemma()+"\n").getBytes());
                            }
                        }
                        if (match) {
                            if (multiWordEntry.isGeneric()) {
                                nGWN++;
                            }
                            else {
                                nDWN++;
                            }
                            //// WE DECIDE ON THE HEAD TERM FROM THE MULTIWORD TERMLIST
                            //// THE HEAD IS USED TO REPRESENT THE MULTIWORD TERM AND THE COMPONENTS
                            //// ALL OTHER TERMS ARE REMOVED FROM KAF AS TERMS
                            //// CHUNKS AND DEPS ARE FIXED
                            KafTerm headTerm = null;
                            if (lgPatterns.size()==0) {
                                headTerm = multiWordTermList.get(multiWordTermList.size()-1);
                                  if (fos!=null) fos.write(("NO LG PATTERNS TAKING THE LAST TERM\n").getBytes());
                            }
                            else {
                                boolean patternMatch = false;
                                for (int k = 0; k < lgPatterns.size(); k++) {
                                    MultiwordPattern pattern = lgPatterns.get(k);
                                 //  System.out.println("pattern.getPos() = " + pattern.getPos());
                                 //   System.out.println("pattern.getPattern() = " + pattern.getPattern());
                                    for (int t = 0; t < multiWordTermList.size(); t++) {
                                        KafTerm term = multiWordTermList.get(t);
                                       // System.out.println("term. = " + term.getPos());
                                        // if (pattern.getPos().equalsIgnoreCase(term.getPos())) {
                                        if ((pattern.getPos().equalsIgnoreCase(term.getPos())) ||
                                            (term.getPos().toLowerCase().startsWith(pattern.getPos()+".")) ||
                                            (term.getPosIni().equalsIgnoreCase(pattern.getPos()))) {
                                           //// the term pos is a valid head
                                           headTerm = term;
                                           //System.out.println("head term term.getLemma() = " + term.getLemma());
                                           if (pattern.getPattern().equalsIgnoreCase("first")) {
                                               patternMatch = true;
                                               break;
                                           }
                                           else {
                                               // we continue because the next could be better
                                           }
                                        }
                                        else if (!pattern.getPattern().equalsIgnoreCase("last")) {
                                            /// this is a non-matching pos which may be the border for the head
                                            if ((pattern.getPattern().equalsIgnoreCase(term.getPos().toLowerCase())) ||
                                                    (term.getPos().toLowerCase().startsWith(pattern.getPattern()+".")) ||
                                                    (term.getPosIni().equalsIgnoreCase(pattern.getPattern()))) {
                                                //System.out.println("marks the post head position term.getPos() = " + term.getPos().toLowerCase());
                                                if (headTerm!=null) {
                                                    //System.out.println("keep the headTerm = " + headTerm.getLemma());
                                                }
                                                else {
                                                    //System.out.println("headTerm is null!");
                                                }

                                                patternMatch = true;
                                                /// this is the breaking point so we break and hope we have a previous term
                                                break;
                                            }
                                        }
                                    }
                                    if (pattern.getPattern().equalsIgnoreCase("last")) {
                                       patternMatch = true;
                                    }
                                    if (patternMatch) {
                                        break;
                                    }
                                }
                            }
                            /// fix the terms
                            if (headTerm != null) {

                                /// First we determine which component  is the head
                                for (int k = 0; k < components.size(); k++) {
                                    TermComponent termComponent = components.get(k);
                                    if (termComponent.getLemma().equals(headTerm.getLemma())) {
                                        headTerm.setHead(termComponent.getId());
                                    }
                                }
                                
                                headTerm.setLemma(multiWordEntry.getMultiWordLemma());
                                headTerm.setSpans(newSpans);
                                headTerm.setComponents(components);
                                //String mwTid = headTerm.getTid() + "mw";
                                String mwTid = termId;
                                //// WE FIX THE CHUNKS AND DEPS IN WHICH THE HEAD TERM OCCURS
                                ArrayList<String> chunks = parser.TermToChunk.get(headTerm.getTid());
                                if (chunks != null) {
                                    for (int k = 0; k < chunks.size(); k++) {
                                        String cid = chunks.get(k);
                                        KafChunk chunk = parser.getChunks(cid);
                                        /// we fix the head reference if necessary
                                        if (chunk.getHead().equals(headTerm.getTid())) {
                                            chunk.setHead(mwTid);
                                        }
                                        ArrayList<String> newChunkSpans = new ArrayList<String> ();
                                        newChunkSpans.add(mwTid);
                                        for (int l = 0; l < chunk.getSpans().size(); l++) {
                                            String s = chunk.getSpans().get(l);
                                            //System.out.println("s = " + s);
                                            //// add spans that are not components
                                            boolean compId = false;
                                            for (int m = 0; m < multiWordTermList.size(); m++) {
                                                KafTerm term = multiWordTermList.get(m);
                                                if (s.equals(term.getTid())) {
                                                    compId = true;
                                                    break;
                                                }
                                            }
                                            if (!compId) {
                                               newChunkSpans.add(s);
                                            }
                                        }
                                        chunk.setSpans(newChunkSpans);
                                    }
                                }
                                for (int k = 0; k < parser.kafDepList.size(); k++) {
                                    KafDep kafDep = parser.kafDepList.get(k);
                                    /// we first change any reference to the term representing the multiword
                                    if (kafDep.getFrom().equals(headTerm.getTid())) {
                                        kafDep.setFrom(mwTid);
                                    }
                                    if (kafDep.getTo().equals(headTerm.getTid())) {
                                        kafDep.setTo(mwTid);
                                    }

                                    /// we remove any dependencies in which non-head components of the terms occur
                                    for (int m = 0; m < multiWordTermList.size(); m++) {
                                        KafTerm term = multiWordTermList.get(m);
                                        if (!term.getTid().equals(headTerm.getTid())) {
                                            if (kafDep.getFrom().equals(term.getTid())) {
                                                parser.kafDepList.remove(kafDep);
                                            }
                                            else if (kafDep.getTo().equals(term.getTid())) {
                                                parser.kafDepList.remove(kafDep);
                                            }
                                        }
                                    }
                                }
                                /// assign the new mwId
                                /// we now remove all terms which are components except for the headTerm
                                for (int k = 0; k < multiWordTermList.size(); k++) {
                                    KafTerm term = multiWordTermList.get(k);
                                    if (!term.getTid().equals(headTerm.getTid())) {
                                       // System.out.println("removing term.getLemma() = " + term.getLemma());
                                        parser.kafTermList.remove(term);
                                    }
                                }
                                headTerm.setTid(mwTid);
                                  if (fos!=null) fos.write(("multiword match headTerm.getLemma() = " + headTerm.getLemma()+"\n").getBytes());
                                /// we have a match and can leaf the mwentry loop now....
                                break;
                            }
                        } else {
                            //// something did not match and we do nothing
                        }
                    } else {
                       // System.out.println("NOT ENOUGH");
                        //// not enough terms left to match
                    }
                } //// end of loop over all mwentries
            } else {
                // mwEntru == null
            }
        } /// end of loop over all terms
        //parser.writeKafToFile(out);
    }

    public int initializeTagger (String pathToConfig) {
        if (fos==null) {
            try {
                fos = new FileOutputStream ("tagging.log");
            } catch (FileNotFoundException e) {
               // e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }

        int err = 0;
        config = new Config(pathToConfig);
        lex = new MultiWordLexiconWnLmf();
        if (new File(config.genericWordnetLmfFilePath).exists()) {
            lex.parseWnLmfFile(new File(config.genericWordnetLmfFilePath), true);
             if (fos!=null) {
                try {
                    fos.write(("Read generic wordnet. lex.entryMap.size() = " + lex.entryMap.size()+"\n").getBytes());
                } catch (IOException e) {
                  //  e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
        else {
            if (fos!=null) {
                try {
                    fos.write(("Cannot find the config.genericWordnetLmfFilePath = " + config.genericWordnetLmfFilePath+"\n").getBytes());
                } catch (IOException e) {
                 //   e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
        if (config.domainWordnetLmfFilePath.length()>0) {
            if (new File(config.domainWordnetLmfFilePath).exists()) {
                lex.parseWnLmfFile(new File(config.domainWordnetLmfFilePath), false);
                if (fos!=null) {
                    try {
                        fos.write(("Read the domain wordnet. lex.entryMap.size() = " + lex.entryMap.size()+"\n").getBytes());
                    } catch (IOException e) {
                   //     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
            else {
                 if (fos!=null) {
                    try {
                        fos.write(("Cannot find the config.domainWordnetLmfFilePath = " + config.domainWordnetLmfFilePath+"\n").getBytes());
                    } catch (IOException e) {
                   //     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        }
        if (lex.entryMap.size()==0) {
            err = -1;
            if (fos!=null) {
                try {
                    fos.write(("No multiword entries read. Aborting err = " + err+"\n").getBytes());
                } catch (IOException e) {
                 //   e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        }
        return err;
    }


    public static void main (String[] args) {
        if (args.length==1) {
            MultiTagger tagger = new MultiTagger();
            if (tagger.initializeTagger(args[0])==0) {
                try {
                    tagger.tagMultiWords(System.in, System.out);
                    System.exit(0);
                } catch (IOException e) {
                    //e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    System.exit(-1);
                }
            }
        }
        else {
            System.exit(-1);
        }
    }


/*    static KafTerm getHeadFromPatterns (ArrayList<String> patterns) {
        KafTerm headTerm = null;
        return headTerm;
    }*/
    

    static ArrayList<MultiWordEntry> getMwEntries (MultiWordLexiconWnLmf lex,KafTerm kafTerm, KafSaxParser parser ) {
        ArrayList<MultiWordEntry> mwEntry = null;
        if (lex.entryMap.containsKey(kafTerm.getLemma())) {
            mwEntry = lex.entryMap.get(kafTerm.getLemma());
        }
        else { /// the lemma was not found but perhaps the original word form can be found
            String wf = "";
            for (int j = 0; j < kafTerm.getSpans().size(); j++) {
               String wid = kafTerm.getSpans().get(j);
                KafWordForm wordForm = parser.getWordForm(wid);
                if (wordForm==null) {
                   // System.out.println("No wordform for wid = " + wid);
                }
                else {
                   wf += " "+wordForm.getWf();
                }
            }
            wf = wf.toLowerCase().trim();
            if (lex.entryMap.containsKey(wf)) {
                mwEntry = lex.entryMap.get(wf);
            }
            else {
                //// no match....
            }
        }
        if (mwEntry!=null) {
            return sortMultiWordArrayOnLength(mwEntry);
        }
        else {
            return null;
        }
    }



    static ArrayList<MultiWordEntry> sortMultiWordArrayOnLength (ArrayList<MultiWordEntry> mws) {
        ArrayList<MultiWordEntry> sortedMws = new ArrayList<MultiWordEntry> ();
        TreeSet sorter = new TreeSet(
                new Comparator() {
                    public int compare(Object a, Object b) {
                        MultiWordEntry itemA = (MultiWordEntry) a;
                        MultiWordEntry itemB = (MultiWordEntry) b;
                        if (itemA.getWordlist().size()<(itemB.getWordlist().size())) {
                            return -1;
                        } else if (itemA.getWordlist().size()==(itemB.getWordlist().size())) // We force equal frequencies to be inserted
                        {
                            return -1;
                        } else {
                            return 1;
                        }
                    }
                }
        );
        for (int i = 0; i < mws.size(); i++) {
            MultiWordEntry multiWordEntry = mws.get(i);
            sorter.add(multiWordEntry);
        }
        Iterator it = sorter.iterator();
        while (it.hasNext()) {
            MultiWordEntry mwe = (MultiWordEntry) it.next();
            sortedMws.add(mwe);
        }
        return sortedMws;
    }
    static boolean matchWord (String word, KafTerm kafTerm, KafSaxParser parser ) {
        if (kafTerm.getLemma().equalsIgnoreCase(word)) {
            return true;
        }
        else { /// the lemma was not found but perhaps the original word form can be found
            String wf = "";
            for (int j = 0; j < kafTerm.getSpans().size(); j++) {
               String wid = kafTerm.getSpans().get(j);
               KafWordForm wordForm = parser.getWordForm(wid);
               if (wordForm==null) {
                 //  System.out.println("No wordform for wid = " + wid);
               }
               else {
                    wf += " "+wordForm.getWf();
               }
            }
            wf = wf.toLowerCase().trim();
            if (wf.equalsIgnoreCase(word)) {
                return true;
            }
            else {
                //// no match....
            }
        }
        return false;
    }

}
