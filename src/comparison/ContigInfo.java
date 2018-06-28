/**
 * 
 */

package comparison;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * 
 *
 * @author Aaron Rothemich
 *
 */
public class ContigInfo {

  public static ArrayList<String[]> metadata = new ArrayList<>(); 
  public static ArrayList<String[]> sampleList = new ArrayList<>();
  public static String metaFilename = "Metadata/hmp2_metadata.csv";

  
  public static void main(String[] args) {
    
    getContigInfo();
    getMetaData();
    printMetadata();
    System.out.println("Done");
    
  }
  
  public static void getMetaData() {
    
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
      
      for (String[] sample : sampleList) {
        if (splitLine[1].equals(sample[0])) {
          splitLine[12] = sample[1];
        }
      }
      metadata.add(splitLine);
    }
    diseaseScanner.close();
    
    
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
          Scanner contigScanner = new Scanner(contig);
          String header = contigScanner.nextLine();
          contigScanner.close();
          String reads = header.substring(header.indexOf("multi=") + 6, header.lastIndexOf(' '));
          totalReads += Double.valueOf(reads);
          String[] sample = {sampleName, "" + totalReads};
          sampleList.add(sample);
        }
        fileScanner.close();
      }
      
      
    }
    
    
  }
  
  public static void printMetadata() {
    File newMetadata = new File(metaFilename);
    PrintStream metaStream;
    try {
      metaStream = new PrintStream(newMetadata);
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException(e);
    }
    for (String[] row : metadata) {
      for (String cell : row) {
        metaStream.print(cell + ",");
      }
      metaStream.print("\n");
    }
    metaStream.close();
  }
  
  
  
  
  
  
}
