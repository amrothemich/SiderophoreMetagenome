package comparison;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

public class CompareSiderophores {

  public static int eValueMax = 05;
  
  public static ArrayList<String[]> siderophores = new ArrayList<>();
  
  public static ArrayList<String[]> refLength = new ArrayList<>();
    
  public static ArrayList<ArrayList<String[]>> contigList = new ArrayList<>();
  
  public static ArrayList<String[]> sampleList = new ArrayList<>();
  
  public static String diamondOutPath = "Diamond/coliCFT073";

  
  /**
   * Main method, calls other methods.
   * 
   * @param args command line arguments
   */
  public static void main(String[] args) {
    /*
     * Write header for siderophores table
     * 
     * 0 = Sample name
     * 1 = Disease State
     * 2 = Contig name
     * 3 = Contig length
     * 4 = Reference name
     * 5 = Contig reads
     * 6 = Reference length
     * 7 = Relative abundance
     * 8 = Alignment length
     * 9 = Percent identity
     * 10 = E-value
     * 11 = Reference Genome
     * 12 = total multi
     */
    String[] header = {"Sample name", "Disease State", "Contig", "Contig length", 
        "Reference name", "Total reads",  "Reference length", "Relative Abundance", 
        "Alignment Length", "Percent identity", "E-value", "Reference Genome", "Total multi"};
    siderophores.add(header);
    collectDiamondData(diamondOutPath);
    getReferenceLength();
    //printRefLength();
    getContigInfo();
    cullData();
    printSiderophores();
    System.out.println("Done");
  }
  
  /** 
   * Collects diamond data and organizes into spreadsheet as 
   * specified in comments of CompareSiderophores.main().
   * @param diamondOutPath file path of directory of Diamond alignment output
   */
  public static void collectDiamondData(String diamondOutPath) {
    
    File diamondOutDir = new File("Diamond/coliCFT073");
    File[] diamondOutList = diamondOutDir.listFiles();
    diamondOutList = diamondOutDir.listFiles();
    for (File f : diamondOutList) {
      String name = f.getName();
      Scanner diamondScanner;
      try {
        diamondScanner = new Scanner(f);
      } catch (FileNotFoundException e) {
        throw new IllegalArgumentException(e);
      }
      while (diamondScanner.hasNextLine()) {
        String[] geneHit = new String[13];
        geneHit[0] = name;
        String line = diamondScanner.nextLine(); 
        Scanner lineScanner = new Scanner(line);
        lineScanner.useDelimiter("\t");
        String queryId = lineScanner.next();
        geneHit[2] = queryId;
        String refId = lineScanner.next();
        geneHit[4] = refId;
        String percentId = lineScanner.next();
        geneHit[9] = percentId;
        String alignLength = lineScanner.next();
        geneHit[8] = alignLength;
        for (int i = 0; i < 6; i++) {
          lineScanner.next();
        }
        String evalue = lineScanner.next();
        geneHit[10] = evalue;
        lineScanner.close();
        String genomeName = "coliCFT073";
        geneHit[11] = genomeName;
        siderophores.add(geneHit);
      }
      diamondScanner.close();
    }
    
    
  }
  
  public static void getReferenceLength() {
    
    File refDir = new File("ReferenceGenes");
    File[] refFiles = refDir.listFiles();
    for (File f : refFiles) {
      String refName = f.getName().substring(0, f.getName().indexOf('.'));
      if (f.getName().contains(".fasta")) {
        Scanner fileScanner;
        try {
          fileScanner = new Scanner(f);
        } catch (FileNotFoundException e) {
          throw new IllegalArgumentException(e);
        }
        fileScanner.useDelimiter(">");
        while (fileScanner.hasNext()) {
          String gene = fileScanner.next();
          String[] geneInfo = new String[3];
          String name = gene.substring(0, gene.indexOf(' '));
          geneInfo[1] = name;
          int count = 0;
          Scanner geneScanner = new Scanner(gene);
          geneScanner.nextLine();
          while (geneScanner.hasNextLine()) {
            String line = geneScanner.nextLine();
            count += line.length();
          }
          geneScanner.close();
          geneInfo[0] = refName;
          geneInfo[2] = "" + count;
          refLength.add(geneInfo);
        }
        fileScanner.close();
      }
      
      
    }
  }
  
