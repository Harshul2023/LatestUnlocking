package org.example;

import com.fazecast.jSerialComm.SerialPort;


import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLOutput;
import java.util.Base64;
import java.util.Random;

public class UnlockMechanism {
    static SerialPort mySerialPort;

    static String generatedHashValue = "";


    public static void main(String[] args) throws InterruptedException, NoSuchAlgorithmException {

//        SerialPort[] AvailablePorts = SerialPort.getCommPorts();
        mySerialPort = SerialPort.getCommPort("COM19");
        int BaudRate = 115200;
        int DataBits = 8;
        int StopBits = SerialPort.ONE_STOP_BIT;
        int Parity = SerialPort.NO_PARITY;

        mySerialPort.setComPortParameters(BaudRate,
                DataBits,
                StopBits,
                Parity);

        mySerialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 1000, 0);

        mySerialPort.openPort();

        if (mySerialPort.isOpen())
            System.out.println("Port is Open ");
        else
            System.out.println(" Port not open ");

        Random rd = new Random();
        String mode = "USER_MODE";
        byte[] arr = new byte[16];
        rd.nextBytes(arr);
        String encoded16 = RandomNumberGenerator.generateRandomNumber();

        String[] randomArray = new String[encoded16.length()];
        for (int i = 0; i < encoded16.length(); i++) {
            randomArray[i] = String.valueOf(encoded16.charAt(i));
        }

