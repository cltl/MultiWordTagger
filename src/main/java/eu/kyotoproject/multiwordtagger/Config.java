package eu.kyotoproject.multiwordtagger;

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
public class Config {
    /*
    lang=en
    N:NH:P:X
    N:[A,V,N]:NH
    V:[A,N]:VH:X
    A:X:AH

    lang=en
    N:P      (term with pos=N before P)
    N:last   (term with pos=N before EOS (end of string))
    N:first  (term with pos=N after SOS (start of string))
    V:last   etc...
    A:last
    */

    public HashMap<String,ArrayList<MultiwordPattern>> patterns;
    public String genericWordnetLmfFilePath;
    public String domainWordnetLmfFilePath;

    public Config(String pathToConfig) {
        final String lgField = "lang=";
        final String gwnField = "generic_wn_lmf=";
        final String dwnField = "domain_wn_lmf=";
        patterns = new HashMap<String,ArrayList<MultiwordPattern>> ();
        genericWordnetLmfFilePath = "";
        domainWordnetLmfFilePath = "";
        //File configFile = new File("conf/mwtagger.cfg");
        File configFile = new File(pathToConfig);
        if (configFile.exists()) {
            try {
                FileInputStream fis = new FileInputStream(configFile);
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader in = new BufferedReader(isr);
                String inputLine;
                String lg = "";
                while (in.ready()&&(inputLine = in.readLine()) != null) {
                    inputLine = inputLine.trim();
                    if ((inputLine.length()>0) && (!inputLine.startsWith("#"))) {
                        if (inputLine.startsWith(lgField)) {
                            lg = inputLine.substring(lgField.length()).trim().toLowerCase();
                        }
                        else if (inputLine.startsWith(gwnField)) {
                            genericWordnetLmfFilePath = new File (inputLine.substring(gwnField.length()).trim()).getAbsolutePath();
                        }
                        else if (inputLine.startsWith(dwnField)) {
                            domainWordnetLmfFilePath = new File (inputLine.substring(dwnField.length()).trim()).getAbsolutePath();
                        }
                        else {
                            if (inputLine.indexOf(":")>-1) {
                                /// line has pattern
                                MultiwordPattern pattern = new MultiwordPattern(inputLine);
                                if (lg.length()>0) {
                                    if (patterns.containsKey(lg)) {
                                        ArrayList<MultiwordPattern> lgPatterns = patterns.get(lg);
                                        lgPatterns.add(pattern);
                                        patterns.put(lg, lgPatterns);
                                    }
                                    else {
                                        ArrayList<MultiwordPattern> lgPatterns = new ArrayList<MultiwordPattern>();
                                        lgPatterns.add(pattern);
                                        patterns.put(lg, lgPatterns);

                                    }
                                }
                            }
                        }
                    }
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else {
         //   System.out.println("COULD NOT FIND configFile.getAbsolutePath() = " + configFile.getAbsolutePath());
        }
    }
}
