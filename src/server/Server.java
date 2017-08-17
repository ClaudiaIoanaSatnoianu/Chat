package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import utilities.Message;
import utilities.Observer;
import utilities.Subject;

public class Server implements Subject, Observer, Runnable {

	private ServerSocket serverSocket = null;
	private Socket connection = new Socket();
	private List<ServerThread> clients = Collections.synchronizedList(new LinkedList<ServerThread>());
	private boolean accept = true;
	private int id = 0;
	private ServerThread serverThread = null;
	private static Server server = null;
	private List<Observer> observers = new LinkedList<Observer>();
	private Date date;
	private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	public static Server getServer() {
		if (null == server) {
			server = new Server();
		}
		return server;
	}

	public synchronized boolean sendMessage(Message message) {
		boolean check = true;

		try {

			if (message.getType() == 1) {
				for (ServerThread existingThread : clients) {
					existingThread.send(message);
				}
				
			} else if (message.getType() == 2) {
				
				for (ServerThread existingThread : clients) {
					if (existingThread.getId() == message.getIdTo()) {
						existingThread.send(message);
					} else if (existingThread.getId() == message.getIdFrom()) {
						existingThread.send(new Message(message.getType(), message.getText(), message.getIdTo(),
								message.getIdTo()));
					}
				}
				
			} else if (message.getType() == 3) {
				
				for (ServerThread existingThread : clients) {
					if (message.getIdFrom() != existingThread.getId())
						existingThread.send(message);
				}
				
			} else if (message.getType() == 4) {

				for (ServerThread firExistent : clients) {
					if (firExistent.getId() == message.getIdFrom()) {
						clients.remove(firExistent);
					}
				}

				for (ServerThread firExistent : clients) {
					firExistent.send(message);
				}

			}
		} catch (Exception e) {
			System.out.println("The message could not be sent: " + e.toString());
			check = false;
		}

		return check;
	}

	public boolean startServer() {
		boolean check = true;
		date = new Date();
		try {
			serverSocket = new ServerSocket(8080);
			System.out.println("[" + dateFormat.format(date) + "] The server is running on port 8080..");
		} catch (IOException e) {
			System.out.println("[" + dateFormat.format(date) + "] Error starting server: " + e.toString());
			check = false;
		}
		return check;

	}

	public void acceptConnections() {
		while (accept) {
			date = new Date();
			try {
				notifyAllObservers(new Message(5, "The server accepts a new client...",-1,-1));
				System.out.println("[" + dateFormat.format(date) + "] The server is waiting for clients..");
				connection = serverSocket.accept();
				System.out.println("[" + dateFormat.format(date) + "] The server has found a new client");
				id++;
				serverThread = new ServerThread(connection, id);
				System.out.println("[" + dateFormat.format(date) + "] The id of the new client is: " + serverThread.getId());
				serverThread.addObserver(this);
				clients.add(serverThread);
				new Thread(serverThread).start();

			} catch (Exception e) {
				System.out.println("[" + dateFormat.format(date) + "] Error accepting a new client: " + e.toString());
			}
		}

	}

	synchronized boolean sendConnectedPersons(int send) {
		boolean check = true;
		Message data;
		ServerThread serverThread = null;

		try {

			for (ServerThread existingThread : clients) {
				if (existingThread.getId() == send) {
					serverThread = existingThread;
				}
			}

			if (serverThread != null) {
				if(clients.size()!=0) {
					date = new Date();
					System.out.println("[" + dateFormat.format(date) + "] The person with the id "+ 
					serverThread.getId() + " send connected persons to: " );
					for (ServerThread existingThread : clients) {
						data = new Message(3, existingThread.getName(), existingThread.getId(), serverThread.getId());
						if (existingThread.getId() != send) {
							serverThread.send(data);
							System.out.println(existingThread.getId() + " " + existingThread.getName());
						}
					}					
				}
			}

		} catch (Exception e) {
			System.out.println("[" + dateFormat.format(date) + "] The connected users could not be sent: " + e.toString());
			check = false;
		}

		return check;
	}

	@Override
	public void addObserver(Observer observer) {
		observers.add(observer);
	}

	@Override
	public void deleteObserver(Observer observer) {
		observers.remove(observer);

	}

	@Override
	public void notifyAllObservers(Message message) {
		for (Observer observer : observers) {
			observer.notify(message);
		}
	}

	@Override
	public void notify(Message message) {
		
		try {
			if (message.getType() == 5) {
				sendConnectedPersons(message.getIdFrom());
			} else {
				sendMessage(message);
				notifyAllObservers(message);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		notifyAllObservers(new Message(5, "Starting the server...",-1,-1));
		startServer();
		notifyAllObservers(new Message(5, "Server has started", -1,-1));	
		acceptConnections();
	}

}
