# BatchPDFSign

Command line tool for digital signature of PDF files, useful for example in Batch processes, from non Java programming languages like Php, Shell scripts, etc...
 
BatchPDFSign is a command line to sign PDF file with a PKCS12 certificate.
You need a PKCS12 certificate. It should be a <filename>.pfx file.
You need a password for the .pfx
A PDF file to sign.

## self signed certificate creation
You can create your own self signed certificate with this following 4 commands in Ubuntu.

```bash
openssl genrsa -aes128 -out myself.key 2048
openssl req -new -days 365 -key myself.key -out myself.csr
openssl x509 -in myself.csr -out myself.crt -req -signkey myself.key -days 365
openssl pkcs12 -export -out myself.pfx -inkey myself.key -in myself.crt
```

## Signing
Example:
```bash
java -jar BatchPDFSign.jar myCertificate.pfx mySecurePassword myPdfFile.pdf outputfile.pdf PDF_A_1A
```
### Synopsis
**java -jar BatchPDFSign.jar** _certificate.pfx_ _password_ _filetosign.pdf_ \[outputfile.pdf] \[PDFAFormat]  

- **type exactly as shown**
- _replace with appropriate argument_
- \[optional]

### certificate
This parameter is the certificate you want to sign the pdf with. It can be generated with the code documented in the chapter self signed certificate creation.

### password
This parameter is the password for the certificate. The password is set during the creation of the certificate file.

### filetosign
The file you want to sign.

### outputfile
If this parameter is set, a new file with this name will be created and signed. The original file will remain untouched.

### PDFAFormat 
The PDF/A Format of the file, if this flag is set, the flag "outputfile" also needs to be set.
More information can be found in the chapter PDF/A.

## PDF/A
Following PDF/A formats are supported:
- PDF_A_1A 
- PDF_A_1B 
- PDF_A_2A 
- PDF_A_2B 
- PDF_A_2U 
- PDF_A_3A 
- PDF_A_3B 
- PDF_A_3U 
- ZUGFeRD
- ZUGFeRDBasic
- ZUGFeRDComfort
- ZUGFeRDExtended

## Development
You'll need:
- Maven
- Java 14 JDK

That's all folks.
