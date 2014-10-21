package eu.kyotoproject.multiwordtagger;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

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
public class MultiTaggerSingleNaf {
    static MultiTagger tagger = new MultiTagger();
    static final String layer = "terms";
    static final String name = "vua-multiword-tagger";
    static final String version = "1.0";


    static public void main (String [] args) {
        tagger = new MultiTagger();
        tagger.setNAF(true);
        String configFilePath = "";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ((arg.equals("--conf-file")) && (args.length>(i+1))) {
                configFilePath = new File(args[i+1]).getAbsolutePath();
                break;
            }

        }
        try {
          //  System.out.println("configFilePath = " + configFilePath);
            if (tagger.initializeTagger(configFilePath)==0) {
                if (args.length==2) {
                    tagger.tagMultiWords(layer, name, version, System.in, System.out);
                }
                else if (args.length==4) {
                    String pathToNafFile = "";
                    for (int i = 0; i < args.length; i++) {
                        String arg = args[i];
                        if (arg.equals("--naf-file") && args.length>(i+1)) {
                            pathToNafFile = args[i+1];
                        }
                    }
                    FileInputStream in = new FileInputStream (pathToNafFile);
                    tagger.tagMultiWords(layer, name, version, in, System.out);

                }
                else {
                   // System.out.println("args.length = " + args.length);
                }
            }
            else {
                /// error processing configuration file.
             //   System.out.println("error reading configFilePath = " + configFilePath);
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}