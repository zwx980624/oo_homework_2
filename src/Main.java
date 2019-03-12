import polys.Poly;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    private static Poly shortProc(Poly poly)
    {
        return poly.shorter().shorter2().shorter().shorter3().
                shorter().shorter4().shorter();
    }

    public static void main(String[] args) {
        try (Scanner in = new Scanner(System.in)) {
            String str = in.nextLine();
            Poly poly = new Poly(str);
            poly = shortProc(poly);
            poly = poly.diff();
            System.out.println(shortProc(poly));
        } catch (NumberFormatException | NoSuchElementException e) {
            System.out.println("WRONG FORMAT!");
        }
    }
}