  public static void getContigInfo() {
    File contigDir = new File("contigs");
    File[] contigFiles = contigDir.listFiles();
    for (File f : contigFiles) {
      if (f.getName().contains(".fna")) {
        String sampleName = f.getName().substring(0, f.getName().indexOf('_'));
        
        Scanner fileScanner;
        try {
          fileScanner = new Scanner(f);
        } catch (FileNotFoundException e) {
          throw new IllegalArgumentException(e);
        }
        fileScanner.useDelimiter(">");
        
        double totalReads = 0;
        while (fileScanner.hasNext()) {
          String contig = fileScanner.next();
          String[] contigInfo = new String[4];
          String contigName = contig.substring(0, contig.indexOf(' '));
          Scanner contigScanner = new Scanner(contig);
          String header = contigScanner.nextLine();
          contigScanner.close();
          String length = header.substring(header.lastIndexOf('=') + 1, header.length());
          contigInfo[0] = sampleName;
          contigInfo[1] = contigName;
          contigInfo[2] = length;
          String reads = header.substring(header.indexOf("multi=") + 6, header.lastIndexOf(' '));
          contigInfo[3] = reads;
          totalReads += Double.valueOf(reads);
          if (contigList.isEmpty()) {
            contigList.add(new ArrayList<String[]>());
            contigList.get(0).add(contigInfo);
          }
          boolean exists = false;
          for (ArrayList<String[]> contigLength : contigList) {
            if (contigLength.get(0)[0].equals(contigInfo[0])) {
              contigLength.add(contigInfo);
              exists = true;
            }
          }
          if (!exists) {
            ArrayList<String[]> toAdd = new ArrayList<String[]>();
            toAdd.add(contigInfo);
            contigList.add(toAdd);
          }
          String[] sample = {sampleName, "" + totalReads};
          sampleList.add(sample);
        }
        fileScanner.close();
      }
      
      
    }
  }

  
  public static void cullData() {
    for (int i = 1; i < siderophores.size(); i++) {
      String[] geneHit = siderophores.get(i);
      double evalue = Double.valueOf(geneHit[10].substring(geneHit[10].lastIndexOf('-') + 1, 
          geneHit[10].length() - 1));
      // Remove if e-value less than 10^-5
      if (evalue < 5) {
        siderophores.remove(i);
        i--;
      } else if (Double.valueOf(geneHit[9]) <= 60.0) {
        siderophores.remove(i);
        i--;
      } else {
        boolean stillThere = true;
        for (String[] length : refLength) {
          
          if (length[0].equals(geneHit[11])) {
            System.out.println("genome refL: " + length[0] + ", " + "genome hit: " + geneHit[11] 
                + "name refL: " + length[1] + ", " + "name hit: " + geneHit[4]);
            if (length[1].equals(geneHit[4])) {
              geneHit[6] = length[2];
              System.out.println("!!!EUREKA!!!");
              // Remove if hit length is less than 50% of length of gene
              if ((Double.valueOf(geneHit[8] + ".0") / Double.valueOf(length[2] + ".0")) <= .5) {
                siderophores.remove(i);
                i--;
                stillThere = false;

              }
            }
          }
        }
        if (stillThere) {
          for (ArrayList<String[]> contigLength : contigList) {
            if (contigLength.get(0)[0].equals(geneHit[0])) {
              for (String[] length : contigLength) {
                if (length[1].equals(geneHit[2])) {
                  geneHit[3] = length[2];
                  geneHit[5] = length[3];
                }
                
              } 
            }
          }
        }
        for (String[] sample : sampleList) {
          
          if (sample[0].equals(geneHit[0])) {
            geneHit[12] = sample[1];
          }
          
        }
        
        /*double relativeA = Double.valueOf(geneHit[5]);
        relativeA *= Double.valueOf(geneHit[8]);
        relativeA /= Double.valueOf(geneHit[6]);
        relativeA /= Double.valueOf(geneHit[12]);
        geneHit[7] = "" + relativeA;*/
        
        
        // Fill out metadata
        File metaData = new File("Metadata/hmp2_metadata.csv");
        Scanner diseaseScanner;
        try {
          diseaseScanner = new Scanner(metaData);
        } catch (FileNotFoundException e) {
          throw new IllegalArgumentException(e);
        }
        while (diseaseScanner.hasNextLine()) {
          String line = diseaseScanner.nextLine();
          String[] splitLine = line.split(",");
          if (splitLine[1].equals(geneHit[0])) {
            geneHit[1] = splitLine[69];
          }
        }
        diseaseScanner.close();
      }
    }
  }
  

  public static void printSiderophores() {
    File outFile = new File("Results.csv");
    PrintStream outStream;
    try {
      outStream = new PrintStream(outFile);
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
    
    for (String[] hit : siderophores) {
      if (hit[11].equals("coliCFT073")) {
        for (String s : hit) {
          outStream.print(s + ",");
        }
        outStream.println();
      }
      
    }
    outStream.close();
  }
  
  public static void printRefLength() {
    for (String[] hit : refLength) {
      for (String s : hit) {
        System.out.print(s + "\t");
      }
      System.out.println();;
    }
  }
}
