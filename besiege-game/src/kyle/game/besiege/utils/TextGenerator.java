package kyle.game.besiege.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Scanner;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import kyle.game.besiege.NameGenerator;

/**********************************************************************
 *    Name:     Kyle Dhillon and Keshav Singh
 *    Login:    kdhillon, keshavs
 *    Precept:  PO2A, P02
 * 
 *    Takes two command-line integers k and T, reads the input text from
 *    standard input, builds a Markov model of order k from the input text
 *    and prints out T characters generated by simulating a trajectory
 *    through the corresponding Markov chain.
 *
 * 
 *    Dependencies: MarkovModel.java, StdIn.java
 * 
 *    Execution 
 * 
 *********************************************************************/

public class TextGenerator {
	MarkovModel mm;

//	public TextGenerator(String filename) {
//		this(2, filename);
//	}

	public TextGenerator(int k, String path, String filename) throws FileNotFoundException {
	    this(k, new Scanner(Gdx.files.internal(path+filename).reader()));
	}

	public TextGenerator(int k, Scanner in2) {
		//		Scanner in = new Scanner(Gdx.files.internal("data/units/standard_biomes.txt").reader()).useDelimiter("\\t\\t*|\r\n[\r\n]*");
		//		String text = "Chicago Atlanta Bermuda Bahamas Detroit";       //reads text from standard input
		//		BufferedReader in = new BufferedReader(file.reader());
		StringBuilder total = new StringBuilder("");
		while (in2.hasNext()) {
			total.append(in2.nextLine() + " ");
		}
		in2.close();
		String text = total.toString();
		//		System.out.println(text);
		//		String kgram = text.substring(0, k); // create kgram
		mm = new MarkovModel(text, k); //builds a Markov model
	}

	public String gen(int minLength, int maxLength, int minWordLength) {
		String raw = "";
		maxLength = minLength + (int) (Math.random() * (maxLength - minLength) + 2);
		int minWordLengthSeen = 1000;
		while (raw.length() < minLength || minWordLengthSeen < minWordLength || raw.length() > maxLength) {
			minWordLengthSeen = 1000;
			raw = mm.gen("", maxLength);
//			System.out.println(raw);
			for (int cutoff = maxLength - 1; cutoff > 0; cutoff--) {
				if (raw.charAt(cutoff) == ' ' || raw.charAt(cutoff) == '-') {
					raw = raw.substring(0, cutoff);
					//					break;
				}
			}
			String[] sep = raw.split(" ");
			for (String s : sep) {
//				System.out.println(s);
				if (s.length() < minWordLengthSeen) minWordLengthSeen = s.length();
//				System.out.println(minWordLengthSeen);
			}
		}
		return raw;
	}

	public static void main(String[] args) {
//		TextGenerator tg = new TextGenerator(4, new FileHandle("A:/users/kyle/dropbox/libgdx/repo/besiege/besiege-game-android/assets/data/namegen/cities15000.txt"));
//
//		TextGenerator tgMale = new TextGenerator(3, new FileHandle("A:/users/kyle/dropbox/libgdx/repo/besiege/besiege-game-android/assets/data/namegen/firstMale.txt"));
//		TextGenerator tgFemale = new TextGenerator(3, new FileHandle("A:/users/kyle/dropbox/libgdx/repo/besiege/besiege-game-android/assets/data/namegen/firstFemale.txt"));
//		TextGenerator tgLast = new TextGenerator(3, new FileHandle("A:/users/kyle/dropbox/libgdx/repo/besiege/besiege-game-android/assets/data/namegen/last.txt"));
//
//		System.out.println("Men:");
//		for (int i = 0; i < 30; i++) {
//			System.out.println( NameGenerator.lowercase(tgMale.gen(5, 10, 5)) + " " +  NameGenerator.lowercase(tgLast.gen(5, 10, 5))); //prints out T characters
//		}
//		System.out.println("Women:");
//		for (int i = 0; i < 30; i++) {
//			System.out.println( NameGenerator.lowercase(tgFemale.gen(5, 10, 5)) + " " +  NameGenerator.lowercase(tgLast.gen(5, 10, 5))); //prints out T characters
//		}

	}
}