package BatchPDFSign;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.util.NoSuchElementException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSignatureAppearance;
import com.lowagie.text.pdf.PdfStamper;
import org.apache.commons.io.FileUtils;


public class BatchPDFSign {

	private static PrivateKey privateKey;
	private static Certificate[] certificateChain;

	private static String PRODUCTNAME = "BatchPDFSign";
	private static String VERSION = "version 1.0.1";
	private static String JAR_FILENAME = "JPdfSign.jar";

	public static void main(String[] args) {

		if (args.length < 3)
			showUsage();

		try {

			boolean flgRename = true;

			String pkcs12FileName = args[0].trim();

			String PkcsPassword = args[1];

			String pdfInputFileName = args[2];
			String pdfOutputFileName = pdfInputFileName + "_signed.pdf";
			if (args.length == 4) {
				pdfOutputFileName = args[3];
				flgRename = false;
			}
			// PDF input file
			File InputFile = new File(pdfInputFileName);
			// Check PDF input file
			if (!InputFile.exists() || InputFile.isDirectory()) {
				System.out.println("");
				System.out.println("PDf file not found !");
				showUsage();
			}

			readPrivateKeyFromPKCS12(pkcs12FileName, PkcsPassword);

			PdfReader reader = null;
			try {
				reader = new PdfReader(pdfInputFileName);
			} catch (IOException e) {
				System.err.println(
						"An unknown error accoured while opening the input PDF file: \"" + pdfInputFileName + "\"");
				e.printStackTrace();
				System.exit(-1);
			}
			FileOutputStream fout = null;
			try {
				fout = new FileOutputStream(pdfOutputFileName);
			} catch (FileNotFoundException e) {
				System.err.println(
						"An unknown error accoured while opening the output PDF file: \"" + pdfOutputFileName + "\"");
				e.printStackTrace();
				System.exit(-1);
			}
			PdfStamper stp;
			try {
				stp = PdfStamper.createSignature(reader, fout, '\0');
				PdfSignatureAppearance sap = stp.getSignatureAppearance();
				sap.setCrypto(privateKey, certificateChain, null, PdfSignatureAppearance.WINCER_SIGNED);
				stp.close();
			} catch (Exception e) {
				System.err.println("An unknown error accoured while signing the PDF file:");
				e.printStackTrace();
				System.exit(-1);
			}
			InputFile.delete();
			// Renaming signed PDF file
			if (flgRename) {
				try {
					FileUtils.moveFile(
						      FileUtils.getFile(pdfOutputFileName),
						      FileUtils.getFile(pdfInputFileName));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.err.println("Error renaming output file from " + pdfOutputFileName + " to " + pdfInputFileName );
					e.printStackTrace();

				}
			}

		} catch (KeyStoreException kse) {
			System.err.println("An unknown error accoured while initializing the KeyStore instance:");
			kse.printStackTrace();
			System.exit(-1);
		}
	}
	/**
	 * Reads private Key
	 * @param pkcs12FileName
	 * @throws KeyStoreException
	 */
	protected static void readPrivateKeyFromPKCS12(String pkcs12FileName, String pkcs12Password) throws KeyStoreException {

		KeyStore ks = null;

		try {
			ks = KeyStore.getInstance("pkcs12");
			ks.load(new FileInputStream(pkcs12FileName), pkcs12Password.toCharArray());
		} catch (NoSuchAlgorithmException e) {
			System.err.println("An unknown error accoured while reading the PKCS#12 file (Password could be wrong):");
			e.printStackTrace();
			System.exit(-1);
		} catch (CertificateException e) {
			System.err.println("An unknown error accoured while reading the PKCS#12 file:");
			e.printStackTrace();
			System.exit(-1);
		} catch (FileNotFoundException e) {
			System.err.println("Unable to open the PKCS#12 keystore file \"" + pkcs12FileName + "\":");
			System.err.println("The file does not exists or missing read permission.");
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("An unknown error accoured while reading the PKCS#12 file: IOException");
			e.printStackTrace();
			System.exit(-1);
		}
		String alias = "";
		try {
			alias = (String) ks.aliases().nextElement();
			privateKey = (PrivateKey) ks.getKey(alias, pkcs12Password.toCharArray());
		} catch (NoSuchElementException e) {
			System.err.println("An unknown error accoured while retrieving the private key:");
			System.err.println("The selected PKCS#12 file does not contain any private keys.");
			e.printStackTrace();
			System.exit(-1);
		} catch (NoSuchAlgorithmException e) {
			System.err.println("An unknown error accoured while retrieving the private key:");
			e.printStackTrace();
			System.exit(-1);
		} catch (UnrecoverableKeyException e) {
			System.err.println("An unknown error accoured while retrieving the private key:");
			e.printStackTrace();
			System.exit(-1);
		}
		certificateChain = ks.getCertificateChain(alias);
	}
	/**
	 * Message shown when error in parameters
	 */
	public static void showUsage() {
		System.out.println("jPdfSign v" + VERSION + " \n");
		System.out.println(PRODUCTNAME + " usage:");
		System.out.println("\nFor using a PKCS#12 (.p12) file as signature certificate and private key source:");
		System.out.print("\tjava -jar " + JAR_FILENAME);
		System.out.println(" <pkcs12FileName> <pkcsPassword> <pdfInputFileName> <pdfOutputFileName>");
		System.out.println("\n<pdfOutputFileName> is optional");
		System.exit(0);
	}

}