package example;

import com.pff.*;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Yariki on 8/11/2015.
 */
public class TestAttachments {

    private final static String AttachmentName = "Invoice_1201003378_2013_08_01.pdf".toUpperCase();

    public static void main(String[] args){
        final String filename = "f:\\Visual\\WORK\\osttest6@gmail.com.ost"; //vincent@metajure.com.ost //  //      iyariki_ya_gmail.pst
        new TestAttachments(filename);
    }

    public TestAttachments(String filename) {
        try {
            PSTFile pstFile = new PSTFile(filename);
            System.out.println(pstFile.getMessageStore().getDisplayName());
            processFolder(pstFile.getRootFolder());
        } catch (Exception err) {
            err.printStackTrace();
        }
    }


    public void processFolder(PSTFolder folder)
            throws PSTException, java.io.IOException {
            System.out.println(folder.getDisplayName());

        // go through the folders...
        if (folder.hasSubfolders()) {
            try{
                Vector<PSTFolder> childFolders = folder.getSubFolders();
                for (PSTFolder childFolder : childFolders) {
                    processFolder(childFolder);
                }
            }catch (Exception e){
                Logger.getGlobal().log(Level.SEVERE,e.getMessage());
            }

        }

        // and now the emails for this folder
        if (folder.getContentCount() > 0) {
            try{
                PSTMessage email = (PSTMessage) folder.getNextChild();
                boolean needBreak = false;
                while (email != null) {
                    int count = email.getNumberOfAttachments();
                    for(int i = 0; i < count; i++){
                        PSTAttachment attachment = email.getAttachment(i);
                        String filename = attachment.getLongFilename().toUpperCase();
                        if(!filename.contains("DOCX")){
                            continue;
                        }
                        //if(filename.equals(AttachmentName)){
                            InputStream reader = attachment.getFileInputStream();
                                ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
                                final int lenght = 8176;
                                byte[] output = new byte[lenght];
                                int nRead;
                                while ( (nRead = reader.read(output)) != -1) {
                                    bufferStream.write(output,0,nRead);
                                }
                                bufferStream.flush();
                                byte[] byteBuffer = bufferStream.toByteArray();
                            FileOutputStream outputStream = new FileOutputStream("C:\\"+filename);
                            outputStream.write(byteBuffer);
                            outputStream.close();
                        needBreak = true;
                        break;

                        //}
                    }
                    if(needBreak){
                        break;
                    }
                    email = (PSTMessage) folder.getNextChild();
                }
            }catch(Exception e){
                Logger.getGlobal().log(Level.SEVERE,e.getMessage());
            }

        }
    }


}
