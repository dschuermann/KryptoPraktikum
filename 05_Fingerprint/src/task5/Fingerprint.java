/*
 * jCrypt - Programmierumgebung für das Kryptologie-Praktikum
 * Studienarbeit am Institut für Theoretische Informatik der
 * Technischen Universität Braunschweig
 * 
 * Datei:        Fingerprint.java
 * Beschreibung: Dummy-Implementierung der Hash-Funktion von Chaum, van Heijst
 *               und Pfitzmann
 * Erstellt:     30. März 2010
 * Autor:        Martin Klußmann
 */

package task5;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Random;

import de.tubs.cs.iti.jcrypt.chiffre.BigIntegerUtil;
import de.tubs.cs.iti.jcrypt.chiffre.HashFunction;

/**
 * Dummy-Klasse für die Hash-Funktion von Chaum, van Heijst und Pfitzmann.
 * 
 * @author Martin Klußmann
 * @version 1.1 - Sat Apr 03 22:20:18 CEST 2010
 */
public final class Fingerprint extends HashFunction {

  public BigInteger p;
  public BigInteger g1;
  public BigInteger g2;
  String paramString;

  BigInteger ZERO = BigInteger.ZERO;
  BigInteger ONE = BigInteger.ONE;
  BigInteger TWO = BigIntegerUtil.TWO;

  /**
   * Berechnet den Hash-Wert des durch den FileInputStream <code>cleartext</code> gegebenen
   * Klartextes und schreibt das Ergebnis in den FileOutputStream <code>ciphertext</code>.
   * 
   * @param cleartext
   *          Der FileInputStream, der den Klartext liefert.
   * @param ciphertext
   *          Der FileOutputStream, in den der Hash-Wert geschrieben werden soll.
   */
  public void hash(FileInputStream cleartext, FileOutputStream ciphertext) {
    System.out.println(paramString);
    
    
  }

  /**
   * Erzeugt neue Parameter.
   * 
   * @see #readParam readParam
   * @see #writeParam writeParam
   */
  public void makeParam() {
    Random sc = new SecureRandom();
    int k = 512; // prime number with k=512 bits
    int certainty = 100; // The probability that the new BigInteger represents a prime number will
                         // exceed (1-1/2^certainty)

    p = null;
    BigInteger q = null;
    do {
      q = new BigInteger(k - 1, certainty, sc);
      p = q.multiply(TWO).add(ONE); // secure prime p = 2q+1
    } while (!p.isProbablePrime(certainty));

    BigInteger MINUS_ONE = ONE.negate().mod(p); // -1 mod p

    long start = System.currentTimeMillis();

    g1 = null;
    BigInteger factor = null;
    do {
      // 2 <= g < p-1
      g1 = BigIntegerUtil.randomBetween(TWO, p.subtract(ONE), sc);
      factor = g1.modPow(q, p);
    } while (!factor.equals(MINUS_ONE));

    System.out.println("g1: " + g1);

    g2 = null;
    BigInteger factor2 = null;
    do {
      do {
        // 2 <= g < p-1
        g2 = BigIntegerUtil.randomBetween(TWO, p.subtract(ONE), sc);
        factor2 = g2.modPow(q, p);
      } while (!factor2.equals(MINUS_ONE));
    } while (g1.equals(g2));

    long end = System.currentTimeMillis();

    System.out.println("Time: " + (end - start));

    paramString = p + "\n" + g1 + "\n" + g2;
  }

  /**
   * Liest die Parameter mit dem Reader <code>param</code>.
   * 
   * @param param
   *          Der Reader, der aus der Parameterdatei liest.
   * @see #makeParam makeParam
   * @see #writeParam writeParam
   */
  public void readParam(BufferedReader param) {
    try {
      // pubkey
      p = new BigInteger(param.readLine());
      g1 = new BigInteger(param.readLine());
      g2 = new BigInteger(param.readLine());

      paramString = p + "\n" + g1 + "\n" + g2;
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Berechnet den Hash-Wert des durch den FileInputStream <code>cleartext</code> gegebenen
   * Klartextes und vergleicht das Ergebnis mit dem durch den FileInputStream
   * <code>ciphertext</code> gelieferten Wert.
   * 
   * @param ciphertext
   *          Der FileInputStream, der den zu prüfenden Hash-Wert liefert.
   * @param cleartext
   *          Der FileInputStream, der den Klartext liefert, dessen Hash-Wert berechnet werden soll.
   */
  public void verify(FileInputStream ciphertext, FileInputStream cleartext) {

  }

  /**
   * Schreibt die Parameter mit dem Writer <code>param</code>.
   * 
   * @param param
   *          Der Writer, der in die Parameterdatei schreibt.
   * @see #makeParam makeParam
   * @see #readParam readParam
   */
  public void writeParam(BufferedWriter param) {
    try {
      param.write(paramString);
      param.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
