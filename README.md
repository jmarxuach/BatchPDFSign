# BatchPDFSign

Command line tool for digital signature of PDF files, useful for example in Batch processes, from non Java programming languages like Php, Shell scripts, etc...
 
BatchPDFSign is a command line to sign PDF file with a PKCS12 certificate.
You need a PKCS12 certificate. It should be a <filename>.pfx file.
You need a password for the .pfx
A PDF file to sign.

You can create your own self signed certificate with this following 4 commands in ubuntu. Release includes this certificate with password <12345>.

```bash
openssl genrsa -aes128 -out myself.key 2048
```

```bash
openssl req -new -days 365 -key myself.key -out myself.csr
```

```bash
openssl x509 -in myself.csr -out myself.crt -req -signkey myself.key -days 365
```

```bash
openssl pkcs12 -export -out myself.pfx -inkey myself.key -in myself.crt
```

Then you can sign a PDF file with following command line.

```bash
java -jar BatchPDFSign.jar <certificate.pfx> <password> <filetosign.pdf>
```

With a specific output file.
```bash
java -jar BatchPDFSign.jar <certificate.pfx> <password> <filetosign.pdf> <outputfile.pdf>
```

For the example included in the releases.

```bash
java -jar BatchPDFSign.jar myself.pfx 12345 test.pdf</code>
```

With an output file defined.

```bash
java -jar BatchPDFSign.jar myself.pfx 12345 test.pdf test-sig.pdf
```


That's all folks.
