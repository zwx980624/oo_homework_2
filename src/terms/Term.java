package terms;

import factors.Factor;
import polys.Poly;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Term {
    private BigInteger coef;
    private ArrayList<Factor> factList; //保证只要构造出Term一定是最简，无指数为0因子

    public Term(BigInteger c, ArrayList<Factor> fl) //new一个fl然后合并同类
    {
        coef = c;
        factList = new ArrayList<Factor>();
        for (Factor f : fl) {
            int pos = findSameFact(factList, f);
            if (pos != -1) {
                factList.set(pos, factList.get(pos).merge(f));
                if (factList.get(pos).getIndex().equals(BigInteger.ZERO)) {
                    factList.remove(pos);
                }
            } else {
                if (!f.getIndex().equals(BigInteger.ZERO)) {
                    factList.add(f);
                }
            }
        }
    }

    //必须传入合法的，在此不做合法性检查
    public Term(String str1) {
        String str = str1;
        coef = BigInteger.ONE;
        factList = new ArrayList<>();
        str = str.replaceAll("\\s+", "");
        String[] factstrs = str.split("\\*");
        // 处理第一非常数项带正负号的情况，去掉符号，并将影响其移入coef中
        if (factstrs[0].charAt(0) == '-' || factstrs[0].charAt(0) == '+') {
            Pattern r = Pattern.compile("([^\\d\\+\\-])|(^[\\+\\-]{2}\\d)");
            Matcher m = r.matcher(factstrs[0]);
            if (m.find()) {
                if (factstrs[0].charAt(0) == '-') {
                    coef = coef.negate();
                }
                factstrs[0] = factstrs[0].substring(1);
            }
        }
        // 遍历构造每一个factor
        for (int i = 0; i < factstrs.length; ++i) {
            //先看是不是常数
            Pattern r = Pattern.compile("[\\+\\-]?\\d+");
            Matcher m = r.matcher(factstrs[i]);
            if (m.matches()) {
                coef = coef.multiply(new BigInteger(factstrs[i]));
                continue;
            }
            //不是常数时，一定是变量因子
            Factor temp = Factor.factorFactory(factstrs[i]); //调用静态工厂方法
            //合并同类变量因子
            int pos = findSameFact(factList, temp);
            if (pos != -1) {
                factList.set(pos, factList.get(pos).merge(temp));
                if (factList.get(pos).getIndex().equals(BigInteger.ZERO)) {
                    factList.remove(pos);
                }
            } else {
                if (!temp.getIndex().equals(BigInteger.ZERO)) {
                    factList.add(temp);
                }
            }
        }
    }

    public BigInteger getCoef() {
        return coef;
    }

    public Poly diff() {
        ArrayList<Term> tl = new ArrayList<>();
        for (int i = 0; i < factList.size(); i++) {
            ArrayList<Factor> fl = (ArrayList<Factor>) factList.clone();
            BigInteger c = coef;
            fl.remove(i);
            Term temp = factList.get(i).diff();
            c = c.multiply(temp.coef);
            fl.addAll(temp.factList);
            tl.add(new Term(c, fl));
        }
        return new Poly(tl);
    }

    private int findSameFact(ArrayList<Factor> fl, Factor f) {
        for (int i = 0; i < fl.size(); i++) {
            if (fl.get(i).getClass() == f.getClass()) {
                return i;
            }
        }
        return -1;
    }

    public Term merge(Term t) throws ClassCastException {
        if (this.isMergeable(t)) {
            return new Term(coef.add(t.coef), factList);
        } else {
            throw new ClassCastException("two term are not Mergeable");
        }
    }

    public boolean isMergeable(Term t) {
        if (this.factList.size() != t.factList.size()) {
            return false;
        }
        for (Factor f : factList) {
            boolean findflag = false;
            for (Factor tf : t.factList) {
                if (f.getClass() == tf.getClass()) {
                    if (f.getIndex().equals(tf.getIndex())) {
                        findflag = true;
                    }
                    break;
                }
            }
            if (!findflag) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        String ret = "";
        if (coef.equals(BigInteger.ZERO)) {
            return "0";
        } else {
            for (Factor f : factList) {
                ret = ret + f.toString() + "*";
            }
            if (coef.equals(new BigInteger("-1"))) {
                if (ret.equals("")) {
                    ret = "-1";
                } else {
                    ret = "-" + ret;
                    ret = ret.substring(0, ret.length() - 1); //去掉结尾乘号
                }
            } else if (coef.equals(BigInteger.ONE)) {
                if (ret.equals("")) {
                    ret = "1";
                } else {
                    ret = ret.substring(0, ret.length() - 1); //去掉结尾乘号
                }
            } else {
                if (ret.equals("")) {
                    ret = coef.toString();
                } else {
                    ret = coef.toString() + "*" + ret;
                    ret = ret.substring(0, ret.length() - 1); //去掉结尾乘号
                }
            }
        }
        return ret;
    }

    //不可除返回null
    public Term extract(Term t) {
        if (t.coef.equals(BigInteger.ZERO)) {
            return null;
        }
        if (!coef.mod(t.coef).equals(BigInteger.ZERO)) {
            return null;
        } else {
            BigInteger c = coef.divide(t.coef);
            ArrayList<Factor> fl = (ArrayList<Factor>) factList.clone();
            for (Factor tf : t.factList) {
                int i;
                for (i = 0; i < fl.size(); i++) {
                    if (fl.get(i).getClass() == tf.getClass()) {
                        fl.set(i, fl.get(i).extract(tf));
                        break;
                    }
                }
                if (i == fl.size()) //没找到
                {
                    fl.add(tf.reciprocal());
                }
            }
            return new Term(c, fl);
        }
    }

    public boolean equals(Term t) {
        return coef.equals(t.coef) && isMergeable(t);
    }
}
