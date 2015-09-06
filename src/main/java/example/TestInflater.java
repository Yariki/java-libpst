package example;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

/**
 * Created by Yariki on 8/15/2015.
 */
public class TestInflater  {

    public static void main(String[] args){
        try{
            FileInputStream fis = new FileInputStream("C:\\log.txt");
            FileOutputStream fos = new FileOutputStream("C:\\deflated.txt");
            DeflaterOutputStream dos = new DeflaterOutputStream(fos);

            doCopy(fis, dos); // copy original.txt to deflated.txt and compress it

            FileInputStream fis2 = new FileInputStream("C:\\deflated.txt");
            InflaterInputStream iis = new InflaterInputStream(fis2);
            FileOutputStream fos2 = new FileOutputStream("C:\\inflated.txt");

            doCopy(iis, fos2); // copy deflated.txt to inflated.txt and uncompress it


        }catch (Exception ex){
            System.out.println(ex.getMessage());
        }
    }

    public static void doCopy(InputStream is, OutputStream os) throws Exception {
        int oneByte;
        while ((oneByte = is.read()) != -1) {
            os.write(oneByte);
        }
        os.close();
        is.close();
    }


}
