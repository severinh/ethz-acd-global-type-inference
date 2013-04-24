package cd.util;

/** Simple class for joining two objects of different type */
public class Tuple<A, B> {

	public final A a;
	public final B b;

	public Tuple(A a, B b) {
		this.a = a;
		this.b = b;
	}

	@Override
	public String toString() {
		return "(" + a.toString() + ", " + b.toString() + ")";
	}

}
