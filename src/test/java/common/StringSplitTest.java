package common;

public class StringSplitTest {
    public static void main(String[] args) {
        String s = "| testdb | test |          3 |      1 |";
        String[] sl = s.split("[ ]*\\|[ ]*");
        for (String i : sl) {
            System.out.println(i);
        }
    }
}
