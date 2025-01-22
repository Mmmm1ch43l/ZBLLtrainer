import java.math.BigInteger;

public class VR {

    private final BR x;
    private final BR y;


    public VR(int x, int y) {
        this.x = new BR(x);
        this.y = new BR(y);
    }

    public VR(int xEnumerator, int xDenominator, int yEnumerator, int yDenominator) {
        this.x = new BR(xEnumerator, xDenominator);
        this.y = new BR(yEnumerator, yDenominator);
    }

    public VR(BR x, BR y){
        this.x = x;
        this.y = y;
    }

    public BR getX() {
        return x;
    }

    public BR getY() {
        return y;
    }

    public boolean isInt() {
        return x.isInt() && y.isInt();
    }

    public boolean isCoprimeInt() {
        if(!isInt()) return false;
        return x.getEnumerator().gcd(y.getEnumerator()).equals(BigInteger.ONE);
    }

    public double doubleValueX() {
        return x.doubleValue();
    }

    public double doubleValueY() {
        return y.doubleValue();
    }

    public VR negate() {
        return new VR(x.negate(),y.negate());
    }
    public int floorAbs() {
        return Math.max(x.floorAbs(), y.floorAbs());
    }

    public BR normSquared() {
        return x.pow(2).add(y.pow(2));
    }

    public BR scalarProduct(VR input) {
        return x.multiply(input.getX()).add(y.multiply(input.getY()));
    }

    public BR crossProduct(VR input) {
        return x.multiply(input.getY()).subtract(y.multiply(input.getX()));
    }

    public int toTheRightOf(VR input) {
        return this.crossProduct(input).signum();
    }

    public VR scale(BR input){
        //if(input.signum() == 0) System.out.println("scaling by 0");
        return new VR(x.multiply(input), y.multiply(input));
    }

    public VR rotateRight() {
        return new VR(y, x.negate());
    }

    public VR rotateLeft() {
        return new VR(y.negate(), x);
    }

    public VR rotateSlightlyRight(int precision) {
        BR smallNumber = (new BR(1, 2)).pow(precision);
        return new VR(x.add(y.multiply(smallNumber)), y.subtract(x.multiply(smallNumber)));
    }

    public VR rotateSlightlyLeft(int precision) {
        BR smallNumber = (new BR(1, 2)).pow(precision);
        return new VR(x.subtract(y.multiply(smallNumber)), y.add(x.multiply(smallNumber)));
    }

    public VR add(VR input) {
        return new VR(x.add(input.getX()),y.add(input.getY()));
    }

    public VR subtract(VR input) {
        return this.add(input.negate());
    }

    public VR clone() {
        return new VR(x, y);
    }

    public void print() {
        System.out.println("(" + doubleValueX() + ", " + doubleValueY() + ")");
    }

    public boolean equals (VR input){
        return x.equals(input.getX()) && y.equals(input.getY());
    }

    public BigInteger lcm () {
        return x.getDenominator().multiply(y.getDenominator()).divide(x.getDenominator().gcd(y.getDenominator()));
    }

    public VR parrentPoint () {
        return this.scale(new BR(this.lcm()));
    }

    public BR divide (VR input) {
        if (input.normSquared().signum() == 0 && this.crossProduct(input).signum() != 0) {
            return new BR(0);
        }
        if (input.getX().signum() != 0) {
            return x.divide(input.getX());
        }
        return y.divide(input.getY());
    }
}
