package baristat.DataReaders;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * Class for reading from .csv Files.
 *
 * Primarily used for reading in HubWay data in lieu of actual sales data from
 * Square.
 *
 * @author Tristin Falk
 *
 */
public class CsvReader {

  private String file;
  private BufferedReader reader;
  private String errorMessage;
  private List<String> headers;

  /**
   * Constructor for CsvReader.
   *
   * @param filePath
   *          filePath to read from
   * @throws IOException
   *           if filePath incorrect
   */
  public CsvReader(String filePath) throws IOException {
    errorMessage = null;
    headers = null;
    setFilePath(filePath);
  }

  /**
   * Sets file path.
   *
   * @param filePath
   *          path to set file to
   * @throws IOException
   *           if filePath invalid
   */
  public void setFilePath(String filePath) throws IOException {
    if (!filePath.endsWith(".csv")) {
      printError("file must be .csv");
      this.file = null;
      throw new IOException("file must be .csv");
    }

    try {
      this.reader = new BufferedReader(
          new InputStreamReader(new FileInputStream(filePath), "UTF8"));
    } catch (IOException ex) {
      printError("could not open .csv file");
      this.file = null;
      throw new IOException("could not open csv");
    }

    this.file = filePath;
  }

  /**
   * Reads a single line from csv.
   *
   * @return Line in form of List
   */
  public List<String> readHeaders() {
    if (headers == null) {
      headers = readLine();
    }
    return headers;
  }

  /**
   * Reads a single line from csv.
   *
   * @return Line in form of List
   */
  public List<String> readLine() {
    if (this.file == null || this.reader == null) {
      printError("must set file path before reading can occur");
      return null;
    }

    if (headers == null) {
      printError("must read headers before reading lines");
      return null;
    }

    List<String> line = new ArrayList<>();

    try {
      String currLine = this.reader.readLine();
      if (currLine == null) {
        // Reached EOF
        this.close();
        return null;
      }
      line = Arrays.asList(currLine.split(","));
    } catch (IOException ex) {
      printError("could not read line");
    }

    return line;
  }

  /**
   * Closes underlying BufferedReader. This will be called when we hit null
   */
  public void close() {
    if (reader != null) {
      try {
        reader.close();
        reader = null;
      } catch (IOException e) {
        this.printError("ERROR: could close file");
      }
    }
  }

  private void printError(String error) {
    errorMessage = error;
    System.out.println("ERROR: " + error);
  }

  /**
   * @return latest error message;
   */
  public String getError() {
    return errorMessage;
  }

}
