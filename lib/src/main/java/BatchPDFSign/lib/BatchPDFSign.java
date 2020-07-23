package BatchPDFSign.lib;

import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
import org.apache.commons.io.FileUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

/**
 * Signs PDF files and retains PDF/A conformity.
 * @author Pep Marxuach, jmarxuach
 * @author Joe Meier, Jocomol, joelmeier08@gmail.com
 * @version 1.0.5.1
 */
public class BatchPDFSign {

	private static PrivateKey privateKey;
	private static Certificate[] certificateChain;

	private final String pkcs12FileName;
	private final String PkcsPassword;
	private final String pdfInputFileName;
	private final String pdfOutputFileName;
	private final boolean flgRename;
	private final File inputFile;

	/**
	 * This is the constructor
	 * Sets all the class variables.
	 * If pdfOutputFileName is null, the pdfOutputFileName class variable is set to pdfInputFileName + "-sig"
	 * @author Joe Meier, Jocomol, joelmeier08@gmail.com
	 * @param pkcs12FileName File name of the key.
	 * @param PkcsPassword Password of the key.
	 * @param pdfInputFileName File name of the PDF file which should be signed.
	 * @param pdfOutputFileName File name which the signed PDF should have.
	 */
	public BatchPDFSign(String pkcs12FileName, String PkcsPassword, String pdfInputFileName, String pdfOutputFileName){
		this.pkcs12FileName = pkcs12FileName;
		this.PkcsPassword = PkcsPassword;
		this.pdfInputFileName = pdfInputFileName;
		this.inputFile = new File(pdfInputFileName);
		this.flgRename = pdfOutputFileName == null;
		if (! this.flgRename){
			this.pdfOutputFileName = pdfOutputFileName;
		} else {
			this.pdfOutputFileName = pdfInputFileName + "-sig";
		}
	}

	/**
	 * Signs a PDF file. This method is configured by the constructors of this class.
	 * @author Pep Marxuach, jmarxuach
	 * @author Joe Meier, Jocomol, joelmeier08@gmail.com
	 * @throws IOException A File couldn't be opened.
	 * @throws GeneralSecurityException Some permissions aren't right.
	 */
	public void signFile() throws IOException, GeneralSecurityException {

		// Check PDF input file
		if (!inputFile.exists() || inputFile.isDirectory()) {
			throw new FileNotFoundException("File: " + this.inputFile + " wasn't found");
		}
		readPrivateKeyFromPKCS12(pkcs12FileName, PkcsPassword);
		PdfReader reader = new PdfReader(pdfInputFileName);
		ITSAClient tsaClient = new TSAClientBouncyCastle("https://freetsa.org/tsr");
		StampingProperties properties = new StampingProperties().preserveEncryption();
		PdfSigner signer = new PdfSigner(reader, new FileOutputStream(pdfOutputFileName), properties);
		IExternalDigest digest = new BouncyCastleDigest();
		BouncyCastleProvider provider = new BouncyCastleProvider();
		Security.addProvider(provider);
		IExternalSignature signature = new PrivateKeySignature(privateKey, DigestAlgorithms.SHA256, provider.getName());
		signer.signDetached(digest, signature, certificateChain, null, null, tsaClient, 0, PdfSigner.CryptoStandard.CMS);

		// Renaming signed PDF file
		if (flgRename) {
			if (this.inputFile.delete())
			{
				FileUtils.moveFile(FileUtils.getFile(pdfOutputFileName), FileUtils.getFile(pdfInputFileName));
			} else {
				throw new IOException("File: " + this.inputFile + " couldn't be deleted");
			}
		}
	}

	/**
	 * Loads the signer key from the file.
	 * @author Pep Marxuach, jmarxuach
	 * @author Joe Meier, Jocomol, joelmeier08@gmail.com
	 * @param pkcs12FileName File name of the key file.
	 * @param pkcs12Password Password to unlock the key file.
	 */
	protected static void readPrivateKeyFromPKCS12 (String pkcs12FileName, String pkcs12Password) throws KeyStoreException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException {
		KeyStore ks = KeyStore.getInstance("pkcs12");
		ks.load(new FileInputStream(pkcs12FileName), pkcs12Password.toCharArray());
		String alias = ks.aliases().nextElement();
		privateKey = (PrivateKey) ks.getKey(alias, pkcs12Password.toCharArray());
		certificateChain = ks.getCertificateChain(alias);
	}
}
