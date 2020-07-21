package BatchPDFSign;

/**
 * This method is called when the jar is executed. It shows the help if fewer than 3 args were entered.
 * If more than 3 args were entered, it creates a BatchPDFSign Object and calls the signFile(); function on it.
 * @author Joe Meier, Jocomol, joelmeier08@gmail.com
 * @version 1.0.3.1
 */
public class Main {
    public static void main(String[] args){
        if (args.length < 3){ //Shows help if not enough args were entered
            BatchPDFSign.showUsage();
        } else {
            BatchPDFSign batchPDFSign = new BatchPDFSign(args);
            batchPDFSign.signFile();
        }
    }
}
