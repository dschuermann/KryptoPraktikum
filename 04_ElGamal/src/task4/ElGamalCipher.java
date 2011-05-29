/*
 * jCrypt - Programmierumgebung für das Kryptologie-Praktikum
 * Studienarbeit am Institut für Theoretische Informatik der
 * Technischen Universität Braunschweig
 * 
 * Datei:        ElGamalCipher.java
 * Beschreibung: Dummy-Implementierung der ElGamal-Public-Key-Verschlüsselung
 * Erstellt:     30. März 2010
 * Autor:        Martin Klußmann
 */

package task4;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;
import de.tubs.cs.iti.jcrypt.chiffre.BigIntegerUtil;
import de.tubs.cs.iti.jcrypt.chiffre.BlockCipher;
import de.tubs.cs.iti.jcrypt.chiffre.BlockCipherUtil;

/**
 * Dummy-Klasse für das ElGamal-Public-Key-Verschlüsselungsverfahren.
 * 
 * @author Martin Klußmann
 * @version 1.1 - Sat Apr 03 22:06:35 CEST 2010
 */
public final class ElGamalCipher extends BlockCipher {
  String publicKey;
  String privateKey;

  // public key
  public BigInteger p;
  public BigInteger g;
  public BigInteger y;

  // private key
  public BigInteger x;

  public void makeKey() {
    Random sc = new SecureRandom();

    getPrimeAndGenerator();

    // private key 1 <= x < p-1
    x = BigIntegerUtil.randomBetween(BigIntegerUtil.ONE, p.subtract(BigIntegerUtil.ONE), sc);

    y = g.modPow(x, p);

    privateKey = p.toString() + "\n" + g.toString() + "\n" + x.toString();
    publicKey = p.toString() + "\n" + g.toString() + "\n" + y.toString();

    System.out.println("private key = " + privateKey);
    System.out.println("public key = (" + publicKey + ")");

  }

  public void getPrimeAndGenerator() {
    Random sc = new SecureRandom();
    int k = 512; // prime number with k=512 bits
    int certainty = 100; // The probability that the new BigInteger represents a prime number will
                         // exceed (1-1/2^certainty)

    p = null;
    BigInteger q = null;
    do {
      q = new BigInteger(k - 1, certainty, sc);
      p = q.multiply(BigIntegerUtil.TWO).add(BigIntegerUtil.ONE); // secure prime p = 2q+1
    } while (!p.isProbablePrime(certainty));

    BigInteger MINUS_ONE = BigInteger.ONE.negate().mod(p); // -1 mod p

    g = null;
    BigInteger factor = null;
    do {
      // 2 <= g < p-1
      g = BigIntegerUtil.randomBetween(BigIntegerUtil.TWO, p.subtract(BigIntegerUtil.ONE), sc);
      factor = g.modPow(q, p);
    } while (!factor.equals(MINUS_ONE));
  }

  public void writeKey(BufferedWriter key) {
    try {
      key.write(publicKey);

      Logger("Writing Information: ");
      Logger("+--Key: " + publicKey);

      key.close();
    } catch (IOException e) {
      System.out.println("Abbruch: Fehler beim Schreiben oder Schließen der " + "Schlüsseldatei.");
      e.printStackTrace();
      System.exit(1);
    }
  }

  public void readKey(BufferedReader key) {
    try {

      // pubkey
      p = new BigInteger(key.readLine());
      g = new BigInteger(key.readLine());
      y = new BigInteger(key.readLine());

      // privateKey = p.toString() + "\n" + g.toString() + "\n" + x.toString();
      publicKey = p.toString() + "\n" + g.toString() + "\n" + y.toString();

      Logger("Reading Information: ");
      Logger("+--KeyString: " + publicKey);

      key.close();
    } catch (IOException e) {
      System.err.println("Abbruch: Fehler beim Lesen oder Schließen der " + "Schlüsseldatei.");
      e.printStackTrace();
      System.exit(1);
    } catch (NumberFormatException e) {
      System.err.println("Abbruch: Fehler beim Parsen eines Wertes aus der " + "Schlüsseldatei.");
      e.printStackTrace();
      System.exit(1);
    }
  }

  public void encipher(FileInputStream cleartext, FileOutputStream ciphertext) {
    Random sc = new SecureRandom();

    int Lp = p.bitLength(); // bitlength of p (512 bit)
    int L = (Lp - 1) / 8; // blocksize

    // read cleartext
    BigInteger M = readClear(cleartext, L);

    // random two <= k < p-1
    BigInteger k = BigIntegerUtil.randomBetween(BigIntegerUtil.TWO, p.subtract(BigIntegerUtil.ONE), sc);

    BigInteger a = g.modPow(k, p); // g^k mod p
    BigInteger b = M.multiply(y.modPow(k, p)); // M * y^k mod p

    System.out.println("M "+M);

    BigInteger C = a.add(b).multiply(p);

    System.out.println("C "+C);
    
    writeCipher(ciphertext, C);
  }

  public void decipher(FileInputStream ciphertext, FileOutputStream cleartext) {

    //
    // // keyGenerator();
    //
    // BigInteger[] C = new BigInteger[] { a, b };
    // BigInteger M = decrypt(C);
    //
    // String outputString = new String(M.toByteArray());
    // System.out.println("Cipher Array: " + C[0] + " " + C[1]);
    // System.out.println("Clear: " + outputString);
    // try {
    // cleartext.write(outputString.getBytes());
    // } catch (IOException e1) {
    // System.out.println("Failed at FileOutputStream");
    // e1.printStackTrace();
    // }
    //
    // try {
    // cleartext.close();
    // ciphertext.close();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // writeClear(cleartext, M);

  }

  public BigInteger x() {
    return new BigInteger("12345678901234567890");
  }

  public BigInteger r(BigInteger k) {
    BigInteger r = g.modPow(k, p);

    return r;
  }

  public BigInteger s(BigInteger M, BigInteger r, BigInteger k_inverse) {
    BigInteger xr = x.multiply(r);
    BigInteger s = ((M.subtract(xr)).multiply(k_inverse)).mod(p.subtract(BigInteger.ONE));

    return s;
  }

  public void gammel(String message) {

    // message.length <= 8 . Wenn groesser als 8, dann kommt
    // was falsches raus o.O
    BigInteger M = new BigInteger(message.getBytes());
    BigInteger[] C = encrypt(M);

    BigInteger M2 = decrypt(C);

    String output = new String(M2.toByteArray());
    System.out.println("Clear: " + output);
  }

  public BigInteger[] encrypt(BigInteger message) {
    Random sc = new SecureRandom();

    BigInteger M = message;
    BigInteger k = new BigInteger(512, sc);

    BigInteger a = g.modPow(k, p);
    BigInteger b = M.multiply(y.modPow(k, p)).mod(p);

    return new BigInteger[] { a, b };
  }

  public BigInteger decrypt(BigInteger[] C) {

    BigInteger a = C[0];
    BigInteger b = C[1];

    BigInteger exponent = (p.subtract(x)).subtract(new BigInteger("1"));
    BigInteger z = a.modPow(exponent, p);
    BigInteger M = (z.multiply(b)).mod(p);

    return M;
  }

  public static String getTextAsString(FileInputStream cleartext) {
    StringBuffer clearTextBuffer = new StringBuffer();

    try {
      int ch = 0;
      while ((ch = cleartext.read()) != -1) {
        clearTextBuffer.append((char) ch);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return clearTextBuffer.toString();
  }

  private void Logger(String event) {
    System.out.println("ElGamal$  " + event);
  }

}
