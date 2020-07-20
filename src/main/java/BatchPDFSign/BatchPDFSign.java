package BatchPDFSign;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.security.*;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;


public class BatchPDFSign {

	private static PrivateKey privateKey;
	private static Certificate[] certificateChain;

	private static String PRODUCTNAME = "BatchPDFSign";
	private static String VERSION = "version 1.0.1";
	private static String JAR_FILENAME = "BatchPDFSign.jar";

	private final String pkcs12FileName;
	private final String PkcsPassword;
	private final String pdfInputFileName;
	private String pdfOutputFileName;

	private boolean flgRename;
	private boolean pdfA;

	private PdfAConformanceLevel pdfAConformanceLevel;

	private final File inputFile;

	public BatchPDFSign(String[] args){
		this(args[0], args[1], args[2]);
		this.flgRename = !(args.length >= 4);
		this.pdfA = args.length == 5;
		if(flgRename){
			this.pdfOutputFileName = pdfInputFileName + "-sig";
		} else {
			this.setPdfOutputFileName(args[3]);
		}
		if(this.pdfA){
			this.setPDFA(args[4]);
		}
	}

	public BatchPDFSign(String pkcs12FileName, String PkcsPassword, String pdfInputFileName){
		this.pkcs12FileName = pkcs12FileName;
		this.PkcsPassword = PkcsPassword;
		this.pdfInputFileName = pdfInputFileName;
		this.inputFile = new File(pdfInputFileName);
		this.pdfOutputFileName = pdfInputFileName + "-sig";
		this.flgRename = true;
	}

	public BatchPDFSign(String pkcs12FileName, String PkcsPassword, String pdfInputFileName, String pdfOutputFileName){
		this(pkcs12FileName, PkcsPassword, pdfInputFileName);
		this.pdfOutputFileName = pdfOutputFileName;
		this.flgRename = false;
	}

	public BatchPDFSign(String pkcs12FileName, String PkcsPassword, String pdfInputFileName, String pdfOutputFileName, String pdfAFormat){
		this(pkcs12FileName, PkcsPassword, pdfInputFileName, pdfOutputFileName);
		this.pdfA = true;
		this.setPDFA(pdfAFormat);
	}

	public void setPDFA(String pdfAFormat){
		switch (pdfAFormat){
			case "PDF_A_1A": this.pdfAConformanceLevel = PdfAConformanceLevel.PDF_A_1A;
				break;
			case "PDF_A_1B": this.pdfAConformanceLevel = PdfAConformanceLevel.PDF_A_1B;
				break;
			case "PDF_A_2A": this.pdfAConformanceLevel = PdfAConformanceLevel.PDF_A_2A;
				break;
			case "PDF_A_2B": this.pdfAConformanceLevel = PdfAConformanceLevel.PDF_A_2B;
				break;
			case "PDF_A_2U": this.pdfAConformanceLevel = PdfAConformanceLevel.PDF_A_2U;
				break;
			case "PDF_A_3A": this.pdfAConformanceLevel = PdfAConformanceLevel.PDF_A_3A;
				break;
			case "PDF_A_3B": this.pdfAConformanceLevel = PdfAConformanceLevel.PDF_A_3B;
				break;
			case "PDF_A_3U": this.pdfAConformanceLevel = PdfAConformanceLevel.PDF_A_3U;
				break;
			case "ZUGFeRD": this.pdfAConformanceLevel = PdfAConformanceLevel.ZUGFeRD;
				break;
			case "ZUGFeRDBasic": this.pdfAConformanceLevel = PdfAConformanceLevel.ZUGFeRDBasic;
				break;
			case "ZUGFeRDComfort": this.pdfAConformanceLevel = PdfAConformanceLevel.ZUGFeRDComfort;
				break;
			case "ZUGFeRDExtended": this.pdfAConformanceLevel = PdfAConformanceLevel.ZUGFeRDExtended;
				break;
			default: throw new IllegalArgumentException();
		}
	}

	public void signFile() throws GeneralSecurityException, IOException, DocumentException {
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

		} catch (FileNotFoundException e){
			showUsage();
		}
	}

	protected static void readPrivateKeyFromPKCS12 (String pkcs12FileName, String pkcs12Password) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException {
		KeyStore ks = KeyStore.getInstance("pkcs12");
		ks.load(new FileInputStream(pkcs12FileName), pkcs12Password.toCharArray());
		String alias = ks.aliases().nextElement();
		privateKey = (PrivateKey) ks.getKey(alias, pkcs12Password.toCharArray());
		certificateChain = ks.getCertificateChain(alias);
	}
	public static void showUsage() { //TODO Update
		System.out.println("jPdfSign v" + VERSION + " \n");
		System.out.println(PRODUCTNAME + " usage:");
		System.out.println("\nFor using a PKCS#12 (.p12) file as signature certificate and private key source:");
		System.out.print("\tjava -jar " + JAR_FILENAME);
		System.out.println(" <pkcs12FileName> <pkcsPassword> <pdfInputFileName> <pdfOutputFileName>");
		System.out.println("\n<pdfOutputFileName> is optional");
		System.exit(0);
	}

	private void setPdfOutputFileName(String pdfOutputFileName) {
		this.pdfOutputFileName = pdfOutputFileName;
	}
}
