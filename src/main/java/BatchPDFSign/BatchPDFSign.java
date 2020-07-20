package BatchPDFSign;

import com.itextpdf.kernel.pdf.PdfAConformanceLevel;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.signatures.*;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

/**
 * Signs PDF files and retains PDF/A conformity if wanted.
 * @author Pep Marxuach, jmarxuach
 * @author Joe Meier, Jocomol, joelmeier08@gmail.com
 * @version 1.0.2
 */
public class BatchPDFSign {

	private static PrivateKey privateKey;
	private static Certificate[] certificateChain;

	private final String pkcs12FileName;
	private final String PkcsPassword;
	private final String pdfInputFileName;
	private String pdfOutputFileName;

	private boolean flgRename;
	private boolean pdfA;

	private PdfAConformanceLevel pdfAConformanceLevel;

	private final File inputFile;

	/**
	 * Constructor for when the jar is executed directly.
	 * @author Joe Meier, Jocomol, joelmeier08@gmail.com
	 * @param args arguments received from the commandline.
	 */
	public BatchPDFSign(String[] args){
		this(args[0], args[1], args[2]);
		this.flgRename = !(args.length >= 4);
		this.pdfA = args.length == 5;
		if(flgRename){
			this.pdfOutputFileName = pdfInputFileName + "-sig";
		} else {
			this.pdfOutputFileName = args[3];
		}
		if(this.pdfA){
			this.setPDFA(args[4]);
		}
	}

	/**
	 * The most basic constructor, only initializes the file name, the file password and the pdf which should be signed class variable.
	 * This constructor is called by every other constructor.
	 * @author Joe Meier, Jocomol, joelmeier08@gmail.com
	 * @param pkcs12FileName File name of the key.
	 * @param PkcsPassword Password of the key.
	 * @param pdfInputFileName File name of the PDF file which should be signed.
	 */
	public BatchPDFSign(String pkcs12FileName, String PkcsPassword, String pdfInputFileName){
		this.pkcs12FileName = pkcs12FileName;
		this.PkcsPassword = PkcsPassword;
		this.pdfInputFileName = pdfInputFileName;
		this.inputFile = new File(pdfInputFileName);
		this.pdfOutputFileName = pdfInputFileName + "-sig";
		this.flgRename = true;
	}

	/**
	 * The constructor which is called if the PDF file which is signed, shouldn't be overwritten.
	 * This constructor calls the basic constructor.
	 * @author Joe Meier, Jocomol, joelmeier08@gmail.com
	 * @param pkcs12FileName File name of the key.
	 * @param PkcsPassword Password of the key.
	 * @param pdfInputFileName File name of the PDF file which should be signed.
	 * @param pdfOutputFileName File name which the signed PDF should have.
	 */
	public BatchPDFSign(String pkcs12FileName, String PkcsPassword, String pdfInputFileName, String pdfOutputFileName){
		this(pkcs12FileName, PkcsPassword, pdfInputFileName);
		this.pdfOutputFileName = pdfOutputFileName;
		this.flgRename = false;
	}

	/**
	 * The PDF/A Constructor, only works when a output file is defined aswell.
	 * This constructor calls the (String, String, String, String) constructor.
	 * @author Joe Meier, Jocomol, joelmeier08@gmail.com
	 * @param pkcs12FileName File name of the key.
	 * @param PkcsPassword Password of the key.
	 * @param pdfInputFileName File name of the PDF file which should be signed.
	 * @param pdfOutputFileName File name of the PDF file which should be signed.
	 * @param pdfAFormat Name of the PDF/A format.
	 */
	public BatchPDFSign(String pkcs12FileName, String PkcsPassword, String pdfInputFileName, String pdfOutputFileName, String pdfAFormat){
		this(pkcs12FileName, PkcsPassword, pdfInputFileName, pdfOutputFileName);
		this.pdfA = true;
		this.setPDFA(pdfAFormat);
	}

	/**
	 * Sets the class variable pdfAConformanceLevel to the conformance level defined in the parameter pdfAFormat.
	 * @author Joe Meier, Jocomol, joelmeier08@gmail.com
	 * @param pdfAFormat Name of the conformance level the class variable pdfAConformanceLevel should be set to.
	 * @throws IllegalArgumentException Is thrown if a non valid PDF/A format is given.
	 */
	public void setPDFA(String pdfAFormat) throws IllegalArgumentException{
		switch (pdfAFormat) {
			case "PDF_A_1A" -> this.pdfAConformanceLevel = PdfAConformanceLevel.PDF_A_1A;
			case "PDF_A_1B" -> this.pdfAConformanceLevel = PdfAConformanceLevel.PDF_A_1B;
			case "PDF_A_2A" -> this.pdfAConformanceLevel = PdfAConformanceLevel.PDF_A_2A;
			case "PDF_A_2B" -> this.pdfAConformanceLevel = PdfAConformanceLevel.PDF_A_2B;
			case "PDF_A_2U" -> this.pdfAConformanceLevel = PdfAConformanceLevel.PDF_A_2U;
			case "PDF_A_3A" -> this.pdfAConformanceLevel = PdfAConformanceLevel.PDF_A_3A;
			case "PDF_A_3B" -> this.pdfAConformanceLevel = PdfAConformanceLevel.PDF_A_3B;
			case "PDF_A_3U" -> this.pdfAConformanceLevel = PdfAConformanceLevel.PDF_A_3U;
			default -> throw new IllegalArgumentException();
		}
	}

