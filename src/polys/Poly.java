package polys;

import terms.Term;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Poly {
    private ArrayList<Term> termList;

    public Poly(ArrayList<Term> tl) {
        termList = new ArrayList<Term>();
        for (Term t : tl) {
            int pos = findSameTerm(termList, t);
            if (pos != -1) {
                termList.set(pos, termList.get(pos).merge(t));
                if (termList.get(pos).equals(BigInteger.ZERO)) {
                    termList.remove(pos);
                }
            } else {
                if (!t.getCoef().equals(BigInteger.ZERO)) {
                    termList.add(t);
                }
            }
        }
    }

    // 可输入非法字符串，抛出异常
    public Poly(String str1) throws NumberFormatException {
        String str = str1;
        termList = new ArrayList<>();
        //先去掉空格干扰，并检测非法字符
        if (!checkSpaceLegal(str)) {
            throw new NumberFormatException();
        }
        str = str.replaceAll("[ \\t]+", "");
        if (str.equals("")) {
            throw new NumberFormatException();
        }
        // 首项之前保证有符号，统一好处理
        if (str.charAt(0) != '+' && str.charAt(0) != '-') {
            str = "+" + str;
        }
        // 从左到右处理，找到每一个项
        while (!str.equals("")) {
            String temp = getFirstLegalTerm(str);
            if (temp.equals("")) {
                throw new NumberFormatException();
            }
            str = str.substring(temp.length());
            temp = temp.replaceAll("\\+\\+|\\-\\-", "\\+");
            temp = temp.replaceAll("\\-\\+|\\+\\-", "\\-");
            Term t = new Term(temp);
            int pos = findSameTerm(termList, t);
            if (pos != -1) {
                termList.set(pos, termList.get(pos).merge(t));
                if (termList.get(pos).equals(BigInteger.ZERO)) {
                    termList.remove(pos);
                }
            } else {
                if (!t.getCoef().equals(BigInteger.ZERO)) {
                    termList.add(t);
                }
            }
        }
    }

    public String getFirstLegalTerm(String str) {
        String numStr = "[\\+\\-]?\\d+";
        String powerFunStr = "x(\\^" + numStr + ")?";
        String sinFunStr = "sin\\(x\\)(\\^" + numStr + ")?";
        String cosFunStr = "cos\\(x\\)(\\^" + numStr + ")?";
        String factorStr = "(" + numStr + ")|(" + powerFunStr + ")|(" +
                sinFunStr + ")|(" + cosFunStr + ")";
        String termStr = "^[\\+\\-]{1,2}(\\d+\\*)?((" + factorStr +
                ")\\*)*+(" + factorStr + ")";
        //System.out.println(termStr);
        Pattern r = Pattern.compile(termStr);
        Matcher m = r.matcher(str);
        if (m.find()) {
            //System.out.println("First");
            return m.group(0);
        } else {
            //System.out.println("Second");
            return "";
        }
    }

    private boolean checkSpaceLegal(String str1) {
        String str = str1;
        //首先看数字间有无空格
        String spaceInNum = "(\\d[ \\t]+\\d)" +
                "|([\\*\\^][ \\t]*[\\+\\-][ \\t]+\\d)" +
                "|(([\\+\\-][ \\t]*){2}[\\+\\-][ \\t]+\\d)";
        Pattern r = Pattern.compile(spaceInNum);
        Matcher m = r.matcher(str);
        if (m.find()) {
            return false;
        }
        //去掉sin(x)cos(x)后看看有无其余非法字符
        str = str.replaceAll("sin", "");
        str = str.replaceAll("cos", "");
        r = Pattern.compile("[^x \\t\\d\\^\\+\\-\\*\\(\\)]");
        m = r.matcher(str);
        if (m.find()) {
            return false;
        }

        return true;
    }

    private int findSameTerm(ArrayList<Term> tl, Term t) {
        for (int i = 0; i < tl.size(); ++i) {
            if (tl.get(i).isMergeable(t)) {
                return i;
            }
        }
        return -1;
    }

    public Poly diff() {
        ArrayList<Term> tl = new ArrayList<>();
        for (Term t : termList) {
            tl.addAll(t.diff().termList);
        }
        return new Poly(tl);
    }

    public String toString() {
        ArrayList<String> termStrs = new ArrayList<>();
        StringBuilder ret = new StringBuilder();
        int ppos = -1;
        for (int i = 0; i < termList.size(); i++) {
            String temp = termList.get(i).toString();
            //System.out.println(temp);
            if (!temp.equals("0")) {
                if (temp.charAt(0) != '-') {
                    temp = "+" + temp;
                    if (ppos == -1) {
                        ppos = termStrs.size();
                    }
                }
                termStrs.add(temp);
            }
        }
        if (ppos != -1) {
            //String temp = termStrs.get(ppos);
            //termStrs.remove(ppos);
            //termStrs.add(0,temp);
            Collections.swap(termStrs, 0, ppos);
            termStrs.set(0, termStrs.get(0).replaceFirst("\\+", ""));
        }
        for (String e : termStrs) {
            ret.append(e);
        }
        if (ret.toString().equals("")) {
            return "0";
        } else {
            return ret.toString();
        }
    }

    public Poly shorter() {
        ArrayList<Term> tl = (ArrayList<Term>) termList.clone();
        while (true) {
            ArrayList<Term> exSin2 = new ArrayList<>();
            ArrayList<Term> exCos2 = new ArrayList<>();
            Term sin2 = new Term("sin(x)^2");
            Term cos2 = new Term("cos(x)^2");
            for (Term t : tl) {
                exSin2.add(t.extract(sin2));
                exCos2.add(t.extract(cos2));
            }
            int i = 0;
            int j = 0;
            boolean flag = false;
            for (i = 0; i < exSin2.size(); i++) {
                for (j = 0; j < exCos2.size(); j++) {
                    if (exSin2.get(i) != null &&
                            exCos2.get(j) != null &&
                            exSin2.get(i).equals(exCos2.get(j))) {
                        flag = true;
                        break;
                    }
                }
                if (flag) {
                    break;
                }
            }
            if (i == exSin2.size()) {
                break;
            }
            assert i != j;
            tl.set(i, exSin2.get(i));
            tl.remove(j);
        }
        return new Poly(tl);
    }
}