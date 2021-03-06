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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import de.tubs.cs.iti.jcrypt.chiffre.BigIntegerUtil;
import de.tubs.cs.iti.jcrypt.chiffre.BlockCipher;

/**
 * Dummy-Klasse für das ElGamal-Public-Key-Verschlüsselungsverfahren.
 * 
 * @author Martin Klußmann
 * @version 1.1 - Sat Apr 03 22:06:35 CEST 2010
 */
public final class ElGamalCipher extends BlockCipher {
  String publicKey;
  String privateKey;
  String publicKeyFile;
  String privateKeyFile;

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
    publicKeyFile = "bla.secr.public";
    privateKeyFile = "bla.secr.private";

    Writer writer = null;

    try {
      writer = new BufferedWriter(new FileWriter(publicKeyFile));
      writer.write(publicKey);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      writer = new BufferedWriter(new FileWriter(privateKeyFile));
      writer.write(privateKey);
      writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    // write reference to files in key.txt
    try {
      key.write(publicKeyFile);
      key.newLine();
      key.write(privateKeyFile);

      Logger("Writing Information: ");
      Logger("+--publicKey: " + publicKey);
      Logger("+--privateKey: " + privateKey);

      key.close();
    } catch (IOException e) {
      System.out.println("Abbruch: Fehler beim Schreiben oder Schließen der " + "Schlüsseldatei.");
      e.printStackTrace();
      System.exit(1);
    }
  }

  public void readKey(BufferedReader key) {
    try {
      publicKeyFile = key.readLine();
      privateKeyFile = key.readLine();

      BufferedReader reader = null;
      try {
        reader = new BufferedReader(new FileReader(publicKeyFile));

        // pubkey
        p = new BigInteger(reader.readLine());
        g = new BigInteger(reader.readLine());
        y = new BigInteger(reader.readLine());

        publicKey = p.toString() + "\n" + g.toString() + "\n" + y.toString();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
      try {
        reader = new BufferedReader(new FileReader(privateKeyFile));

        // privatekey
        p = new BigInteger(reader.readLine());
        g = new BigInteger(reader.readLine());
        x = new BigInteger(reader.readLine());

        privateKey = p.toString() + "\n" + g.toString() + "\n" + x.toString();
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }

      Logger("Reading Information: ");
      Logger("+--publicKey: " + publicKey);
      Logger("+--privateKey: " + privateKey);

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
    while (M != null) {
      // random two <= k < p-1
      BigInteger k = BigIntegerUtil.randomBetween(BigIntegerUtil.TWO, p.subtract(BigIntegerUtil.ONE), sc);

      BigInteger a = g.modPow(k, p); // g^k mod p
      BigInteger b = M.multiply(y.modPow(k, p)).mod(p); // (M * y^k mod p) mod p

      BigInteger C = a.add(b.multiply(p)); // a + b*p

      writeCipher(ciphertext, C);

      M = readClear(cleartext, L);
    }

  }

  public void decipher(FileInputStream ciphertext, FileOutputStream cleartext) {
    // read ciphertext
    BigInteger C = readCipher(ciphertext);
    while (C != null) {
      BigInteger a = C.mod(p);
      BigInteger b = C.divide(p);

      BigInteger exponent = p.subtract(BigInteger.ONE).subtract(x);
      BigInteger z = a.modPow(exponent, p); // a^(p-1-x) mod p

      BigInteger M = z.multiply(b).mod(p); // M = z * b mod p

      writeClear(cleartext, M);
      
      C = readCipher(ciphertext);
    }
  }

  private void Logger(String event) {
    System.out.println("ElGamal$  " + event);
  }

}
