package factors;

import terms.Term;
import java.math.BigInteger;
import java.util.ArrayList;

public class Sinfactor extends Factor {
    public Sinfactor(BigInteger idx) {
        super(idx);
    }

    public Sinfactor() {
        super();
    }

    //必须传入合法的，在此不做合法性检查
    public Sinfactor(String str) {
        super(str);
    }

    public Term diff() {
        ArrayList<Factor> fl = new ArrayList<>();
        BigInteger idx = this.getIndex();
        if (!idx.equals(BigInteger.ZERO)) {
            fl.add(new Cosfactor(BigInteger.ONE));
            if (!idx.equals(BigInteger.ONE)) {
                fl.add(new Sinfactor(idx.subtract(BigInteger.ONE)));
            }
        }
        return new Term(idx, fl);
    }

    public String toString() {
        if (this.getIndex().equals(BigInteger.ZERO)) {
            return "1";
        } else if (this.getIndex().equals(BigInteger.ONE)) {
            return "sin(x)";
        } else {
            return "sin(x)^" + getIndex().toString();
        }
    }

    public Factor merge(Factor f) throws ClassCastException {
        if (this.getClass() == f.getClass()) {
            return new Sinfactor(basedMerge(f));
        } else {
            throw new ClassCastException("try to mearge " +
                    f.getClass().getName() + " to " + getClass().getName());
        }
    }

    public Factor extract(Factor f) {
        return new Sinfactor(getIndex().subtract(f.getIndex()));
    }

    public Factor reciprocal() {
        return new Sinfactor(getIndex().negate());
    }
}
