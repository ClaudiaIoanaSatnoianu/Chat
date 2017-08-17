/* Message types
 * 1 - Standard
 * 2 - Private
 * 3 - Add Contact
 * 4 - Delete Contact
 */

package utilities;

import java.io.Serializable;

public class  Message implements Serializable {

	private static final long serialVersionUID = 1L;
	private int type = 0;
	private String text;
	private int idFrom=0;
	private int idTo=0;

	public Message(int type, String text, int idFrom, int idTo) {
		this.type = type;
		this.text = text;
		this.idFrom = idFrom;
		this.idTo = idTo;
	}

	public int getType() {
		return type;
	}

	public String getText() {
		return text;
	}

	public int getIdFrom() {
		return idFrom;
	}
	public int getIdTo() {
		return idTo;
	}
}