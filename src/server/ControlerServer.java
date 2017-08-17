package server;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import utilities.Content;
import utilities.Message;
import utilities.Observer;

public class ControlerServer implements Observer {

	@FXML
	private Pane paneServer;
	@FXML
	private TextArea textServer;
	@FXML
	private ListView<Content> listServer;

	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Date date = new Date();

	Server server;
	Thread thread;
	
	public void notify(Message message) {
		date = new Date();
		StringBuilder stringBuilder = new StringBuilder();
		
		if (message.getType() == 3) {
			
			Platform.runLater(new Runnable() {
				@Override
				public void run() {
					listServer.getItems().add(new Content(0, message.getText(), ""));
					stringBuilder.append(dateFormat.format(date));
					stringBuilder.append(" - " + message.getText() + " is online\n");
					textServer.appendText(stringBuilder.toString());
				}
			});
			
		} else if (message.getType() == 4) {
			
			Platform.runLater(new Runnable() {
				@Override
				public void run() {

					Iterator<Content> iterator = listServer.getItems().iterator();
					while (iterator.hasNext()) {
						    Content content = iterator.next();
						    if(content.getName().equals(message.getText()))
								if(listServer.getItems().indexOf(content)>=0 && listServer.getItems().indexOf(content)<listServer.getItems().size())
										{
											listServer.getItems().remove(listServer.getItems().indexOf(content));
											stringBuilder.append(dateFormat.format(date));
											stringBuilder.append(" - " + message.getText() + " is offline\n");
											textServer.appendText(stringBuilder.toString());
											break;
										}
						}
				}
			});
			
			
		}

	}
	
	@FXML
    protected void initialize() {
		server = Server.getServer();
		server.addObserver(this);
		thread = new Thread(server);
		thread.start();	
    };

}
