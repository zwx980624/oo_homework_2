import factors.Factor;
import polys.Poly;
import terms.Term;

import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Main
{
    public static void main(String[] args)
    {
        try (Scanner in = new Scanner(System.in))
        {
            String str = in.nextLine();
            Poly poly = new Poly(str);
            //poly = poly.diff();
            System.out.println(poly);
            System.out.println(poly.shorter());
//            String pattern = "(\\d\\*)*\\d";
//            String str = "5";
//            Pattern r = Pattern.compile(pattern);
//            Matcher m = r.matcher(str);
//            if (m.find()) //先匹配非数字形项
//            {
//                System.out.println("yes");
//            }
        }
        catch (NumberFormatException e)
        {
            System.out.println("WRONG FORMAT!");
        }
    }
}
