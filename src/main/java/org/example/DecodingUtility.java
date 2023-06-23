package org.example;

public class DecodingUtility {
    public static String decodeRtuFromSerialPort(String responseFromSerialPort,String randomString)
    {
        String[] randomArray = new String[randomString.length()];
        for (int i = 0; i < randomString.length(); i++) {
            randomArray[i] = String.valueOf(randomString.charAt(i));
        }
        StringBuilder decoded = new StringBuilder();
        for (int i = 0; i < responseFromSerialPort.length(); i++) {
            char c = responseFromSerialPort.charAt(i);
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
        return decoded.toString();
    }
}
