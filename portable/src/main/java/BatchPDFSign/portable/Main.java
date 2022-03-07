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

        Option pageOpt = new Option(null, "page", true, "page of signature rectangle; needs to be specified to output signature rectangle");
        output.setRequired(false);
        options.addOption(pageOpt);

        Option rectPosXOpt = new Option(null, "rx", true, "x position of signature rectangle; needs --page to be specified as well");
        output.setRequired(false);
        options.addOption(rectPosXOpt);

        Option rectPosYOpt = new Option(null, "ry", true, "y position of signature rectangle; needs --page to be specified as well");
        output.setRequired(false);
        options.addOption(rectPosYOpt);

        Option rectWidthOpt = new Option(null, "rw", true, "width of signature rectangle; needs --page to be specified as well");
        output.setRequired(false);
        options.addOption(rectWidthOpt);

        Option rectHeightOpt = new Option(null, "rh", true, "height of signature rectangle; needs --page to be specified as well");
        output.setRequired(false);
        options.addOption(rectHeightOpt);

        Option fontsizeOpt = new Option(null, "fs", true, "font size of text in signature rectangle (default: 12); needs --page to be specified as well");
        output.setRequired(false);
        options.addOption(fontsizeOpt);

        Option signTextOpt = new Option(null, "signtext", true, "signature text; needs --page to be specified as well");
        output.setRequired(false);
        options.addOption(signTextOpt);

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
            if (cmd.hasOption("page")) {
                if (!cmd.hasOption("rx") || !cmd.hasOption("ry") ||
                    !cmd.hasOption("rw") || !cmd.hasOption("rh")) {
                    System.out.println("If a page is specified, all of the options --rx --ry --rw and --rh also need to be specified.");
                    System.exit(1);
                }
                Integer page = Integer.parseInt(cmd.getOptionValue("page"));
                Float rectPosX   = Float.parseFloat(cmd.getOptionValue("rx"));
                Float rectPosY   = Float.parseFloat(cmd.getOptionValue("ry"));
                Float rectWidth  = Float.parseFloat(cmd.getOptionValue("rw"));
                Float rectHeight = Float.parseFloat(cmd.getOptionValue("rh"));
                Float fontSize = 12.f;
                if (cmd.hasOption("fs")) {
                    fontSize = Float.parseFloat(cmd.getOptionValue("fs"));
                }
                String signText = null;
                if (cmd.hasOption("signtext")) {
                    signText  = cmd.getOptionValue("signtext");
                }
                batchPDFSign.signFile(page, rectPosX, rectPosY, rectWidth, rectHeight, fontSize, signText);
            } else {
                batchPDFSign.signFile();
            }
        } catch (GeneralSecurityException | IOException | ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("BatchPDFSignPortable", options);
            System.exit(1);
        }
    }
}
