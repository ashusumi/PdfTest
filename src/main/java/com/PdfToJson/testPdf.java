package com.PdfToJson;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class testPdf {

	private static final String flag = "* The exchange rate selected by Visa from a range of available wholesale rates or, if applicable, the government mandated rate to convert currency on\n"
			+ "the overseas cash withdrawal or other overseas transaction.\n"
			+ "** The Offshore Service Margin is 1.10% for a FastCash overseas withdrawal and 2.10% for a Visa Debit overseas transaction.\n"
			+ "*** The Retail Exchange Margin of 0.70% charged on cash withdrawals made using a Commonwealth Bank of Australia ATM.\n"
			+ "(Note the Retail Exchange Margin only applies to cash withdrawals made prior to 30 October 2012.)\n"
			+ "The Unarranged Overdraft interest rate is 22.5% per annum. The interest rate is less than the Finance Rate within the meaning of the Credit Contracts\n"
			+ "Act 1981(if applicable). Interest Rates are subject to change.\n"
			+ "Transactions processed outside normal business hours may not appear on your Statement until the next business day, although they\n"
			+ "will appear immediately on your available balance.\n"
			+ "CONTACT CENTRE 0800 803 804 ASB Bank Limited, PO Box 35, Shortland Street, Auckland 1140, New Zealand www.asb.co.nz";

	@GetMapping("/")
	public ResponseEntity<?> getPdf() {
		try (PDDocument document = PDDocument
				.load(new File("C:\\Users\\Nimap\\Downloads\\FY23 - INVISION 12-3287-0015413-00 (1).pdf"))) {
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setSortByPosition(true);
			String text = stripper.getText(document);
			text = removeFlagContent(text);
//			System.out.println(text);

			// Convert text to JSON
			String json = convertTextToJSON(text);
//			System.out.println(json);
			return ResponseEntity.ok(json);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

//	public static String convertTextToJSON(String text) {
//		text = text.replaceAll("(?m)^.*Firefox https.*$", "");
//		text = text.replace("\"\\\\d{2}/\\\\d{2}/\\\\d{4}, \\\\d{2}:\\\\d{2} [ap]m\"", "");
//		String[] lines = text.split("\\r?\\n");
//		StringBuilder jsonBuilder = new StringBuilder("{\n");
//		for (String line : lines) {
//			String[] keyValue = line.split(":", 2);
//			if (keyValue.length == 2) {
//				String key = keyValue[0].trim();
//				String value = keyValue[1].trim();
//				jsonBuilder.append("\"").append(key).append("\": \"").append(value).append("\",\n");
//			}
//		}
//		// Remove trailing comma and newline
//		if (jsonBuilder.length() > 2) {
//			jsonBuilder.setLength(jsonBuilder.length() - 2);
//		}
//		jsonBuilder.append("\n}");
//		return jsonBuilder.toString();
//	}

	public static String convertTextToJSON(String text) {

		String accountNumber = "";
		String fromDate = "";
		String toDate = "";
		String name = "";
		String address = "";

		List<String> transactions = new ArrayList<>();
		boolean tableStarted = false;

		// Split the text into lines
		String[] lines = text.split("\\r?\\n");

		for (String line : lines) {
			if (line.startsWith("Account Number:")) {
				accountNumber = line.split(":")[1].trim();
			} else if (line.startsWith("From Date:")) {
				fromDate = line.split(":")[1].trim();
			} else if (line.startsWith("To Date:")) {
				toDate = line.split(":")[1].trim();
			} else if (line.contains("Name:")) {
				name = line.split(":")[1].trim();
			} else if (line.contains("Address:")) {
				address = line.split(":")[1].trim();
			} else if (line.matches("\\d{1,2}\\s+[A-Za-z]{3}\\s+\\d{4}\\s+.+")) { // This line
//	  should contain transaction data
				transactions.add(line);
				tableStarted = true;
			} else if (tableStarted) {
				transactions.add(line);
			}
		}

		StringBuilder jsonBuilder = new StringBuilder("{\n");
		jsonBuilder.append("\"Account Number\": \"").append(accountNumber).append("\",\n");
		jsonBuilder.append("\"From Date\": \"").append(fromDate).append("\",\n");
		jsonBuilder.append("\"To Date\": \"").append(toDate).append("\",\n");
		jsonBuilder.append("\"Name\": \"").append(name).append("\",\n");
		jsonBuilder.append("\"Address\": \"").append(address).append("\",\n");
		jsonBuilder.append("\"Transactions\": [\n");

		for (String transaction : transactions) {
			String[] parts = transaction.split("\\s+", 7); // Limit split to 7 parts
			String transactionDate = "";
			String transactionDescription = "";
			String debit = ""; // Default to empty string
			String credit = ""; // Default to empty string
			String balance = ""; // Default to empty string

			if (parts.length >= 3) {
				transactionDate = parts[0] + " " + parts[1] + " " + parts[2];
			}
			if (parts.length >= 4) {
				transactionDescription = parts[3];
			}
			if (parts.length >= 5 && !parts[4].equals("null")) { // Check if not null
				debit = parts[4];
			}
			if (parts.length >= 6 && !parts[5].equals("null")) { // Check if not null
				credit = parts[5];
			}
			if (parts.length >= 7 && !parts[6].equals("null")) { // Check if not null
				balance = parts[6];
			}

			String jsonTransaction = "{" + "\"Date\": \"" + transactionDate + "\", " + "\"Transaction Description\": \""
					+ transactionDescription + "\", " + "\"Debit/Cheque\": \"" + debit + "\", "
					+ "\"Credit/Deposit\": \"" + credit + "\", " + "\"Balance\": \"" + balance + "\"" + "}";
			jsonBuilder.append(jsonTransaction).append(",\n");
		}

		// Remove the trailing comma and newline
		if (!transactions.isEmpty()) {
			jsonBuilder.setLength(jsonBuilder.length() - 2);
		}
		jsonBuilder.append("\n]}");

		return jsonBuilder.toString();

	}

	/*
	 * public static String convertTextToJSON(String text) { // Parse header
	 * information String accountNumber = ""; String fromDate = ""; String toDate =
	 * ""; String name = ""; String address = "";
	 * 
	 * // Parse table data List<String> transactions = new ArrayList<>(); boolean
	 * tableStarted = false; boolean skipNextLines = false; // Flag to skip lines
	 * containing the specified text
	 * 
	 * // Split the text into lines String[] lines = text.split("\\r?\\n");
	 * 
	 * for (String line : lines) { if (line.startsWith("Account Number:")) {
	 * accountNumber = line.split(":")[1].trim(); } else if
	 * (line.startsWith("From Date:")) { fromDate = line.split(":")[1].trim(); }
	 * else if (line.startsWith("To Date:")) { toDate = line.split(":")[1].trim(); }
	 * else if (line.contains("Name:")) { name = line.split(":")[1].trim(); } else
	 * if (line.contains("Address:")) { address = line.split(":")[1].trim(); } else
	 * if (line.matches("\\d{1,2}\\s+[A-Za-z]{3}\\s+\\d{4}\\s+.+")) { // This line
	 * should contain transaction data if (!skipNextLines) { transactions.add(line);
	 * } tableStarted = true; } else if (tableStarted &&
	 * !line.contains("Transactions processed outside normal business hours")) { //
	 * This line is part of the transaction table and does not contain the specified
	 * // text if (!skipNextLines) { transactions.add(line); } } else if
	 * (line.contains("Transactions processed outside normal business hours")) { //
	 * Stop skipping lines if the specified text is encountered skipNextLines =
	 * false; } }
	 * 
	 * // Build JSON string StringBuilder jsonBuilder = new StringBuilder("{\n");
	 * jsonBuilder.append("\"Account Number\": \"").append(accountNumber).append(
	 * "\",\n");
	 * jsonBuilder.append("\"From Date\": \"").append(fromDate).append("\",\n");
	 * jsonBuilder.append("\"To Date\": \"").append(toDate).append("\",\n");
	 * jsonBuilder.append("\"Name\": \"").append(name).append("\",\n");
	 * jsonBuilder.append("\"Address\": \"").append(address).append("\",\n");
	 * jsonBuilder.append("\"Transactions\": [\n");
	 * 
	 * for (String transaction : transactions) { String[] parts =
	 * transaction.split("\\s+", 7); // Limit split to 7 parts String
	 * transactionDate = ""; String transactionDescription = ""; String debit = "";
	 * String credit = ""; String balance = "";
	 * 
	 * if (parts.length >= 3) { transactionDate = parts[0] + " " + parts[1] + " " +
	 * parts[2]; } if (parts.length >= 4) { transactionDescription = parts[3]; } if
	 * (parts.length >= 5) { debit = parts[4].isEmpty() ? "" : parts[4]; // Handling
	 * empty debit } if (parts.length >= 6) { credit = parts[5]; } if (parts.length
	 * >= 7) { balance = parts[6]; }
	 * 
	 * // Create JSON object for the transaction String jsonTransaction = "{" +
	 * "\"Date\": \"" + transactionDate + "\", " + "\"Transaction Description\": \""
	 * + transactionDescription + "\", " + "\"Debit/Cheque\": \"" + debit + "\", " +
	 * "\"Credit/Deposit\": \"" + credit + "\", " + "\"Balance\": \"" + balance +
	 * "\"" + "}"; jsonBuilder.append(jsonTransaction).append(",\n"); }
	 * 
	 * // Remove the trailing comma and newline if (!transactions.isEmpty()) {
	 * jsonBuilder.setLength(jsonBuilder.length() - 2); }
	 * jsonBuilder.append("\n]}");
	 * 
	 * return jsonBuilder.toString(); }
	 */

	private static String removeFlagContent(String text) {
		// Define the regex pattern to match the flag content
		String regex = "\\*\\s+The exchange rate selected by Visa[\\s\\S]*?www\\.asb\\.co\\.nz";

		// Remove the flag content using regex
		return text.replaceAll(regex, "");
	}

}