	/**
	 * Signs a PDF file. This method is configured by the constructors of this class.
	 * @author Pep Marxuach, jmarxuach
	 * @author Joe Meier, Jocomol, joelmeier08@gmail.com
	 */
	public void signFile() {
		try{
			// Check PDF input file
			if (!inputFile.exists() || inputFile.isDirectory()) {
				throw new FileNotFoundException();
			}
			readPrivateKeyFromPKCS12(pkcs12FileName, PkcsPassword);
			PdfReader reader = new PdfReader(pdfInputFileName);
			FileOutputStream fout = new FileOutputStream(pdfOutputFileName);
			TSAClient tsaClient = new TSAClientBouncyCastle("https://freetsa.org/tsr");
			PdfSignatureAppearance sap;
			if (this.pdfA){
				PdfAStamper stp = PdfAStamper.createSignature(reader, fout, '\0', this.pdfAConformanceLevel);
				sap = stp.getSignatureAppearance();
			}else{
				PdfStamper stp = PdfAStamper.createSignature(reader, fout, '\0');
				sap = stp.getSignatureAppearance();
			}
			ExternalDigest digest = new BouncyCastleDigest();
			BouncyCastleProvider provider = new BouncyCastleProvider();
			Security.addProvider(provider);
			ExternalSignature signature = new PrivateKeySignature(privateKey, DigestAlgorithms.SHA256, provider.getName());
			MakeSignature.signDetached(sap, digest, signature, certificateChain, null, null, tsaClient, 0, MakeSignature.CryptoStandard.CMS);

			// Renaming signed PDF file
			if (flgRename) {
				if (this.inputFile.delete())
				{
					FileUtils.moveFile(FileUtils.getFile(pdfOutputFileName), FileUtils.getFile(pdfInputFileName));
				} else {
					throw new FileNotFoundException();
				}
			}
		} catch (IOException e){
			errorHandling(e, "The PDF file wasn't found or wasn't readable. Please check if you entered the correct file, if it exists and if the permissions are set correctly.", false, true);
		} catch (GeneralSecurityException e) {
			errorHandling(e, "A fatal error occurred, please contact the developer with the following details:", true, false);
		}
	}

	/**
	 * Loads the signer key from the file.
	 * @author Pep Marxuach, jmarxuach
	 * @author Joe Meier, Jocomol, joelmeier08@gmail.com
	 * @param pkcs12FileName File name of the key file.
	 * @param pkcs12Password Password to unlock the key file.
	 */
	protected static void readPrivateKeyFromPKCS12 (String pkcs12FileName, String pkcs12Password)  {
		try {
			KeyStore ks = KeyStore.getInstance("pkcs12");
			ks.load(new FileInputStream(pkcs12FileName), pkcs12Password.toCharArray());
			String alias = ks.aliases().nextElement();
			privateKey = (PrivateKey) ks.getKey(alias, pkcs12Password.toCharArray());
			certificateChain = ks.getCertificateChain(alias);
		}
		catch (UnrecoverableKeyException e){
			errorHandling(e, "The Key could not be recovered. The password was probably entered incorrectly", false, true);
		} catch (KeyStoreException | CertificateException | NoSuchAlgorithmException e){
			errorHandling(e, "Key has an invalid signature. Please check if you entered the correct file.", false, true);
		} catch (IOException e) {
			errorHandling(e, "Key file wasn't found or wasn't readable. Please check if you entered the correct file, if it exists and if the permissions are set correctly.", false, true);
		}
	}

	/**
	 * Handles errors. can print stacktrace, usage and error messages.
	 * This Method is configurable by booleans.
	 * @author Joe Meier, Jocomol, joelmeier08@gmail.com
	 * @param e Exception.
	 * @param errorMessage Error message for the user.
	 * @param printStacktrace Should it print the stacktrace?
	 * @param showUsage Should it call the showUsage() method?
	 */
	public static void errorHandling(Exception e, String errorMessage, Boolean printStacktrace, Boolean showUsage){
		System.err.println(errorMessage);
		if (e != null && printStacktrace){
			e.printStackTrace();
		}
		if (showUsage){
			showUsage();
		}
	}
	/**
	 * Basic help and usage documentation.
	 * @author Pep Marxuach, jmarxuach
	 * @author Joe Meier, Jocomol, joelmeier08@gmail.com
	 */
	public static void showUsage() {
		System.out.println("java -jar BatchPDFSign.jar certificate.pfx password filetosign.pdf [outputfile.pdf] [PDFAFormat]");
	}
}
