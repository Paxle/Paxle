
package org.paxle.util.ac;

public class MappedArrayNode<E> extends ANode<E> {
	
	private final byte mapLength;
	private final byte[] asciiMap;
	private final byte[] mapAscii;
	private final ANode<E>[] paths;
	
	@SuppressWarnings("unchecked")
	public MappedArrayNode(final byte[] asciiMap, final byte[] mapAscii) {
		if (mapAscii.length >= (int)Byte.MAX_VALUE)
			throw new IllegalArgumentException("mapAscii.length >= 127: " + mapAscii.length);
		mapLength = (byte)(mapAscii.length + 1);
		paths = new ANode[mapLength];
		this.asciiMap = asciiMap;
		this.mapAscii = mapAscii;
	}
	
	public byte[] getKeys() {
		int num = 0;
		for (int i=1; i<mapLength; i++)
			if (paths[i] != null)
				num++;
		final byte[] r = new byte[num];
		num = 0;
		for (byte i=1; i<mapLength; i++)
			if (paths[i] != null)
				r[num++] = mapAscii[i-1];
		return r;
	}
	
	public ANode<E> funcFail() {
		return paths[0];
	}
	
	public ANode<E> funcGoto(byte b) {
		if (b < 0)
			return null;
		return paths[asciiMap[b]];
	}
	
	public void setFail(ANode<E> node) {
		paths[0] = node;
	}
	
	public void setGoto(byte b, ANode<E> node) {
		paths[asciiMap[b]] = node;
	}
}