        switch (mode) {
            case "USER_MODE" -> {
                String rtuResponse = getCommandFromSerialPort("RTU" + encoded16);
                System.out.println("RTU Response ----> " + rtuResponse);
                rtuResponse = rtuResponse.substring(191);
                rtuResponse = rtuResponse.substring(7);

                StringBuilder decoded = new StringBuilder();
                for (int i = 0; i < rtuResponse.length(); i++) {
                    char c = rtuResponse.charAt(i);
                    // Initialize the index to -1 (indicating not found)
                    String index;
                    for (int j = 0; j < randomArray.length; j++) {

                        if (randomArray[j].equals(String.valueOf(c))) {
                            if (j == 10)
                                index = "A";
                            else if (j == 11)
                                index = "B";
                            else if (j == 12)
                                index = "C";
                            else if (j == 13)
                                index = "D";
                            else if (j == 14)
                                index = "E";
                            else if (j == 15)
                                index = "F";
                            else index = String.valueOf(j);
                            decoded.append(index);
                            break;  // Exit the loop if the character is found in randomArray
                        }
                    }
                }
                System.out.println("Decoded" + decoded);
                String deviceId = decoded.substring(0, 32).toLowerCase();
                String microControllerId = decoded.substring(32, 56).toLowerCase();
                String receivedHash = decoded.substring(56).toLowerCase();
                StringBuilder asciiHashValue = new StringBuilder();
                for (int i = 0; i < receivedHash.length(); i += 2) {
                    String hexCharacter = receivedHash.substring(i, i + 2);
                    int asciiValue = Integer.parseInt(hexCharacter, 16);
                    char asciiCharacter = (char) asciiValue;
                    asciiHashValue.append(asciiCharacter);
                }
                receivedHash = String.valueOf(asciiHashValue);

                generatedHashValue = generateHash(deviceId, microControllerId);
                generatedHashValue = generatedHashValue.trim();
                if (generatedHashValue.equals(receivedHash)) {
                    String rtcResponse = getCommandFromSerialPort("RTC" + generatedHashValue);
                    System.out.println("RTc Response ----> " + rtcResponse);
                }

                Thread threadSTA = new Thread(() -> {
                    getCommandFromSerialPort("STA");
                });
                Thread threadSTP = new Thread(() -> {
                    getCommandFromSerialPort("STP");

                });
                threadSTA.start();
                Thread.sleep(10000);
                threadSTP.start();
                System.exit(0);
            }
            case "ADMIN_MODE" -> {
                sendCommandToSerialPort("ADMIN_SUNFOX");
//                if (getCommandFromSerialPort("GET_INF").contains("Lite")) {

                sendCommandToSerialPort("SET_DID" + encoded16);

                String deviceId = getCommandFromSerialPort("GET_DID");

                System.out.println("DEVICE ID ---->" + deviceId);
                deviceId = deviceId.substring(7);

                String mId = getCommandFromSerialPort("GET_MID");
                mId += " ";
//                mId = mId.substring(10);
                byte[] bytes = mId.getBytes(StandardCharsets.US_ASCII);
                StringBuilder hexBuilder = new StringBuilder();

                for (byte b : bytes) {
                    String hex = String.format("%02x", b);
                    hexBuilder.append(hex);
                }
                mId = hexBuilder.toString();
                mId = mId.substring(14);
                generatedHashValue = generateHash(asciiToHex(deviceId), mId);
                sendCommandToSerialPort("SET_HAS" + generatedHashValue);

                if (getCommandFromSerialPort("GET_HAS").contains("ACK_HAS" + generatedHashValue)) {
                    String temp = "ACK_HAS" + generatedHashValue;
                    System.out.println(temp);
                }
                arr = new byte[12];
                rd.nextBytes(arr);
                String encoded12 = Base64.getUrlEncoder().encodeToString(arr);
                sendCommandToSerialPort("SET_ATM" + encoded12);
                long currentTimeStamp = System.currentTimeMillis();
                String atmAcknowledgment = getCommandFromSerialPort("GET_ATM" + currentTimeStamp);
                System.out.println("ATm ACKNOWLEDGEMENT ---->" + atmAcknowledgment);

                sendCommandToSerialPort("SET_AID" + encoded16);

                String activatorIdValue = getCommandFromSerialPort("GET_AID");

                System.out.println("Activator Id ---->" + activatorIdValue);

                String microControllerUniqueId = getCommandFromSerialPort("GET_MID");

                System.out.println("Micro Controller Unique Id ---->" + microControllerUniqueId);

                arr = new byte[16];
                rd.nextBytes(arr);

                String rtuValue = getCommandFromSerialPort("RTU" + encoded16);

                System.out.println("RTU Value ---->" + rtuValue);

                String rtcValue = getCommandFromSerialPort("RTC");

                System.out.println("RTC Value ---->" + rtcValue);

                System.out.println("______________SETTING STAGES ,CUSTOMER AND PHONE ID _________________ ");

                String stageId1 = "SpandanLite12345SpandanLite12345";
                sendCommandToSerialPort("SET_SI1" + stageId1);


                String stageId2 = "SpandanLite67890SpandanLite67890";
                sendCommandToSerialPort("SET_SI2" + stageId2);


                String customerId = "12CustByteId";
                sendCommandToSerialPort("SET_CID" + customerId);


                String phoneId = "ph7983145689";
                sendCommandToSerialPort("SET_PID" + phoneId);


                String si1 = getCommandFromSerialPort("GET_SI1");

                System.out.println("si1 ---->" + si1);


                String si2 = getCommandFromSerialPort("GET_SI2");

                System.out.println("si2 ---->" + si2);

                String cid = getCommandFromSerialPort("GET_CID");

                System.out.println("CID ----> " + cid);

                String pid = getCommandFromSerialPort("GET_PID");

                System.out.println("pid ---->" + pid);
            }
        }
//        }

    }

    private static String generateHash(String deviceId, String microControllerId) throws NoSuchAlgorithmException {

        String hashInput = deviceId + microControllerId;
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(hashInput.getBytes(StandardCharsets.UTF_8));
        BigInteger number = new BigInteger(1, hash);

        // Converting the message digest into a Hexa decimal value.
        StringBuilder hexString = new StringBuilder(number.toString(16));

        while (hexString.length() < 64) {
            hexString.insert(0, '0');
        }

        return hexString.toString();
    }

    private static String asciiToHex(String asciiStr) {
        char[] chars = asciiStr.toCharArray();
        StringBuilder hex = new StringBuilder();
        for (char ch : chars) {
            hex.append(Integer.toHexString((int) ch));
        }
        return hex.toString();
    }

    private static String getCommandFromSerialPort(String command) {
        String s = "";

        try {
            byte[] WriteByte = command.getBytes();

            int bytesTxed = 0;

            bytesTxed = mySerialPort.writeBytes(WriteByte, command.getBytes().length);
            System.out.print(" Bytes Transmitted -> " + bytesTxed + "\n");
            byte[] readBuffer = new byte[1024];
            try {
                StringBuilder sBuilder = new StringBuilder();
                if (command.equals("STA")) {
//                    readBuffer = new byte[4];
                    StringBuilder s2 = new StringBuilder();
                    while (mySerialPort.readBytes(readBuffer, readBuffer.length) != 0) {

                        s = new String(readBuffer, StandardCharsets.UTF_8);
                        for (int i = 0; i < s.length(); i++) {
                            String sTemp = String.valueOf(s.charAt(i));
                            switch (sTemp) {
                                case "2" -> sTemp = "0";
                                case "3" -> sTemp = "1";
                                case "1" -> sTemp = "2";
                                case "5" -> sTemp = "3";
                                case "7" -> sTemp = "4";
                                case "9" -> sTemp = "5";
                                case "4" -> sTemp = "6";
                                case "6" -> sTemp = "7";
                                case "0" -> sTemp = "8";
                                case "8" -> sTemp = "9";
                                default -> String.valueOf(s.charAt(i));
                            }
                            s2.append(sTemp);
                        }
                        System.out.print(s2);
                        s2 = new StringBuilder("");
                    }
//                    System.out.print(s2);
                } else {
                    while (mySerialPort.readBytes(readBuffer, readBuffer.length) != 0) {
                        s = new String(readBuffer, StandardCharsets.UTF_8).trim();
                        System.out.print(s);
                        sBuilder.append(s);
                    }
                }
//                System.out.println(sBuilder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        s += "\n";
        return s.trim();
    }

    private static void sendCommandToSerialPort(String command) {
        try {
            byte[] WriteByte = command.getBytes();

            int bytesTxed = 0;

            bytesTxed = mySerialPort.writeBytes(WriteByte, command.getBytes().length);

            System.out.print(" Bytes Transmitted -> " + bytesTxed + "\n");

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}