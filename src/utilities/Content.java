package utilities;

import java.io.Serializable;

public class Content implements Serializable{
	
	private static final long serialVersionUID = 1L;
	int id;
	String name;
	String text;
	
	public Content(int id,String name,String text)
	{
		this.id = id;
		this.name = name;
		this.text = text;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
	
	public void append(String textToAppend)
	{
		text = text + textToAppend;
	}
	
	@Override
	public String toString()
	{
		return name;
	}
	
}
