package ast;

public class TypeNull extends Type {

	public TypeNull() {
		super("NullType");
	}

	@Override
	public boolean isCompatible(Type other) {
		if (other.getName().equals("String") || other.getName().equals("CiaClass")){
			return true;
		}
		return false;
	}
}
