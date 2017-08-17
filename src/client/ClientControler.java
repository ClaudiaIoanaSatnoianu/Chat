package client;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import utilities.Content;
import utilities.Message;
import utilities.Observer;


public class ClientControler implements Observer{

	@FXML
	private Parent name;
	@FXML
	private TextField textName;
	@FXML
	private TextField textIp;
	@FXML
	private TextField textPort;
	@FXML
	private Pane paneConnect;
	@FXML
	private Pane paneChat;
	@FXML
	private TextArea textChatWrite;
	@FXML
	private TextArea textChatRead;
	@FXML
	private Button buttonConnect;
	@FXML
	private ListView<Content> listChat;
	@FXML
	private Button buttonChatSend;
	@FXML
	private TabPane tabPane;
	@FXML
	private TextField textSelectedName;
	@FXML
	private TextField textChatId;
	@FXML
	private ImageView logout;
	@FXML
	private AnchorPane anchorPane;
	
	private Content tmp;
	private LinkedList<Content> list = new LinkedList<Content>();
	private ChatClient client;
	private Content writeTo;
	Thread thread = null;

	@FXML
	public void showName(ActionEvent event) {
		list = new LinkedList<Content>();
		String name;
		String ip;
		int port;
		name = textName.getText();
		ip = textIp.getText();
		port = Integer.parseInt(textPort.getText());
		client = new ChatClient(ip, port, name);
		client.addObserver(this);
		paneConnect.setVisible(false);
		paneChat.setVisible(true);

		try {
			thread = new Thread(client);
			thread.setDaemon(true);
			thread.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void writeXML() {
		try {
			Element root = new Element("parteners");
			Document doc = new Document(root);

			for (Content x : list) {
				Element nod = new Element("partener");
				nod.setAttribute(new Attribute("id", Integer.toString(x.getId())));
				nod.setText(x.getName());
				doc.getRootElement().addContent(nod);
			}

			XMLOutputter xmlOut = new XMLOutputter();

			xmlOut.setFormat(Format.getPrettyFormat());
			xmlOut.output(doc, new FileOutputStream(new File(client.getNume() + ".con")));

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void updateUI(int id, String mesaj) {

		for (Content c : list) {
			if (c.getId() == id) {
				c.append(mesaj);

				StringBuilder sB = new StringBuilder();
				sB.append(c.getName());
				sB.append(".dat");

				File fisier = new File(sB.toString());

				try {
					ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fisier));
					oos.writeObject(new Content(c.getId(), c.getName(), c.getText()));
					oos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (writeTo.getId() != c.getId()) {

					StringBuilder sBld = new StringBuilder();
					sBld.append(">");
					sBld.append(c.getName());
					sBld.append("<");

					c.setName(sBld.toString());

				}

			}
		}
		tmp = new Content(-4, " ", " ");

		list.addLast(tmp);
		updateList();
		list.removeLast();

		if (writeTo != null) {
			textChatRead.setText(writeTo.getText());
		}

	}
	
	public void sendMessage(ActionEvent event) {
		send();
	}

	public void sendEnter(KeyEvent keyEvent) {
		if (keyEvent.getCode() == KeyCode.ENTER) {
			send();
			textChatWrite.clear();
		}
	}

	private void send() {
		String message = textChatWrite.getText();

		if (!message.equals("")) {

			StringBuilder sb = new StringBuilder();
			sb.append(client.getNume());
			sb.append(": ");
			sb.append(message);
			sb.append("\n");

			if (writeTo != null) {
				client.sendMessage(new Message(2, sb.toString(), -1, writeTo.getId()));
				textChatWrite.clear();
			}
		}
		textChatWrite.clear();
	}

	private void updateList() {

		ObservableList<Content> names = FXCollections.observableArrayList();
		if (!list.isEmpty())
			names.addAll(list);

		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				listChat.setMouseTransparent(false);
				listChat.setItems(names);
			}
		});
	}

	public void hoverLogout(MouseEvent mouse) {

		logout.setOpacity(100);
	}

	public void noHoverLogout(MouseEvent mouse) {
		logout.setOpacity(0.4);
	}

	private void add(int id, String message) {

		StringBuilder stringBuilder = new StringBuilder();
		StringBuilder stringBuilder2 = new StringBuilder();

		stringBuilder.append(message);
		stringBuilder.append(".dat");

		File file = new File(stringBuilder.toString());

		if (file.exists()) {
			System.out.println("File opened");
			try {
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file.toString()));

				Content tmpContent = (Content) ois.readObject();
				stringBuilder2.append(tmpContent.getText());

				System.out.println("The text is: " + stringBuilder2.toString() + tmpContent.getText());
				ois.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}

		}
		if (!message.equals(client.getNume())) {
			list.add(new Content(id, message, stringBuilder2.toString()));
		}

		if (writeTo == null) {
			if (!list.isEmpty())
				writeTo = list.getFirst();
			buttonChatSend.setDisable(false);
			textChatWrite.setDisable(false);

		}
		
		writeXML();

	}

	public void update(MouseEvent mouse) {

		if (listChat.getSelectionModel().getSelectedItems() != null) {
			textChatRead.setText(listChat.getSelectionModel().getSelectedItem().getText());

			writeTo = listChat.getSelectionModel().getSelectedItem();

			if (listChat.getSelectionModel().getSelectedItem().getName().contains("<")) {
				listChat.getSelectionModel().getSelectedItem()
						.setName(listChat.getSelectionModel().getSelectedItem().getName().replace("<", ""));
				listChat.getSelectionModel().getSelectedItem()
						.setName(listChat.getSelectionModel().getSelectedItem().getName().replace(">", ""));

				updateList();
			}
			textSelectedName.setText(listChat.getSelectionModel().getSelectedItem().getName());

			if (buttonChatSend.isDisable() || textChatWrite.isDisable()) {
				buttonChatSend.setDisable(false);
				textChatWrite.setDisable(false);
			}
		}
	}

	private void delete(Message message) {
		for (Content c : list) {
			if (c.getId() == message.getIdFrom()) {
				list.remove(c);
			}
		}
		writeXML();
		updateList();
	}

	@SuppressWarnings("deprecation")
	public void logoutEvent(MouseEvent mouse) {
		try {
			client.sendMessage(new Message(4, client.getNume(), -1, -1));
			textChatRead.clear();
			client.shutDown();
			thread.stop();
			client = null;
			if(list.size()>0)
				list.remove();
			list = null;
		} catch (Exception e) {
			System.out.println("Controler error");
			e.printStackTrace();
		}

		paneConnect.setVisible(true);
		paneChat.setVisible(false);

	}

	@Override
	public void notify(Message message) {
		System.out.println("Notification: " + message.getText());
		
		if (message.getType() == 1||message.getType() ==2) {
			updateUI(message.getIdFrom(),message.getText());
		} else if (message.getType() == 3) {
			add(message.getIdFrom(),message.getText());
			updateList();
		} else if (message.getType() == 4) {
			delete(message);
		}
		
	}

}
