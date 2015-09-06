package com.pff;

import com.sun.deploy.util.SystemUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

//import com.jcraft.jzlib.*;

/**
 * Created by Yariki on 8/15/2015.
 */
public class PSTNodeAttachmentInputStream extends PSTNodeInputStream {
    public PSTNodeAttachmentInputStream(PSTFile pstFile, byte[] attachmentData) throws PSTException {
        super(pstFile, attachmentData);
    }

    public PSTNodeAttachmentInputStream(PSTFile pstFile, byte[] attachmentData, boolean encrypted) throws PSTException {
        super(pstFile, attachmentData, encrypted);
    }

    public PSTNodeAttachmentInputStream(PSTFile pstFile, PSTDescriptorItem descriptorItem) throws IOException, PSTException {
        super(pstFile, descriptorItem);
    }

    public PSTNodeAttachmentInputStream(PSTFile pstFile, OffsetIndexItem offsetItem) throws IOException, PSTException {
        super(pstFile, offsetItem);
    }

    @Override
    protected void detectZlib() throws PSTException {
        // not really sure how this is meant to work, kind of going by feel here.
        if (this.length < 4) {
            return;
        }
        try {
            if (this.read() == 0x78 && this.read() == 0x9c) {
                // we are a compressed block, decompress the whole thing into a buffer
                // and replace our contents with that.
                // firstly, if we have blocks, use that as the length
                int uncompressedLength = (int) this.length;
                if (this.indexItems.size() > 0) {
                    uncompressedLength = 0;
                    for (OffsetIndexItem i : this.indexItems) {
                        uncompressedLength += i.size;
                    }
                }
                byte[] inData = new byte[uncompressedLength]; // TODO: make this stream correctly.
                this.seek(0);
                int lengthRead = this.read(inData);
                if (lengthRead != uncompressedLength) {
                    throw new PSTException("Bad assumption: " + lengthRead);
                }

                FileOutputStream fOUt = new FileOutputStream("C:\\test.cmp");
                fOUt.write(inData);
                fOUt.close();

                ByteArrayOutputStream outputStream = new ByteArrayOutputStream((int) this.length);
                InflaterOutputStream inflaterStream = new InflaterOutputStream(outputStream);
                inflaterStream.write(inData);
                inflaterStream.close();
                outputStream.close();

                this.allData = outputStream.toByteArray();
                this.inData = inData;
                this.currentLocation = 0;
                this.currentBlock = 0;
                this.length = this.allData.length;
                isZlib = true;
            }
            this.seek(0);
        } catch (IOException err) {
            throw new PSTException("Unable to compress reportedly compressed block", err);
        } catch (Exception ex) {
            throw new PSTException("Unable to compress reportedly compressed block", ex);
        }
    }


    @Override
    public int read(byte[] output) throws IOException {
        // this method is implemented in an attempt to make things a bit faster than the byte-by-byte read() crap above.
        // it's tricky 'cause we have to copy blocks from a few different areas.

        if (this.currentLocation == this.length) {
            // EOF
            return -1;
        }

        // first deal with the small stuff
        if (this.allData != null) {
            int bytesRemaining = (int) (this.length - this.currentLocation);
            if (output.length >= bytesRemaining) {
                System.arraycopy(this.allData, (int) this.currentLocation, output, 0, bytesRemaining);
                if (this.encrypted) {
                    PSTObject.decode(output);
                }
                this.currentLocation += bytesRemaining; // should be = to this.length
                return bytesRemaining;
            } else {
                System.arraycopy(this.allData, (int) this.currentLocation, output, 0, output.length);
                if (this.encrypted) {
                    PSTObject.decode(output);
                }
                this.currentLocation += output.length;
                return output.length;
            }
        }

        boolean filled = false;
        int totalBytesFilled = 0;
        Inflater inf  = new Inflater();

        long size = 0;

//        Collections.sort(this.indexItems, new Comparator<OffsetIndexItem>() {
//            public int compare(OffsetIndexItem o1, OffsetIndexItem o2) {
//                return Long.compare(o1.size,o2.size);
//            }
//        });


        for(OffsetIndexItem i : this.indexItems){
            in.seek(i.fileOffset);
            byte[] chunk = new byte[i.size];
            int n = in.read(chunk);
            size += i.size;
            System.out.println(String.format("Offset = %s Size = %s End = %s OriginalOffset = %s", Long.toHexString(i.fileOffset), Long.toHexString(i.size),Long.toHexString(i.fileOffset + i.size),Long.toHexString(size)));
            System.arraycopy(chunk, 0, output, totalBytesFilled, chunk.length);
            totalBytesFilled += chunk.length;
        }

        // decode the array if required
        if (this.encrypted) {
            PSTObject.decode(output);
        }

        // fill up our chunk
        // move to the next chunk
        return totalBytesFilled;
    }
}
