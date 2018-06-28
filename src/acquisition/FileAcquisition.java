/**
 * 
 */

package acquisition;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This program will iterate through the metadata to build a list of samples. Then it will download
 * log files for each sample. Next, it will find the number of reads after-QC within the log file.
 * Then, it will build a list of the samples for each patient with the highest number of reads 
 * post-QC.
 * Metadata found at: https://ibdmdb.org/tunnel/cb/document/Public/HMP2/Metadata/hmp2_metadata.csv
 * Log files found at: https://ibdmdb.org/tunnel/public/HMP2/WGS/1818/rawfiles
 *
 * @author Aaron Rothemich
 *
 */
public class FileAcquisition {
  
  /** Full URL of metadata. */
  public static String metaURL = "https://ibdmdb.org/tunnel/cb/document/Public/HMP2/Metadata/hmp2_metadata.csv";
  
  /** Relative path and filename for storage of metadata. */
  public static String metaFilename = "Metadata/hmp2_metadata.csv";
  
  /** 
   * Data type of interest (e.g. metagenomics, metatranscriptomics, 
   * per column E of hmp2_metadata). 
   */
  public static String interestingDataType = "metagenomics";
  
  /** ArrayList to store metadata before file writing. */
  public static ArrayList<String[]> metadata = new ArrayList<>();
  
  public static String sequenceUrlPath = "https://ibdmdb.org/tunnel/static/HMP2/WGS/1818/";
  
  public static String sequenceFileExt = ".tar";
  
  public static String sequenceType = "rawseqs";
  
  /**
   * Main method, calls other methods.
   * 
   * @param args command line arguments
   */
  public static void main(String[] args) {
        
    metadata = getMetadata();
    downloadLogs();
    countSize();
    keepLargest();
    printMetadata();
    downloadSequences();
  }
  
  /**
   * Collect the metadata from the .csv file pointed to by metaURL
   * @return metadata ArrayList
   */
  public static ArrayList<String[]> getMetadata() {
    new File("metadata").mkdirs();
    try {
      saveUrl(metaFilename, metaURL);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
    
    ArrayList<String[]> metadata = new ArrayList<>();
    File metaFile = new File(metaFilename);
    String line;
    try {
      BufferedReader metaReader = new BufferedReader(new FileReader(metaFile));
      while ((line = metaReader.readLine()) != null) {
        String[] row = line.split(",");
        if (row[4].equals(interestingDataType) 
            && (row[69].equals("CD") || row[69].equals("nonIBD"))) {
          metadata.add(row);
        }
        
      }
      metaReader.close();
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
    // Assign several new header fields for later use
    metadata.get(0)[11] = "QC'd Raw Reads";
    metadata.get(0)[12] = "Total contig multi";
    return metadata;

  }
  
  /**
   * Downloads logs from the URL starter as .log files
   */
  public static void downloadLogs() {
    new File("RawLogs").mkdir();
    try {
      for (String[] row : metadata) {
        saveUrl("RawLogs/" + row[1] + ".log", sequenceUrlPath 
                + row[1] + ".log");
      }
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
    
  }
  
  /**
   * Downloads file from URL specified.
   * 
   * @param filename name of File to be saved
   * @param urlString name of URL to be downloaded from
   * @throws MalformedURLException if not properly formatted
   * @throws IOException if errors in input
   */
  public static void saveUrl(final String filename, final String urlString)
          throws MalformedURLException, IOException {
    BufferedInputStream in = null;
    FileOutputStream fout = null;
    try {
      in = new BufferedInputStream(new URL(urlString).openStream());
      fout = new FileOutputStream(filename);

      final byte[] data = new byte[1024];
      int count;
      while ((count = in.read(data, 0, 1024)) != -1) {
        fout.write(data, 0, count);
      }
    } finally {
      if (in != null) {
        in.close();
      }
      if (fout != null) {
        fout.close();
      }
    }
  }
  
  public static void countSize() { 

    File logDir = new File("RawLogs");
    File[] files = logDir.listFiles();
    for (File f : files) {
      if (f.getName().contains(".log")) {
        String content = "";
        Scanner fileScanner;
        try {
          fileScanner = new Scanner(f);
        } catch (FileNotFoundException e) {
          throw new IllegalArgumentException(e);
        }
        while (fileScanner.hasNextLine()) {
          content += fileScanner.nextLine() + "\n";
        }
        fileScanner.close();
        int startPoint = 
            content.lastIndexOf("final pair1");
        content = content.substring(startPoint);
        Scanner contentScanner = new Scanner(content);
        while (!contentScanner.hasNextInt()) {
          contentScanner.next();
        }
        int readCount = contentScanner.nextInt();
        contentScanner.close();
        String name = f.getName();
        name = name.substring(0, name.indexOf('.'));
        for (String[] row : metadata) {
          if (name.equals(row[1])) {
            row[11] = "" + readCount;
          }
        }     
      }
    }
      
    printMetadata();
  }
  
  public static void printMetadata() {
    new File("metadata").mkdir();
    
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
    
  public static void keepLargest() {
    for (int a = 1; a < metadata.size(); a++) {
      String[] row = metadata.get(a);
      int max = Integer.valueOf(row[11]);
      boolean smaller = false;
      for (int i = 1; i < metadata.size(); i++) {
        if (row[2].equals(metadata.get(i)[2])) {
          if (Integer.valueOf(metadata.get(i)[11]) > max) {
            smaller = true;
          }
        }
      }
      if (smaller) {
        metadata.remove(a);
        a--;
      }
    }

    printMetadata();
  }
  
  public static void downloadSequences() {
    new File(sequenceType).mkdir();
    
    for (int i = 1; i < metadata.size(); i++) {
      String[] row = metadata.get(i);
      try {
        saveUrl(sequenceType + "/" + row[1] + sequenceFileExt, 
            sequenceUrlPath + row[1] + sequenceFileExt);
      } catch (IOException e) {
        throw new IllegalArgumentException(e);
      }
    }
  }
}
