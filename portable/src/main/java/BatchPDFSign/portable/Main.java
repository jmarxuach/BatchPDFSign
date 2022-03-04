package BatchPDFSign.portable;

import BatchPDFSign.lib.BatchPDFSign;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Scanner;

/**
 * This method is called when the jar is executed. It shows the help if parameters weren't given correctly.
 * If the correct parameters were given, it creates a BatchPDFSign Object and calls the signFile(); function on it.
 * @author Joe Meier, Jocomol, joelmeier08@gmail.com
 * @version 1.0.5.2
 */
public class Main {
    public static void main(String[] args){

        Options options = new Options();

        Option input = new Option("i", "input", true, "input file path");
        input.setRequired(true);
        options.addOption(input);

        Option key = new Option("k", "key", true, "key file path");
        key.setRequired(true);
        options.addOption(key);

        Option password = new Option("p", "password", true, "keyfile password");
        password.setRequired(true);
        options.addOption(password);

        Option output = new Option("o", "output", true, "output file");
        output.setRequired(false);
        options.addOption(output);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
            String inputFilePath = cmd.getOptionValue("input");
            String outputFilePath = cmd.getOptionValue("output");
            String keyFilePath = cmd.getOptionValue("key");
            String passwordString = cmd.getOptionValue("password");
            if (passwordString.equals("-")) {
                Scanner scanner = new Scanner(System.in);
                if (scanner.hasNextLine()) {
                    passwordString = scanner.nextLine();
                }
            }
            BatchPDFSign batchPDFSign;
            batchPDFSign = new BatchPDFSign(keyFilePath, passwordString, inputFilePath, outputFilePath);
            batchPDFSign.signFile();
        } catch (GeneralSecurityException | IOException | ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("BatchPDFSignPortable", options);
            System.exit(1);
        }
    }
}
