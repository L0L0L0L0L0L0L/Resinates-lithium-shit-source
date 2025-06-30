
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

public class KaoticCodeGenerator {

    private record CodeInfo(int itemId, int amount, int price) {

    }

    private static final Map<CodeInfo, Integer> CODE_AMOUNTS = new HashMap<>();

    static {
        CODE_AMOUNTS.put(new CodeInfo(4310505, 10, 10), 999);
	CODE_AMOUNTS.put(new CodeInfo(4310505, 25, 25), 999);
	CODE_AMOUNTS.put(new CodeInfo(4310505, 50, 50), 999);
	CODE_AMOUNTS.put(new CodeInfo(4310505, 100, 100), 999);
    }

    public static void main(String[] args) throws FileNotFoundException {
        Scanner in = new Scanner(System.in);
        System.out.print("Output folder : ");
        File outputFolder = new File(in.nextLine());
        if (!outputFolder.exists()) {
            outputFolder.mkdir();
        }

        SecureRandom rand = new SecureRandom();
        Map<CodeInfo, List<String>> codes = new HashMap<>();
        for (CodeInfo ci : CODE_AMOUNTS.keySet()) {
            int count = CODE_AMOUNTS.get(ci);
            for (int i = 0; i < count; i++) {
                codes.computeIfAbsent(ci, key -> new ArrayList<>()).add(generateCode(rand));
            }
        }

        for (CodeInfo ci : CODE_AMOUNTS.keySet()) {
            try (PrintWriter out = new PrintWriter(new File(outputFolder.getName() + File.separator + ci.itemId + "_" + ci.amount + "_" + ci.price + ".txt"))) {
                List<String> codeList = codes.get(ci);
                codeList.forEach(out::println);
                out.println();
            }
        }

        try (PrintWriter out = new PrintWriter(new File(outputFolder.getName() + File.separator + "all.sql"))) {
            out.println("INSERT INTO sellix_available_codes (code, itemid, amount) VALUES ");
            for (Entry<CodeInfo, List<String>> e : codes.entrySet()) {
                e.getValue().forEach(c -> out.println("('" + c + "', " + e.getKey().itemId + ", " + e.getKey().amount + "),"));
            }
        }
        System.out.println("Done.");
    }

    private static String generateCode(SecureRandom rand) {
        final String CODE_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < 24; i++) {
            sb.append(CODE_CHARS.charAt(rand.nextInt(CODE_CHARS.length())));
        }
        return sb.toString();
    }

}
