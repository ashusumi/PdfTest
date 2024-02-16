package com.PdfToJson;

import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;

public class Itext {

	public void read() {
		try {
			PdfReader reader = new PdfReader(
					"C:\\Users\\Nimap\\Desktop\\PdfTask\\Sample_Files_PDF_Conversion (1)\\Bank Statement Coding\\FY23 - INVISION 12-3287-0015413-00.pdf");
			int n = reader.getNumberOfPages();
			String str = PdfTextExtractor.getTextFromPage(reader, 2); // Extracting the content from a particular page.
			System.out.println(str);
			reader.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
