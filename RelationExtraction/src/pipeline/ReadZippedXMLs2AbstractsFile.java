package pipeline;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class ReadZippedXMLs2AbstractsFile {

	public static int abstractIndex = 1;

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		String inputFileDirectoryPath = "C:\\Research\\Medline Data\\2015 MEDLINEPUBMED BASELINE FILES";
		String fileNameRoot = "medline15n0";
		// both inclusive, 3 digit string
		int fileNameIndexStart = 201;
		// int fileNameIndexEnd = 779;
		int fileNameIndexEnd = 201;

		// int fileNameIndexEnd = 2;
		BufferedWriter fileBW = new BufferedWriter(new FileWriter(new File(
				"all_abstracts.txt")));
		BufferedWriter statBW = new BufferedWriter(new FileWriter(new File(
				"abstracts_stats.csv")));
		String csvFirstLine = "Index,Source,WordCount";
		statBW.write(csvFirstLine);
		statBW.newLine();
		String indexStr;
		for (int i = fileNameIndexStart; i <= fileNameIndexEnd; i++) {
			System.out.println("current index is: " + i);
			if (i < 10)
				indexStr = "00" + i;
			else if (i >= 10 && i < 100)
				indexStr = "0" + i;
			else
				indexStr = "" + i;

			readZippedXML2AbstractsString(inputFileDirectoryPath, fileNameRoot
					+ indexStr + ".xml", fileBW, statBW);
		}
		fileBW.close();
		statBW.close();
	}

	/**
	 * 
	 * @param fileDir
	 *            "C:\\Research\\Medline Data\\2015 MEDLINEPUBMED BASELINE FILES"
	 * @param fileXMLName
	 *            "medline15n0001.xml"
	 * @throws Exception
	 */
	public static void readZippedXML2AbstractsString(String fileDir,
			String fileXMLName, BufferedWriter fileBW, BufferedWriter statBW)
			throws Exception {
		String abstractsStr = "";
		FileInputStream fin = new FileInputStream(fileDir + "\\" + fileXMLName
				+ ".zip");
		ZipInputStream zin = new ZipInputStream(fin);
		ZipEntry ze = null;
		String abstractText;
		String stat;
		while ((ze = zin.getNextEntry()) != null) {
			if (ze.getName().equals("../zip/" + fileXMLName)) {
				// pass zin to DocumentBuilder
				DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory
						.newInstance();
				DocumentBuilder docBuilder = docBuilderFactory
						.newDocumentBuilder();
				Document document = docBuilder.parse(zin);
				NodeList nl = document.getDocumentElement()
						.getElementsByTagName("AbstractText");
				for (int i = 0; i < nl.getLength(); i++) {
					abstractText = nl.item(i).getTextContent();
					fileBW.write(abstractText);
					fileBW.newLine();
					stat = "" + abstractIndex + "," + fileXMLName + ","
							+ countWords(abstractText);
					statBW.write(stat);
					statBW.newLine();
					abstractIndex++;
				}

				break;
			}
		}
		zin.close();
		// return abstractsStr;
	}

	public static int countWords(String s) {
		int wordCount = 0;
		boolean word = false;
		int endOfLine = s.length() - 1;
		for (int i = 0; i < s.length(); i++) {
			// if the char is a letter, word = true.
			if (Character.isLetter(s.charAt(i)) && i != endOfLine) {
				word = true;
				// if char isn't a letter and there have been letters before,
				// counter goes up.
			} else if (!Character.isLetter(s.charAt(i)) && word) {
				wordCount++;
				word = false;
				// last word of String; if it doesn't end with a non letter, it
				// wouldn't count without this.
			} else if (Character.isLetter(s.charAt(i)) && i == endOfLine) {
				wordCount++;
			}
		}
		return wordCount;
	}
}
