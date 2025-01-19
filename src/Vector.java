public class Vector {

    private BR x;
    private BR y;


    public Vector(int x, int y) {
        this.x = new BR(x);
        this.y = new BR(y);
    }

    public Vector(int xEnumerator, int xDenominator, int yEnumerator, int yDenominator) {
        this.x = new BR(xEnumerator, xDenominator);
        this.y = new BR(yEnumerator, yDenominator);
    }

    public Vector(BR x, BR y){
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

    public double doubleValueX() {
        return x.doubleValue();
    }

    public double doubleValueY() {
        return y.doubleValue();
    }

    public Vector negate() {
        return new Vector(x.negate(),y.negate());
    }
    public int floorAbs() {
        return Math.max(x.floorAbs(), y.floorAbs());
    }

    public BR normSquared() {
        return x.pow(2).add(y.pow(2));
    }

    public BR scalarProduct(Vector input) {
        return x.multiply(input.getX()).add(y.multiply(input.getY()));
    }

    public BR crossProduct(Vector input) {
        return x.multiply(input.getY()).subtract(y.multiply(input.getX()));
    }

    public int toTheRightOf(Vector input) {
        return this.crossProduct(input).signum();
    }

    public Vector scale(BR input){
        return new Vector(x.multiply(input), y.multiply(input));
    }

    public Vector rotateRight() {
        return new Vector(y, x.negate());
    }

    public Vector rotateLeft() {
        return new Vector(y.negate(), x);
    }

    public Vector rotateSlightlyRight() {
        BR smallNumber = (new BR(1, 10)).pow(2);
        return new Vector(x.add(y.multiply(smallNumber)), y.subtract(x.multiply(smallNumber)));
    }

    public Vector rotateSlightlyLeft() {
        BR smallNumber = (new BR(1, 10)).pow(2);
        return new Vector(x.subtract(y.multiply(smallNumber)), y.add(x.multiply(smallNumber)));
    }
}
