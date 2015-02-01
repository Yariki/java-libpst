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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

/**
 *
 * @author Yariki
 */
public class PSTConversationIndexData {
    
    private static final int RESERVED_INDEX = 0;
    private static final int FILETIME_LENGHT = 5;
    private static final int CONVERSSATION_INDEX_OFFSET = 6;
    private static final int CONVERSATION_INDEX_GUID_LENGHT = 16;
    private static final int CONVERSATION_INDEX_LEGHT = CONVERSSATION_INDEX_OFFSET + CONVERSATION_INDEX_GUID_LENGHT;
    private static final int CCH_MAX = 256;
    
    private byte[] data;
    
    public PSTConversationIndexData(byte[] data) {
        this.data = data;
    }
    
    public byte getReservedValue(){
        return data[RESERVED_INDEX];
    }
    
    public UUID  getConversationUUID(String conversationTopic, boolean conversationIndexTracking) throws NoSuchAlgorithmException{
        boolean useTopic = true;
        UUID uuId = null;
        if(conversationIndexTracking && data != null && data.length >= CONVERSATION_INDEX_LEGHT && getReservedValue() == 0x01){
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
                for(ich = 0; ich <= cchHash;ich++){
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
        byte[] uuidArray = Arrays.copyOfRange(data,CONVERSSATION_INDEX_OFFSET,CONVERSATION_INDEX_GUID_LENGHT);
        return uuidArray;
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
    
}
