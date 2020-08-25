import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Parse {
	public static final String[] name = { "Shinichi Kudo", "Ai Haibara", "Yukiko Kudo", "Heiji Hattori", "Shuichi Akai",
			"Black Org", "Kaitou Kid", "Jodie Starling", "Vermouth", "Rei Furuya", "FBI" };
	public static final String prefix = "https://www.detectiveconanworld.com/wiki/";
	public static final String suffix = "_Appearances";
	public static final String[] web = { "Shinichi_Kudo", "Ai_Haibara", "Yukiko_Kudo", "Heiji_Hattori", "Shuichi_Akai",
			"Black_Organization", "Kaitou_Kid", "Jodie_Starling", "Vermouth", "Rei_Furuya", "FBI" };
	public static final Pattern p = Pattern.compile("^Episode (\\d+): (.*?)(\\(Note: .*?\\))?$");
	public static final Pattern n = Pattern.compile("^\\(Note: (.*)\\)");
	public static void main(String[] args) {
		List<List<Entry>> list = new ArrayList<List<Entry>>(name.length);
		for (int i = 0; i < name.length; i++) {
			parseUrl(list, i);
		}
		writeUrl(list);

	}

	public static boolean hasNext(int[] indices, int[] last) {
		for (int i = 0; i < indices.length; i++) {
			if (indices[i] < last[i]) {
				return true;
			}
		}
		return false;
	}

	public static int getNext(List<List<Entry>> list, int[] indices) {
		int index = -1;
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < indices.length; i++) {
			if (indices[i] < list.get(i).size()) {
				int key = list.get(i).get(indices[i]).key;
				if (key < min) {
					index = i;
					min = key;
				}
			}
		}
		return index;
	}

	public static void write(List<List<Entry>> list) {
		try {
			BufferedWriter br = new BufferedWriter(new FileWriter("episodes.txt"));
			int[] index = new int[list.size()];
			int[] last = new int[list.size()];
			for (int i = 0; i < list.size(); i++) {
				last[i] = list.get(i).size();
			}
			while (hasNext(index, last)) {
				int i = getNext(list, index);
				Entry e = list.get(i).get(index[i]);
				br.write(name[i] + "\t|\t" + e.value + '\n');
				System.out.println(i + " " + e.key + " " + e.value);
				index[i]++;
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
		}
	}

	public static void writeUrl(List<List<Entry>> list) {
		try {
			BufferedWriter br = new BufferedWriter(new FileWriter("episodes.txt"));
			int[] index = new int[list.size()];
			int[] last = new int[list.size()];
			for (int i = 0; i < list.size(); i++) {
				last[i] = list.get(i).size();
			}
			int i = getNext(list, index);
			while (i >= 0) {
				Entry e = list.get(i).get(index[i]);
				br.write(name[i] + "\t|\t" + e + '\n');
				index[i]++;
				i = getNext(list, index);
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
		}
	}

	public static void parse(List<List<Entry>> list, int i) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(i + ".txt"));
			br.readLine();
			String split = " ";
			String line;
			List<Entry> episodes = new ArrayList<Entry>();
			while ((line = br.readLine()) != null) {
				String[] tokens = line.split(split);
				int ep = Integer.parseInt(tokens[1].substring(0, tokens[1].length() - 1));
				Entry e = new Entry();
				e.key = ep;
				e.value = line;
				episodes.add(e);
			}
			list.add(i, episodes);
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("Input CSV file does not exist");
		} catch (IOException e) {
			throw new IllegalArgumentException("Input CSV file could not be closed");
		}
	}

	public static int find(String str, String pattern, int i) {
		int index = i;
		int match = 0;
		while (index < str.length()) {
			if (str.charAt(index++) == pattern.charAt(match++)) {
				if (match == pattern.length()) {
					return index;
				}
			} else {
				match = 0;
			}
		}
		return -1;
	}

	public static void parseUrl(List<List<Entry>> list, int i) {
		Document doc = null;
		try {
			doc = Jsoup.connect(prefix + web[i] + suffix).get();
		} catch (IOException e) {
			System.out.println("Could not connect to wiki page");
		}
		Elements episodeRows = doc.selectFirst("h2:contains(Anime)").nextElementSibling().select("li > a");
		List<Entry> episodes = new ArrayList<Entry>();
		for (Element episode : episodeRows) {
			System.out.println(episode.text());
			Matcher m = p.matcher(episode.text());
			Entry e = new Entry();
			if (m.matches()) {
				e.key = Integer.parseInt(m.group(1).trim());
				e.value = m.group(2).trim();
				if (m.group(3) != null) {
					String note = m.group(3);
					Matcher noteMatcher = n.matcher(note);
					if (noteMatcher.matches()) {
						e.note = noteMatcher.group(1);
					} else {
						System.out.println(m.group(3) + " did not match Pattern n");
					}
				}
				episodes.add(e);
			} else {
				System.out.println(episode.text() + " did not match Pattern p");
			}
		}
		Collections.sort(episodes);
		list.add(episodes);
	}
}
