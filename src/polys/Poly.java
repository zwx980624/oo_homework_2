package polys;

import factors.Factor;
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
                if (termList.get(pos).getCoef().equals(BigInteger.ZERO)) {
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
                if (termList.get(pos).getCoef().equals(BigInteger.ZERO)) {
                    termList.remove(pos);
                }
            } else {
                if (!t.getCoef().equals(BigInteger.ZERO)) {
                    termList.add(t);
                }
            }
        }
    }

    public ArrayList<Term> getTermList() {
        return (ArrayList<Term>) termList.clone();
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
        //去掉sin cos后看看有无其余非法字符
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

    // deal with aAsin(x)^2+bBcos(x)^2
    public Poly shorter() {
        ArrayList<Term> tl = this.getTermList();
        int time = 5; //最多查5次保证时间
        while (time != 0) {
            time--;
            ArrayList<Term> exSin2 = new ArrayList<>(); //保存提取Sinx2后剩下的项
            ArrayList<Term> exCos2 = new ArrayList<>(); //保存提取Cosx2后剩下的项
            ArrayList<Integer> posSin = new ArrayList<>(); //匹配的下标
            ArrayList<Integer> posCos = new ArrayList<>();
            Term sin2 = new Term("sin(x)^2");
            Term cos2 = new Term("cos(x)^2");
            for (Term t : tl) {
                exSin2.add(t.extract(sin2));
                exCos2.add(t.extract(cos2));
            }
            compfunc(exSin2, exCos2, posSin, posCos);
            if (posSin.size() == 0) {
                break;
            }
            boolean flag = false;
            for (int i = 0; i < posSin.size(); ++i) {
                int oriLen = tl.get(posSin.get(i)).toString().length() +
                        tl.get(posCos.get(i)).toString().length();
                Term axA = exSin2.get(posSin.get(i));
                Term bxA = exCos2.get(posCos.get(i));
                BigInteger a = axA.getCoef();
                BigInteger b = bxA.getCoef();
                // CosTerm = (b-a)*A*cos(x)^2
                // SinTerm = (a-b)*A*sin(x)^2
                Term cosTerm = new Term(b.subtract(a),
                        tl.get(posCos.get(i)).getFactList());
                Term sinTerm = new Term(a.subtract(b),
                        tl.get(posSin.get(i)).getFactList());
                // CosPoly: a*A + (b-a)*A*cos(x)^2
                // SinPoly: b*A + (a-b)*A*sin(x)^2
                int cosPolyLen = axA.toString().length() +
                        cosTerm.toString().length();
                int sinPolyLen = bxA.toString().length() +
                        sinTerm.toString().length();
                if (cosPolyLen < oriLen) {
                    flag = true;
                    if (sinPolyLen < cosPolyLen) { // s < c < o
                        tl.set(posSin.get(i), bxA);
                        tl.set(posCos.get(i), sinTerm);
                    } else { // c < s < o || c < o < s
                        tl.set(posSin.get(i), axA);
                        tl.set(posCos.get(i), cosTerm);
                    }
                } else if (sinPolyLen < oriLen) { // s < o < c
                    flag = true;
                    tl.set(posSin.get(i), bxA);
                    tl.set(posCos.get(i), sinTerm);
                }
            }
            if (!flag) {
                break;
            }
            tl = new Poly(tl).getTermList(); //利用构造函数合并同类项
        }
        return new Poly(tl);
    }

    private void compfunc(ArrayList<Term> exSin2, ArrayList<Term> exCos2,
                      ArrayList<Integer> posSin, ArrayList<Integer> posCos) {
        for (int i = 0; i < exSin2.size(); i++) {
            for (int j = 0; j < exCos2.size(); j++) {
                if (exSin2.get(i) != null && exCos2.get(j) != null &&
                        exSin2.get(i).isMergeable(exCos2.get(j)) &&
                        !posCos.contains(i) && !posSin.contains(j)) { //防重复
                    posSin.add(i);
                    posCos.add(j);
                }
            }
        }
    }

    // deal with aA + bAcos(x)^2
    public Poly shorter2() {
        ArrayList<Term> tl = this.getTermList();
        int time = 5; //最多查5次保证时间
        while (time != 0) {
            time--;
            ArrayList<Term> exOne = new ArrayList<>(); //保存提取1后剩下的项
            ArrayList<Term> exCos2 = new ArrayList<>(); //保存提取Cosx2后剩下的项
            ArrayList<Integer> posOne = new ArrayList<>(); //匹配的下标
            ArrayList<Integer> posCos = new ArrayList<>();
            Term one = new Term("1");
            Term cos2 = new Term("cos(x)^2");
            for (Term t : tl) {
                exOne.add(t.extract(one));
                exCos2.add(t.extract(cos2));
            }
            compfunc(exOne, exCos2, posOne, posCos);
            if (posOne.size() == 0) {
                break;
            }
            boolean flag = false;
            for (int i = 0; i < posOne.size(); ++i) {
                int oriLen = tl.get(posOne.get(i)).toString().length() +
                        tl.get(posCos.get(i)).toString().length();
                Term axA = exOne.get(posOne.get(i));
                Term bxA = exCos2.get(posCos.get(i));
                BigInteger a = axA.getCoef();
                BigInteger b = bxA.getCoef();
                //make (a+b)A
                Term abxA = axA.merge(bxA);
                // make -bxAsin2
                Factor ftmp = Factor.factorFactory("sin(x)^2");
                ArrayList<Factor> fltmp = bxA.getFactList();
                fltmp.add(ftmp);
                Term nbxAsin2 = new Term(bxA.getCoef().negate(), fltmp);
                if (a.add(b).equals(BigInteger.ZERO) ||
                        abxA.toString().length() +
                                nbxAsin2.toString().length() < oriLen) {
                    tl.set(posOne.get(i), axA.merge(bxA));
                    tl.set(posCos.get(i), nbxAsin2);
                    flag = true;
                }
            }
            if (!flag) {
                break;
            }
            tl = new Poly(tl).getTermList(); //利用构造函数合并同类项
        }
        return new Poly(tl);
    }

    // deal with aA + bAsin(x)^2
    public Poly shorter3() {
        ArrayList<Term> tl = this.getTermList();
        int time = 5; //最多查5次保证时间
        while (time != 0) {
            time--;
            ArrayList<Term> exOne = new ArrayList<>(); //保存提取1后剩下的项
            ArrayList<Term> exSin2 = new ArrayList<>(); //保存提取Sinx2后剩下的项
            ArrayList<Integer> posOne = new ArrayList<>(); //匹配的下标
            ArrayList<Integer> posSin = new ArrayList<>();
            Term one = new Term("1");
            Term cos2 = new Term("sin(x)^2");
            for (Term t : tl) {
                exOne.add(t.extract(one));
                exSin2.add(t.extract(cos2));
            }
            compfunc(exOne, exSin2, posOne, posSin);
            if (posOne.size() == 0) {
                break;
            }
            boolean flag = false;
            for (int i = 0; i < posOne.size(); ++i) {
                int oriLen = tl.get(posOne.get(i)).toString().length() +
                        tl.get(posSin.get(i)).toString().length();
                Term axA = exOne.get(posOne.get(i));
                Term bxA = exSin2.get(posSin.get(i));
                BigInteger a = axA.getCoef();
                BigInteger b = bxA.getCoef();
                //make (a+b)A
                Term abxA = axA.merge(bxA);
                // make bxAcos2
                Factor ftmp = Factor.factorFactory("cos(x)^2");
                ArrayList<Factor> fltmp = bxA.getFactList();
                fltmp.add(ftmp);
                Term nbxAcos2 = new Term(bxA.getCoef().negate(), fltmp);
                if (a.add(b).equals(BigInteger.ZERO) ||
                        abxA.toString().length() +
                                nbxAcos2.toString().length() < oriLen) {
                    tl.set(posOne.get(i), axA.merge(bxA));
                    tl.set(posSin.get(i), nbxAcos2);
                    flag = true;
                }
            }
            if (!flag) {
                break;
            }
            tl = new Poly(tl).getTermList(); //利用构造函数合并同类项
        }
        return new Poly(tl);
    }

    // deal with Asin(x)^4 - Acos(x)^4
    public Poly shorter4() {
        ArrayList<Term> tl = this.getTermList();
        int time = 5; //最多查5次保证时间
        while (time != 0) {
            time--;
            ArrayList<Term> exSin4 = new ArrayList<>(); //保存提取Sinx2后剩下的项
            ArrayList<Term> exCos4 = new ArrayList<>(); //保存提取Cosx2后剩下的项
            ArrayList<Integer> posSin = new ArrayList<>(); //匹配的下标
            ArrayList<Integer> posCos = new ArrayList<>();
            Term sin4 = new Term("sin(x)^4");
            Term ncos4 = new Term("-cos(x)^4");
            for (Term t : tl) {
                exSin4.add(t.extract(sin4));
                exCos4.add(t.extract(ncos4));
            }
            compeqfunc(exSin4, exCos4, posSin, posCos);
            if (posSin.size() == 0) {
                break;
            }
            for (int i = 0; i < posSin.size(); ++i) {
                Term termA = exSin4.get(posSin.get(i));
                ArrayList<Factor> fltmp = termA.getFactList();
                fltmp.add(Factor.factorFactory("sin(x)^2"));
                Term termAs2 = new Term(termA.getCoef(), fltmp);
                tl.set(posSin.get(i), termAs2);
                fltmp = termA.getFactList();
                fltmp.add(Factor.factorFactory("cos(x)^2"));
                Term termAc2 = new Term(termA.getCoef().negate(), fltmp);
                tl.set(posCos.get(i), termAc2);
            }
            tl = new Poly(tl).getTermList(); //利用构造函数合并同类项
        }
        return new Poly(tl);
    }

    private void compeqfunc(ArrayList<Term> exSin2, ArrayList<Term> exCos2,
                        ArrayList<Integer> posSin, ArrayList<Integer> posCos) {
        for (int i = 0; i < exSin2.size(); i++) {
            for (int j = 0; j < exCos2.size(); j++) {
                if (exSin2.get(i) != null && exCos2.get(j) != null &&
                        exSin2.get(i).equals(exCos2.get(j)) &&
                        !posCos.contains(i) && !posSin.contains(j)) { //防重复
                    posSin.add(i);
                    posCos.add(j);
                }
            }
        }
    }
}