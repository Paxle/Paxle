
package org.paxle.util.ac;

public class ArrayNode<E> extends ANode<E> {
	
	private final ANode<E>[] gotoMap;
	private ANode<E> fail = null;
	
	@SuppressWarnings("unchecked")
	public ArrayNode() {
		gotoMap = new ANode[128];
	}
	
	@Override
	public ANode<E> funcFail() {
		return fail;
	}
	
	@Override
	public ANode<E> funcGoto(byte b) {
		if (b < 0)
			return null;
		return gotoMap[b];
	}
	
	@Override
	public byte[] getKeys() {
		int count = 0;
		for (byte i=0; i>=0; i++)
			if (gotoMap[i] != null)
				count++;
		final byte[] r = new byte[count];
		count = 0;
		for (byte i=0; i>=0; i++)
			if (gotoMap[i] != null)
				r[count++] = i;
		return r;
	}
	
	@Override
	public void setFail(ANode<E> node) {
		fail = node;
	}
	
	@Override
	public void setGoto(byte b, ANode<E> node) {
		gotoMap[b] = node;
	}
}
