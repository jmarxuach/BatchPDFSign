# BatchPDFSign

Command line tool for digital signature of PDF files, useful for example in Batch processes, from non Java programming languages like Php, Shell scripts, etc...
 
BatchPDFSign is a command line to sign PDF file with a PKCS12 certificate.
You need a PKCS12 certificate. It should be a <filename>.pfx file.
You need a password for the .pfx
A PDF file to sign.

You can create your own self signed certificate with this following 4 commands in ubuntu. Release includes this certificate with password <12345>.

<code>openssl genrsa -aes128 -out myself.key 2048</code>
<code>openssl req -new -days 365 -key myself.key -out myself.csr</code>
<code>openssl x509 -in myself.csr -out myself.crt -req -signkey myself.key -days 365</code>
<code>openssl pkcs12 -export -out myself.pfx -inkey myself.key -in myself.crt</code>

Then you can sign a PDF file with following command line.

<code>java -jar BatchPDFSign.jar <certificate.pfx> "password" "filetosign.pdf"</code>

For the example included in the releases.

<code>java -jar BatchPDFSign.jar myself.pfx "12345" "test.pdf"</code>

That's all folks.
