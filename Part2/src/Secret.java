import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Secret {
  private ArrayList<BigInteger> binaries;
  private BigInteger word;

  private int k;
  private int m;

  public ArrayList<BigInteger> getBinaries() {
    return binaries;
  }

  public void setPrefixe(ArrayList<BigInteger> binaries) {
    this.binaries = binaries;
  }

  public BigInteger getWord() {
    return word;
  }

  public void setWord(BigInteger word) {
    this.word = word;
  }

  public Secret(BigInteger word, int k, int m) {
    this.word = word;
    this.k = k;
    this.m = m;

    if (word.bitLength() > m) {
      System.out.println("Problem: Bitlength of word (" + word.bitLength() + ") is bigger than m (" + m + ")! Exiting...");
      System.exit(0);
    }

    this.binaries = new ArrayList<BigInteger>();

    makeBinaries();
  }

  private void makeBinaries() {
    int n = (int) Math.pow(2, k + 1); // number of prefixes

    BigInteger counter = new BigInteger("0");
    for (int i = 0; i < n; i++) {
      binaries.add(counter);

      counter = counter.add(BigInteger.ONE);
    }
  }

  public boolean containsWord() {
    boolean isWord = false;
    Iterator<BigInteger> it = binaries.iterator();
    while (it.hasNext()) {
      BigInteger current = it.next();

      if (current.equals(word)) {
        isWord = true;
      }
    }

    return isWord;
  }

  private boolean isPrefix(BigInteger val) {
    boolean isPrefix = false;

    int shift = m - (k + 1);
    BigInteger modifiedWord = word.shiftRight(shift);

    if (val.equals(modifiedWord)) {
      isPrefix = true;
    }
    return isPrefix;
  }

  public String binariesToString(ArrayList<BigInteger> myBinaries, int radix) {
    Iterator<BigInteger> it = myBinaries.iterator();

    String output = "";
    while (it.hasNext()) {
      BigInteger current = it.next();
      output += current.toString(radix) + ", ";
    }

    return output;
  }
  
  public BigInteger getLastBinary() {
    return binaries.get(0);
  }
  
  public String binariesToString() {
    return binariesToString(binaries, 36);
  }

  public void debug() {
//    System.out.println("word: " + word.toString(36));
//    System.out.println("binaries: " + binariesToString(binaries, 36));

  }

  public int removeRandomBinary() {
    Random rnd = new Random();

    // remove only those binaries that are no prefix of the word!!!
    int rndIndex = rnd.nextInt(binaries.size());
    while (isPrefix(binaries.get(rndIndex))) {
      rndIndex = rnd.nextInt(binaries.size());
    }

//    System.out.println("removeRandomPrefix: binary removed: " + binaries.get(rndIndex).toString(2));

    binaries.remove(rndIndex);

    //System.out.println("removeRandomPrefix: remaining binaries: " + binariesToString(binaries, 2));

    return rndIndex;
  }

  public void removeBinary(int index) {
    //System.out.println("removeBinary: binary removed: " + binaries.get(index).toString(2));

    binaries.remove(index);

    //System.out.println("removeBinary: remaining binaries: " + binariesToString(binaries, 2));
  }

  private ArrayList<BigInteger> generateNewBinaries(ArrayList<BigInteger> myBinaries) {
    ArrayList<BigInteger> oldBinaries = myBinaries;
    ArrayList<BigInteger> newBinaries = new ArrayList<BigInteger>();

    Iterator<BigInteger> it = oldBinaries.iterator();
    while (it.hasNext()) {
      BigInteger current = it.next();

      BigInteger new1 = current.shiftLeft(1); // append 0 from right
      BigInteger new2 = current.shiftLeft(1).add(BigInteger.ONE); // append 1 from right

      // System.out.println("old: " + current.toString(2));
      // System.out.println("new1: " + new1.toString(2));
      // System.out.println("new2: " + new2.toString(2));

      newBinaries.add(new1);
      newBinaries.add(new2);
    }

    // System.out.println("old binaries: " + binariesToString(oldBinaries));
    // System.out.println("new binaries: " + binariesToString(newBinaries));

    return newBinaries;
  }

  public void expandBinaries() {
    binaries = generateNewBinaries(binaries);
    k += 1;
  }
}
