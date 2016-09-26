package osh.old.busdriver.wago.parser;

import java.util.Iterator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Iterator for a NodeList (XML DOM)
 *
 */
public class NodeListIterable implements Iterable<Node> {
	private NodeList list;

	public NodeListIterable(NodeList list) {
		this.list = list;
	}

	public static class NodeListIterator implements Iterator<Node> {
		private NodeList list;
		private int pos = 0;

		public NodeListIterator(NodeList list) {
			this.list = list;
		}

		@Override
		public boolean hasNext() {
			return pos < list.getLength();
		}

		@Override
		public Node next() {
			return list.item(pos++);
		}

		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}

	}

	@Override
	public Iterator<Node> iterator() {
		return new NodeListIterator(list);
	}
}
