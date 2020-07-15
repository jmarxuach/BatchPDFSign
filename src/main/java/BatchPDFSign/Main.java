package BatchPDFSign;

import com.itextpdf.text.DocumentException;

import java.io.IOException;
import java.security.GeneralSecurityException;

public class Main {
    public static void main(String[] args){
        if (args.length < 3){
            BatchPDFSign.showUsage();
        } else {
            BatchPDFSign batchPDFSign = new BatchPDFSign(args);
            try {
                batchPDFSign.signFile();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }
    }
}
