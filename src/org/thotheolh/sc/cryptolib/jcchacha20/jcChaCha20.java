/*
 * ChaCha20 implemented according to RFC-7539.
 */
package org.thotheolh.sc.cryptolib.jcchacha20;

import javacard.framework.*;
import javacardx.framework.util.ArrayLogic;

/**
 *
 * @author Thotheolh
 */
public class jcChaCha20 extends Applet {

    private short[] sBuff = JCSystem.makeTransientShortArray((short) 2, JCSystem.CLEAR_ON_RESET);
    private byte[] nonce = JCSystem.makeTransientByteArray((short) 12, JCSystem.CLEAR_ON_RESET);
    private byte[] counter = JCSystem.makeTransientByteArray((short) 4, JCSystem.CLEAR_ON_RESET);
    private byte[] key = JCSystem.makeTransientByteArray((short) 32, JCSystem.CLEAR_ON_RESET);
    private byte[] b = JCSystem.makeTransientByteArray((short) 256, JCSystem.CLEAR_ON_RESET);
    private ChaCha20 cipher;

    /**
     * Installs this applet.
     *
     * @param bArray
     * the array containing installation parameters
     * @param bOffset
     * the starting offset in bArray
     * @param bLength
     * the length in bytes of the parameter data in bArray
     */
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new jcChaCha20();
        new ChaCha20();
    }

    /**
     * Only this class's install method should create the applet object.
     */
    protected jcChaCha20() {
        register();
    }

    /**
     * Processes an incoming APDU.
     *
     * @see APDU
     * @param apdu
     * the incoming APDU
     */
    public void process(APDU apdu) {
        //Insert your code here

        if (selectingApplet()) {
            return;
        }

        byte[] buffer = apdu.getBuffer();
        apdu.setIncomingAndReceive();

        // CLA = 00, INS = DA (PUT DATA)
        if (buffer[ISO7816.OFFSET_CLA] == (byte) 0x00) {
            if (buffer[ISO7816.OFFSET_INS] == (byte) 0xDA) {

                /**
                 * Input APDU Data Structure
                 * -------------------------
                 *
                 * +------------+-------------+----------+---------------+
                 * | Nonce (12) | Counter (4) | Key (32) | Message (208) |
                 * +------------+-------------+----------+---------------+
                 *
                 */
                if (buffer[ISO7816.OFFSET_P1] == (byte) 0x01) {
                    if ((buffer[ISO7816.OFFSET_LC] & 0xFF) > 48) {
                        // Copy nonce
                        ArrayLogic.arrayCopyRepackNonAtomic(buffer, (short) 5, (short) 12, nonce, (short) 0);

                        // Copy counter
                        ArrayLogic.arrayCopyRepackNonAtomic(buffer, (short) 17, (short) 4, counter, (short) 0);

                        // Copy key
                        ArrayLogic.arrayCopyRepackNonAtomic(buffer, (short) 21, (short) 32, key, (short) 0);

                        // Calculate message length
                        sBuff[0] = (short) ((buffer[ISO7816.OFFSET_LC] & 0xFF) - 48);

                        // Copy incoming message
                        ArrayLogic.arrayCopyRepackNonAtomic(buffer, (short) 53, (short) sBuff[0], b, (short) 0);

                        // Encrypt
                        while (sBuff[0] > 0) {
                            if (sBuff[0] <= 64) {
                                cipher.encrypt(key, (short) 0, nonce, (short) 0, counter, (short) 0, b, (short) 0, sBuff[1], buffer, sBuff[1]);
                                
                                // Increment length of processed bytes
                                sBuff[1] += sBuff[0];
                                
                                // Send out response of encrypted ciphertext
                                apdu.setOutgoing();
                                apdu.setOutgoingLength(sBuff[1]);
                                apdu.sendBytesLong(buffer, (short) 0, sBuff[1]);
                                
                                // Reset
                                sBuff[0] = 0;
                                sBuff[1] = 0;
                            } else {
                                cipher.encrypt(key, (short) 0, nonce, (short) 0, counter, (short) 0, b, (short) 0, (short) 64, buffer, sBuff[1]);
                                sBuff[0] -= 64;
                                sBuff[1] += 64;
                            }
                        }
                    }
                } else if (buffer[ISO7816.OFFSET_P1] == (byte) 0x02) {
                    // Decrypt
                } else {
                    ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
                }
            } else {
                ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
            }
        } else {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }
    }
}
