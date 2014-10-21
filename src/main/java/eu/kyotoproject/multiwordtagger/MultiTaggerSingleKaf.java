package eu.kyotoproject.multiwordtagger;


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
public class MultiTaggerSingleKaf {

    public static void main (String[] args) {


        String inputKafFilePath = "";
        String configFilePath = "";
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ((arg.equals("--kaf-file")) && (args.length>(i+1))) {
               inputKafFilePath = args[i+1];
            }
            if ((arg.equals("--conf-file")) && (args.length>(i+1))) {
                configFilePath = args[i+1];
            }
        }
        if (!configFilePath.isEmpty() && !inputKafFilePath.isEmpty()) {
            MultiTagger tagger = new MultiTagger();
           // tagger.setDebug(true);
/*            try {
                tagger.fos = new FileOutputStream("mw.log");
            } catch (FileNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }*/
            if (tagger.initializeTagger(configFilePath)==0) {
                try {
                    FileInputStream in = new FileInputStream (inputKafFilePath);
                    tagger.tagMultiWords(in, System.out);
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            else {
               // System.out.println("error reading configuration file");
            }
/*            try {
                tagger.fos.close();
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }*/

        }
    }
}