import polys.Poly;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        try (Scanner in = new Scanner(System.in)) {
            String str = in.nextLine();
            Poly poly = new Poly(str);
            poly = poly.diff();
            //System.out.println(poly);
            System.out.println(poly.shorter());
        } catch (NumberFormatException | NoSuchElementException e) {
            System.out.println("WRONG FORMAT!");
        }
    }
}
