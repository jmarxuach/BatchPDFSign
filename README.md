# BatchPDFSign

![Java CI with Maven](https://github.com/jmarxuach/BatchPDFSign/workflows/Java%20CI%20with%20Maven/badge.svg)

Command line tool for digital signature of PDF files, useful for example in Batch processes, from non Java programming languages like Php, Shell scripts, etc...

BatchPDFSign is a command line to sign PDF file with a PKCS12 certificate or with a PKCS11 Hardware Token.

To use it you need:

- a PKCS12 certificate. It should be a `<filename>`.pfx or `<filename>`.p12 file.
- a password for the certificate
- and a PDF file to sign.

For PKCS11 version you need:

- a PKCS11 config file. `name=MyToken\nlibrary=/path/to/lib.so\n`
- a PIN/password for the certificate
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

```
usage: BatchPDFSignPortable
 -m,--mode <arg>       pkcs12 or pkcs11 (defaults to pkcs12)
 -i,--input <arg>      input file path
 -k,--key <arg>        key file path or pkcs11 config file
 -o,--output <arg>     output file
 -p,--password <arg>   keyfile password or PIN
    --page <arg>       page of signature rectangle; needs to be specified
                       to output signature rectangle
    --fs <arg>         font size of text in signature rectangle (default:
                       12); needs --page to be specified as well
    --rh <arg>         height of signature rectangle; needs --page to be
                       specified as well
    --rw <arg>         width of signature rectangle; needs --page to be
                       specified as well
    --rx <arg>         x position of signature rectangle; needs --page to
                       be specified as well
    --ry <arg>         y position of signature rectangle; needs --page to
                       be specified as well
    --signtext <arg>   signature text; needs --page to be specified as
    --tsa <arg>        URI of the time service authority (TSA) 
    --reason <arg>     Reason field of signature
    --location <arg>   Location field of signature
```

### Mode

PKCS mode operation `pkcs12` or `pkcs11`. Defaults to `pkcs12`.

### key file path

This parameter is the certificate you want to sign the pdf with. It can be generated with the code documented in the chapter self signed certificate creation.

For `pkcs11` mode, this file is the config file of the HSM/Token ex: `name=MyToken\nlibrary=/path/to/lib.so\n`

### password

This parameter is the password for the certificate. The password is set during the creation of the certificate file.

For `pkcs11` mode, this is the PIN/Password for the certificate.

### input file path

The file you want to sign.

### output file

If this parameter is set, a new file with this name will be created and signed. The original file will remain untouched.

### visible signature

By default, the signature will not be (easily) visible in the final PDF file. If you want to make it easier for users to see, and with that and some GUIs easier to check the signature, you have to specify the location and size of the "signature rectangle". You also have the option to change the font size and to specify your own text.

- --page
  this option is required if you want the signature to appear. If you give this option, you will also have to specify --rx, --ry, --rw and --rh.
- --rx and --ry
  specify the location of the signature rectangle
- --rw and --rh
  specify the width and height of the signature rectangle
- --fs
  specify the font size of the text within the signature rectangle; 12 by default
- --signtext
  override the standard text provided by the signature-library with your own, provided text
- --reason
  Reason field of the signature
- --location
  Location field of the signature

## Development

You'll need:

- Maven
- Java 11 JDK

That's all folks....
