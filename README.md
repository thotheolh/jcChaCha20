# jcChaCha20
JavaCard based ChaCha20 stream cipher optimized for JavaCard (16-bit) environment and maybe referenced for other 8/16-bit environments according to RFC-7539 standard.

### Method of Invocation and Testing
The sample library is capable of encrypting and decrypting data as long as the following data format is
used in sequential order while sending an APDU command.

 * Nonce (12 bytes)
 * Counter (4 bytes)
 * Key (32 bytes)
 * Message (208 bytes)

The APDU header bytes are as follows:
 * CLA - 00
 * INS - DA
 * ENCRYPT_P1 - 01 / DECRYPT_P1 - 02
 * P2 - 00

### How to call the encrypt/decrypt function via class methods
 * ChaCha20.encrypt() or ChaCha20.decrypt() function would respectively call encrypt and decrypt but the
function is only suitable for operating on 64 bytes of data before you need to update the key, nonce and
counter manually.
