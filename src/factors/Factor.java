package factors;

import java.math.BigInteger;
import terms.Term;

public abstract class Factor {

    private BigInteger index;

    public Factor(BigInteger idx) {
        index = idx;
    }

    public Factor() {
        index = BigInteger.ZERO;
    }

    public Factor(String str) {
        index = parseString(str);
    }

    public abstract Term diff();

    public abstract String toString();

    public abstract Factor merge(Factor f);

    public abstract Factor extract(Factor f);

    public abstract Factor reciprocal();

    protected BigInteger parseString(String str1) {
        String str = str1;
        str = str.replaceAll("\\s+", "");
        BigInteger idx = BigInteger.ONE;
        if (str.contains("^")) {
            idx = new BigInteger(str.split("\\^")[1]);
        }
        return idx;
    }

    //不可变
    protected BigInteger basedMerge(Factor f) {
        return this.getIndex().add(f.getIndex());
    }

    //不可变
    public BigInteger getIndex() {
        return index;
    }

    public static Factor factorFactory(String str) {
        if (str.contains("sin")) {
            return new Sinfactor(str);
        } else if (str.contains("cos")) {
            return new Cosfactor(str);
        } else {
            return new Xfactor(str);
        }
    }
}
