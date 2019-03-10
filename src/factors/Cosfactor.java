package factors;

import terms.Term;
import java.math.BigInteger;
import java.util.ArrayList;

public class Cosfactor extends Factor {
    public Cosfactor(BigInteger idx) {
        super(idx);
    }

    public Cosfactor() {
        super();
    }

    //必须传入合法的，在此不做合法性检查
    public Cosfactor(String str) {
        super(str);
    }

    public Term diff() {
        ArrayList<Factor> fl = new ArrayList<>();
        BigInteger idx = this.getIndex();
        if (!idx.equals(BigInteger.ZERO)) {
            fl.add(new Sinfactor(BigInteger.ONE));
            if (!idx.equals(BigInteger.ONE)) {
                fl.add(new Cosfactor(idx.subtract(BigInteger.ONE)));
            }
        }
        return new Term(idx.negate(), fl);
    }

    public String toString() {
        if (this.getIndex().equals(BigInteger.ZERO)) {
            return "1";
        } else if (this.getIndex().equals(BigInteger.ONE)) {
            return "cos(x)";
        } else {
            return "cos(x)^" + getIndex().toString();
        }
    }

    public Factor merge(Factor f) throws ClassCastException {
        if (this.getClass() == f.getClass()) {
            return new Cosfactor(basedMerge(f));
        } else {
            throw new ClassCastException("try to mearge " +
                    f.getClass().getName() + " to " + getClass().getName());
        }
    }

    public Factor extract(Factor f) {
        return new Cosfactor(getIndex().subtract(f.getIndex()));
    }

    public Factor reciprocal() {
        return new Cosfactor(getIndex().negate());
    }
}
