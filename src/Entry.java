public class Entry implements Comparable<Entry> {
	int key = 0;
	String value = "";
	String note = "";
	@Override
	public int compareTo(Entry e) {
		return this.key - e.key;
	}
	@Override
	public String toString() {
		String s = "Episode " + this.key + ": " + this.value;
		if (note.length() > 0) {
			s += " (Note: " + this.note + ")";
		}
		return s;
	}
}