package pipeline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashSet;

public class FilterMRREL {

	public static void main(String[] args) throws Exception {

		BufferedReader br = null;
		BufferedWriter bw = null;
		MRRELRecord rec = null;
		int index = 0;

		String rela;
		String sCurrentLine;
		br = new BufferedReader(new FileReader("MRREL.RRF"));
		bw = new BufferedWriter(new FileWriter("COMPACT_MRREL.RRF"));
		while ((sCurrentLine = br.readLine()) != null) {
			rec = new MRRELRecord();
			rec.items = sCurrentLine.split("\\|", 17);
			// ??? the case of cui1 == cui2
			rela = rec.getRELA();
			if (!rela.equals("")) {
				bw.write(sCurrentLine);
				bw.newLine();
			}

		}
		br.close();
		bw.close();
	}

}
