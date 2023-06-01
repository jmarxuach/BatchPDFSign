package BatchPDFSign.lib;

import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.StampingProperties;
import com.itextpdf.signatures.*;
import com.itextpdf.kernel.geom.Rectangle;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.Provider;
import java.security.Security;


/**
 * Signs PDF files and retains PDF/A conformity.
 * @author KernelPanic80, github.com/KernelPanic80
 * @author Pep Marxuach, jmarxuach
 * @author Joe Meier, Jocomol, joelmeier08@gmail.com
 * @version 1.0.5.2
 */
public class BatchPDFSignSmartCard {

	private static final String SUN_PKCS11_CLASSNAME = "sun.security.pkcs11.SunPKCS11";
	private static final String SUN_PKCS11_PROVIDER_NAME = "SunPKCS11";

	private static PrivateKey privateKey;
	private static Certificate[] certificateChain;
 	private static String defaultTsaUri = "https://freetsa.org/tsr";

	private final String pkcs11ConfFileName;
	private final String PkcsPassword;
	private final String pdfInputFileName;
	private final String pdfOutputFileName;
	private final boolean flgRename;
	private final File inputFile;

	/**
	 * This is the constructor
	 * Sets all the class variables.
	 * If pdfOutputFileName is null, the pdfOutputFileName class variable is set to pdfInputFileName + "-sig"
	 * @author KernelPanic80, github.com/KernelPanic80
	 * @author Joe Meier, Jocomol, joelmeier08@gmail.com
	 * @param pkcs11ConfFileName File name of PKCS11 conf file.
	 * @param PkcsPassword Password of the key (PIN).
	 * @param pdfInputFileName File name of the PDF file which should be signed.
	 * @param pdfOutputFileName File name which the signed PDF should have.
	 */
	public BatchPDFSignSmartCard(String pkcs11ConfFileName, String PkcsPassword, String pdfInputFileName, String pdfOutputFileName){
		this.pkcs11ConfFileName = pkcs11ConfFileName;
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
	 * @author KernelPanic80, github.com/KernelPanic80
	 * @author Pep Marxuach, jmarxuach
	 * @author Joe Meier, Jocomol, joelmeier08@gmail.com
	 * @throws IOException A File couldn't be opened.
	 * @throws GeneralSecurityException Some permissions aren't right.
	 */
	public void signFile(int page, float rx, float ry, float rw, float rh, float fs, String signtext) throws IOException, GeneralSecurityException {
      signFile(page, rx, ry, rw, rh, fs, signtext, null, null,null);
  }
	public void signFile(int page, float rx, float ry, float rw, float rh, float fs, String signtext, String tsaUri, String reason, String location) throws IOException, GeneralSecurityException {
    if(tsaUri == null) {
      tsaUri = defaultTsaUri;
    }
		// Check PDF input file
		if (!inputFile.exists() || inputFile.isDirectory()) {
			throw new FileNotFoundException("File: " + this.inputFile + " wasn't found");
		}
		Provider provider = readPrivateKeyFromPKCS11(pkcs11ConfFileName, PkcsPassword);
		PdfReader reader = new PdfReader(pdfInputFileName);
		ITSAClient tsaClient = new TSAClientBouncyCastle(tsaUri);
		StampingProperties properties = new StampingProperties().preserveEncryption();
		PdfSigner signer = new PdfSigner(reader, new FileOutputStream(pdfOutputFileName), properties);
		PdfSignatureAppearance appearance = signer.getSignatureAppearance();
		if (page > 0) {
			appearance.setPageNumber(page);
			appearance.setPageRect(new Rectangle(rx, ry, rw, rh));
			appearance.setLayer2FontSize(fs);
			if (signtext != null) {
				appearance.setLayer2Text(signtext);
			}
		}
		if(reason != null){
			appearance.setReason(reason);
		}
		if(location != null){
			appearance.setLocation(location);
		}
		IExternalDigest digest = new BouncyCastleDigest();
		IExternalSignature signature = new PrivateKeySignature(privateKey, DigestAlgorithms.SHA256, provider.getName());
		signer.signDetached(digest, signature, certificateChain, null, null, tsaClient, 0, PdfSigner.CryptoStandard.CADES);
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
	public void signFile(String tsaUri, String reason, String location) throws IOException, GeneralSecurityException {
		this.signFile(0, 0, 0, 0, 0, 10, "", tsaUri, reason, location);
	}
	public void signFile() throws IOException, GeneralSecurityException {
		this.signFile(null,null,null);
	}

	/**
	 * Instanciates and return a PKCS11 provider based on the config file, and loads the signer key from the smart card.
	 * 
	 * @author KernelPanic80, github.com/KernelPanic80
	 * @author Pep Marxuach, jmarxuach
	 * @author Joe Meier, Jocomol, joelmeier08@gmail.com
	 * @param pkcs11FileName File name of PKCS11 conf file.
	 * @param pkcs11Password Password/PIN to unlock the key file.
	 */
	protected static Provider readPrivateKeyFromPKCS11 (String pkcs11ConfFileName, String pkcs11Password) throws KeyStoreException, IOException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateException {
		Provider providerPKCS11;
		try {
			int javaVersion = BatchPDFSignSmartCard.getVersion();
			if (javaVersion >= 9) {
				Provider prototype = Security.getProvider(SUN_PKCS11_PROVIDER_NAME);
				Class<?> sunPkcs11ProviderClass = Class.forName(SUN_PKCS11_CLASSNAME);
				Method configureMethod = sunPkcs11ProviderClass.getMethod("configure", String.class);
				providerPKCS11 = (Provider) configureMethod.invoke(prototype, pkcs11ConfFileName);
			} else {
				Class<?> sunPkcs11ProviderClass = Class.forName(SUN_PKCS11_CLASSNAME);
				Constructor<?> constructor = sunPkcs11ProviderClass.getConstructor(String.class);
				providerPKCS11 = (Provider) constructor.newInstance(pkcs11ConfFileName);
			}
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException ex) {
			// Handle any error, log message or throw an exception
			ex.printStackTrace(System.out);
			throw new KeyStoreException(ex);
		}
		Security.addProvider(providerPKCS11);
		KeyStore ks = KeyStore.getInstance("PKCS11");
		ks.load(null, pkcs11Password.toCharArray());
		String alias = ks.aliases().nextElement();
		privateKey = (PrivateKey) ks.getKey(alias, pkcs11Password.toCharArray());
		certificateChain = ks.getCertificateChain(alias);
		return providerPKCS11;
	}

	/**
	 * Get current Java Version
	 * @return actual java version
	 */
    public static int getVersion() {
        String version = System.getProperty("java.version");
        if(version.startsWith("1.")) {
            version = version.substring(2, 3);
        } else {
            int dot = version.indexOf(".");
            if(dot != -1) { version = version.substring(0, dot); }
        } return Integer.parseInt(version);
    }
	
}
