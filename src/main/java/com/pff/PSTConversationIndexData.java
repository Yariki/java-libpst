/*
 * Copyright 2015 Yariki.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pff;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.Buffer;
import sun.misc.BASE64Decoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Yariki
 */
public class PSTConversationIndexData {
    
    private static final int RESERVED_INDEX = 0;
    private static final int CONVERSSATION_INDEX_OFFSET = 6;
    private static final int CONVERSATION_INDEX_GUID_LENGHT = 16;
    private static final int CONVERSATION_INDEX_LEGHT = CONVERSSATION_INDEX_OFFSET + CONVERSATION_INDEX_GUID_LENGHT;
    private static final int CCH_MAX = 256;
    
    private byte[] data;
    
    public PSTConversationIndexData(byte[] data) {
        this.data = data;
        init(this.data);
    }
    
    public byte getReservedValue(){
        return data[RESERVED_INDEX];
    }
    
    public UUID  getConversationUUID(String conversationTopic, boolean conversationIndexTracking) throws NoSuchAlgorithmException{
        boolean useTopic = true;
        UUID uuId = null;
        //TODO conversationIndexTracking && 
        if( data != null && data.length >= CONVERSATION_INDEX_LEGHT && getReservedValue() == 0x01){
            byte[] uuidArray = getConversationIndexUuidArray();
            uuId =  UUID.nameUUIDFromBytes(uuidArray);
            useTopic = false;
        }
        if(useTopic){
            int cchHash;
            byte[] wzBuffer = new byte[CCH_MAX];
            int cbHash = 0;
            
            byte[] topicBytes = stringToBytesUTFCustom(conversationTopic);
            cchHash = topicBytes.length;
            
            if(cchHash < CCH_MAX){
                int ich;
                for(ich = 0; ich < cchHash;ich++){
                    wzBuffer[ich] = topicBytes[ich];
                }
                try {
                    MessageDigest  md = MessageDigest.getInstance("MD5");
                    md.update(wzBuffer);    
                    byte[] md5Array = md.digest();
                    uuId =  UUID.nameUUIDFromBytes(md5Array);        
                } catch (Exception e) {
                }
            }
        }
        return uuId;
    }
    
    public byte[] getConversationIndexUuidArray(){
        byte[] uuidArray = Arrays.copyOfRange(data,CONVERSSATION_INDEX_OFFSET,
                CONVERSATION_INDEX_GUID_LENGHT);
        return uuidArray;
    }
    
    public Date getConversationFileTime(){
        Date date = null;
        byte[] filetimeArr = Arrays.copyOfRange(data, 0, CONVERSSATION_INDEX_OFFSET);
        String hex_str = javax.xml.bind.DatatypeConverter.printHexBinary(filetimeArr) + "0000";
        long mil = Long.parseLong(hex_str, 16);
        date = convertFILETIMEToDate(mil);
        return date;
    }
    
    
    private static Date convertFILETIMEToDate(long filetime){
          // Filetime Epoch is JAN 01 1601
          // java date Epoch is January 1, 1970
          // so take the number and subtract java Epoch:
          long javaTime = filetime - 0x19db1ded53e8000L;

          // convert UNITS from (100 nano-seconds) to (milliseconds)
          javaTime /= 10000;

          // Date(long date)
          // Allocates a Date object and initializes it to represent 
          // the specified number of milliseconds since the standard base 
          // time known as "the epoch", namely January 1, 1970, 00:00:00 GMT.
          Date theDate = new Date(javaTime);


          return theDate;
     }
    
    
    public static byte[] stringToBytesASCII(String str) {

        char[] buffer = str.toCharArray();
        byte[] b = new byte[buffer.length];
        for (int i = 0; i < b.length; i++) {
         b[i] = (byte) buffer[i];
        }
        return b;
    }
    
    public static byte[] stringToBytesUTFCustom(String str) {
        char[] buffer = str.toCharArray();
        byte[] b = new byte[buffer.length << 1];
        for(int i = 0; i < buffer.length; i++) {
            int bpos = i << 1;
            b[bpos] = (byte) ((buffer[i]&0xFF00)>>8);
            b[bpos + 1] = (byte) (buffer[i]&0x00FF);
        }
        return b;
    }
    
    public String getHexString(){
        if(data == null || data.length == 0){
            return "";
        }
        BASE64Decoder decoder = new BASE64Decoder();
        String hex_string = javax.xml.bind.DatatypeConverter.printHexBinary(data);
        return hex_string;
    }
    
    
    private void init(byte[] buffer){
        if(buffer == null || buffer.length == 0){
            return;
        }
        
        BASE64Decoder decoder = new BASE64Decoder();
        String hex_string = javax.xml.bind.DatatypeConverter.printHexBinary(buffer);
        byte[] filetime_data = Arrays.copyOfRange(buffer, 0, 6);
        byte[] uuidArray = Arrays.copyOfRange(buffer,CONVERSSATION_INDEX_OFFSET,
                CONVERSATION_INDEX_GUID_LENGHT);
        
        
        
    }
    
    
}
