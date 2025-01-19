import java.math.BigInteger;

public class BR {

    private BigInteger enumerator;
    private BigInteger denominator;

    public BR(int input) {
        this.enumerator = BigInteger.valueOf(input);
        this.denominator = BigInteger.ONE;
    }

    public BR(int enumerator, int denominator) {
        this.enumerator = BigInteger.valueOf(enumerator);
        this.denominator = BigInteger.valueOf(denominator);
        this.reduce();
    }

    public BR(BigInteger enumerator, BigInteger denominator){
        this.enumerator = enumerator;
        this.denominator = denominator;
    }

    public BR(BigInteger enumerator, BigInteger denominator, boolean safe){
        this.enumerator = enumerator;
        this.denominator = denominator;
        if (safe) this.reduce();
    }

    private void reduce(){
        BigInteger gcd = enumerator.gcd(denominator);
        enumerator = enumerator.divide(gcd).multiply(BigInteger.valueOf(denominator.signum()));
        denominator = denominator.abs().divide(gcd);
    }

    public BigInteger getEnumerator() {
        return enumerator;
    }

    public BigInteger getDenominator() {
        return denominator;
    }

    public boolean isInt() {
        return denominator.equals(BigInteger.ONE);
    }

    public double doubleValue() {
        if (denominator.equals(BigInteger.ZERO)) return 0;
        return enumerator.doubleValue()/denominator.doubleValue();
    }

    public BR negate() {
        return new BR(enumerator.negate(),denominator);
    }

    public BR invert() {
        return new BR(denominator.multiply(BigInteger.valueOf(enumerator.signum())),enumerator.abs());
    }

    public BR add(BR input){
        return new BR(enumerator.multiply(input.getDenominator()).add(input.getEnumerator().multiply(denominator)),denominator.multiply(input.getDenominator()),true);
    }

    public BR subtract(BR input){
        return this.add(input.negate());
    }

    public BR multiply(BR input){
        return new BR(enumerator.multiply(input.getEnumerator()),denominator.multiply(input.getDenominator()),true);
    }

    public BR divide(BR input){
        return this.multiply(input.invert());
    }

    public BR pow(int exponent){
        return new BR(enumerator.pow(exponent),denominator.pow(exponent));
    }

    public BR abs(){
        return new BR(enumerator.abs(),denominator);
    }

    public int compareTo(BR input){
        return enumerator.multiply(input.getDenominator()).compareTo(input.getEnumerator().multiply(denominator));
    }

    public int signum() {
        return enumerator.signum();
    }

    public int floorAbs() {
        return enumerator.abs().divide(denominator).intValue();
    }
}
