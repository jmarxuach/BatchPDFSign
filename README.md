# BatchPDFSign

![Maven Package](https://github.com/jmarxuach/BatchPDFSign/workflows/Maven%20Package/badge.svg)
![Java CI with Maven](https://github.com/jmarxuach/BatchPDFSign/workflows/Java%20CI%20with%20Maven/badge.svg)


Command line tool for digital signature of PDF files, useful for example in Batch processes, from non Java programming languages like Php, Shell scripts, etc...
 
BatchPDFSign is a command line to sign PDF file with a PKCS12 certificate.

To use it you need: 
- a PKCS12 certificate. It should be a <filename>.pfx file.
- a password for the .pfx
- and a PDF file to sign.

## self signed certificate creation
You can create your own self signed certificate with this following 4 commands in Ubuntu.

```bash
openssl genrsa -aes128 -out myself.key 2048
openssl req -new -days 365 -key myself.key -out myself.csr
openssl x509 -in myself.csr -out myself.crt -req -signkey myself.key -days 365
openssl pkcs12 -export -out myself.pfx -inkey myself.key -in myself.crt
```

## Signing
### Synopsis
required options: i, k, p  
usage: BatchPDFSign  
 -i,--input <arg>      input file path  
 -k,--key <arg>        key file path  
 -o,--output <arg>     output file  
 -p,--password <arg>   keyfile password  

### key file path
This parameter is the certificate you want to sign the pdf with. It can be generated with the code documented in the chapter self signed certificate creation.

### password
This parameter is the password for the certificate. The password is set during the creation of the certificate file.

### input file path
The file you want to sign.

### output file
If this parameter is set, a new file with this name will be created and signed. The original file will remain untouched.

## Development
You'll need:
- Maven
- Java 8 JDK

That's all folks.
