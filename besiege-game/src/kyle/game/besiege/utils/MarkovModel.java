package kyle.game.besiege.utils;

/**********************************************************************
 *    Name:     Kyle Dhillon and Keshav Singh
 *    Login:    kdhillon, keshavs
 *    Precept:  PO2A, P02
 * 
 *    Markov Model data type with given text and order.
 * 
 *    Dependencies: none
 * 
 *********************************************************************/
public class MarkovModel {
    private static int k; // order of Markov Model
    private static int ASCII = 128; // number of possible chars we will use
    // create symbol table for each k-gram
    private ST<String, int[]> st = new ST<String, int[]>();
    
    // constructor for Markov model with input text and order k
    public MarkovModel(String words, int order) {
        String text = words;
        k = order; // order of Markov Model
        int length = text.length(); // length of text
        String suffix = text.substring(0, k); // first k characters of text
        text = text + suffix; // appends suffix to end to create "circular" text
        
        st = new ST<String, int[]>(); // initialize symbol table
        
        // populate symbol table with every k-gram in the text, each with a
        // value that is an empty array of size 128 to hold frequencies
        for (int i = 0; i < length; i++) {
            String kgram = text.substring(i, i+k);
            // the first time you see a certain k-gram (no duplicates)
            if (!st.contains(kgram)) {
                int[] intArray = new int[ASCII];
                st.put(kgram, intArray);
            }
            char c = text.charAt(i+k); // the following character
            st.get(kgram)[c]++;
        }
    }
    
    // return the order of the Markov model
    public int order() {
        return k;
    }
    
    // returns the number of times the kgram was found in original text
    public int freq(String kgram) {
        if (kgram.length() != k) 
            throw new RuntimeException("Invalid kgram length");
        
        int total = 0; // total frequency of kgram
        for (int i = 0; i < ASCII; i++) 
            total += st.get(kgram)[i]; // add number of occurances to total
        
        return total;
    }
    
    // returns the number of times the kgram was followed by character c
    public int freq(String kgram, char c) {
        if (kgram.length() != k)
            throw new RuntimeException("Invalid kgram length");
        
        return st.get(kgram)[c];
    }
    
    //returns a random character following given kgram
    public char rand(String kgram) {
        if (kgram.length() != k) 
            throw new RuntimeException("Invalid kgram length");
        
        if (!st.contains(kgram)) 
            throw new RuntimeException("No such kgram");
        
        double[] probs = new double[ASCII]; // new array of doubles
        double total = this.freq(kgram); //total frequency of kgram
        
        //populate new double array with probabilities
        for (int i = 0; i < ASCII; i++) 
            probs[i] = st.get(kgram)[i] / total;
        
        return (char) StdRandom.discrete(probs); 
    }
    
    //generates a String of length T characters
    public String gen(String kgram, int T) {
        if (kgram.length() != k) 
            throw new RuntimeException("Invalid kgram length");
        
        StringBuilder sb = new StringBuilder(kgram); // new StringBuilder
        for (int i = k; i < T; i++) {
            String text = sb.toString();
            char c = this.rand(text.substring(i-k, i)); // generate random char
            sb.append(c); // add char to end of string
        }
        return sb.toString();
    }
            
}