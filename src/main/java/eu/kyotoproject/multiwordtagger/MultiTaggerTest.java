package eu.kyotoproject.multiwordtagger;


import java.io.*;
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
public class MultiTaggerTest {

    public static void main (String[] args) {
        String insuffix = ".kaf";
        String outsuffix = ".mw.kaf";
        if (args.length==3) {
            insuffix = args[2];
        }
        else if (args.length==4) {
            insuffix = args[2];
            outsuffix = args[3];
        }
        if (args.length>=2) {
            int nDWN = 0;
            int nGWN = 0;
            int nTerms = 0;
            ArrayList<File> files = new ArrayList<File>();
            MultiTagger tagger = new MultiTagger();
            tagger.setDebug(true);
            if (tagger.initializeTagger(args[0])==0) {
                try {
                    String inputKafFolderPath = args[1];
                    File inputKafFolder = new File (inputKafFolderPath);
                    System.out.println("inputKafFolder.getAbsolutePath() = " + inputKafFolder.getAbsolutePath());
                    if (inputKafFolder.exists()) {
                        String outputKafFolderPath = inputKafFolder+"_"+"lp+mw";
                        File outputKafFolder = new File (outputKafFolderPath);
                        if (!outputKafFolder.exists()) {
                            outputKafFolder.mkdir();
                        }
                        files = makeRecursiveFileList(inputKafFolder, insuffix);
                        for (int f = 0; f < files.size(); f++) {
                            File inputKafFile = files.get(f);
                            String inputKafFilePath = inputKafFile.getAbsolutePath();
                            System.out.println("inputKafFile = " + inputKafFilePath);
                            if (tagger.fos!=null) tagger.fos.write(inputKafFilePath.getBytes());
                            String inputKafName = inputKafFile.getName();
                            String outputKafFile = outputKafFolderPath+"/"+ inputKafName.substring(0, inputKafName.lastIndexOf("."))+outsuffix;
                            FileInputStream in = new FileInputStream (inputKafFile);
                            FileOutputStream fos = new FileOutputStream (outputKafFile);
                            tagger.tagMultiWords(in, fos);
                            nTerms += tagger.getnTerms();
                            nDWN += tagger.getnDWN();
                            nGWN += tagger.getnGWN();
                            fos.close();
                        }
                    }
                    else {
                        System.out.println("Cannot find inputKafFolder = " + inputKafFolderPath);
                    }
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            if (tagger.fos!=null) {
                try {
                    tagger.fos.write(("Nr files="+files.size()+"\n").getBytes());
                    tagger.fos.write(("All terms="+nTerms+"\n").getBytes());
                    tagger.fos.write(("Generic multiwords tagged="+nGWN+"\n").getBytes());
                    tagger.fos.write(("Domain multiwords tagged="+nDWN+"\n").getBytes());
                    tagger.fos.close();
                } catch (IOException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            System.out.println("files = " + files.size());
            System.out.println("nTerms = " + nTerms);
            System.out.println("nGWN = " + nGWN);
            System.out.println("nDWN = " + nDWN);            
        }

    }


    static public ArrayList<File> makeRecursiveFileList(File lF, String theFilter) {
        ArrayList<File> acceptedFileList = new ArrayList<File>();
        File[] theFileList = null;
        if ((lF.canRead()) && lF.isDirectory()) {
            theFileList = lF.listFiles();
            for (int i = 0; i < theFileList.length; i++) {
                String newFilePath = theFileList[i].getAbsolutePath();
                if (theFileList[i].isDirectory()) {
                    ArrayList<File> nextFileList = makeRecursiveFileList(theFileList[i], theFilter);
                    for (int j = 0; j < nextFileList.size(); j++) {
                        File file = nextFileList.get(j);
                        acceptedFileList.add(file);
                    }
                    nextFileList = null;
                } else {
                   if (theFileList[i].getName().toLowerCase().endsWith(theFilter.toLowerCase())) {
                        acceptedFileList.add(theFileList[i]);
                    }
                }
            }
        }
        return acceptedFileList;
    }

}