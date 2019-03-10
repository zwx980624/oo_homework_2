package factors;

import terms.Term;
import java.math.BigInteger;
import java.util.ArrayList;

public class Xfactor extends Factor {
    public Xfactor(BigInteger idx) {
        super(idx);
    }

    public Xfactor() {
        super();
    }

    //必须传入合法的，在此不做合法性检查
    public Xfactor(String str) {
        super(str);
    }

    public Term diff() {
        ArrayList<Factor> fl = new ArrayList<>();
        BigInteger idx = this.getIndex();
        //指数为1和0时无xfactor项
        if (!idx.equals(BigInteger.ZERO) && !idx.equals(BigInteger.ONE))
        {
            fl.add(new Xfactor(this.getIndex().subtract(BigInteger.ONE)));
        }
        return new Term(this.getIndex(), fl);
    }

    public String toString() {
        if (this.getIndex().equals(BigInteger.ONE)) {
            return "x";
        } else if (this.getIndex().equals(BigInteger.ZERO)) {
            return "1";
        } else {
            return "x^" + this.getIndex().toString();
        }
    }

    public Factor merge(Factor f) throws ClassCastException {
        if (this.getClass() == f.getClass()) {
            return new Xfactor(basedMerge(f));
        } else {
            throw new ClassCastException("try to mearge " +
                    f.getClass().getName() + " to " + getClass().getName());
        }
    }

    public Factor extract(Factor f) {
        return new Xfactor(getIndex().subtract(f.getIndex()));
    }

    public Factor reciprocal() {
        return new Xfactor(getIndex().negate());
    }
}
