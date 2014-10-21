package eu.kyotoproject.multiwordtagger;

import java.util.ArrayList;

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
public class MultiWordEntry {

    private ArrayList<String> wordlist;
    private ArrayList<Sense> senseList;
    private String pos;
    private boolean generic;

    public MultiWordEntry() {
        this.wordlist = new ArrayList<String>();
        this.pos = "";
        this.generic = true;
        this.senseList = new ArrayList<Sense>();

    }


    public boolean isGeneric() {
        return generic;
    }

    public void setGeneric(boolean generic) {
        this.generic = generic;
    }

    public ArrayList<String> getWordlist() {
        return wordlist;
    }

    public void setWordlist(ArrayList<String> wordlist) {
        this.wordlist = wordlist;
    }

    public void addWordlist(String word) {
        this.wordlist.add(word);
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public ArrayList<Sense> getSenseList() {
        return senseList;
    }

    public void setSenseList(ArrayList<Sense> senseList) {
        this.senseList = senseList;
    }

    public void addSenseList(Sense sense) {
        this.senseList.add(sense);
    }

    public String getMultiWordLemma () {
        String lemma = "";
        for (int i = 0; i < wordlist.size(); i++) {
            lemma += " " + wordlist.get(i);

        }
        return lemma.trim();
    }
}
