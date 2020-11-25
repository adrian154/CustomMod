package dev.codesoup.mc.mcws;

import java.net.InetSocketAddress;
import java.util.List;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import dev.codesoup.mc.CustomMod;
import dev.codesoup.mc.mcws.messages.InMessage;
import dev.codesoup.mc.mcws.messages.OutMessage;

public class WSServer extends WebSocketServer {
	
	private CustomMod mod;
	private List<WebSocket> authedClients;
	
	public WSServer(CustomMod mod) {
		super(new InetSocketAddress(1738));
		this.mod = mod;
		this.start();
	}
	

	@Override
	public void onClose(WebSocket conn, int arg1, String reason, boolean remote) {
		authedClients.remove(conn);
	}

	@Override
	public void onError(WebSocket conn, Exception ex) {
		System.out.println("an error occured on connection with " + conn.getRemoteSocketAddress() + ": " + ex.getMessage());
	}

	@Override
	public void onMessage(WebSocket conn, String strMessage) {
		InMessage message = mod.gson.fromJson(strMessage, InMessage.class);
		String response = message.execute(mod, conn);
		if(response.length() > 0) {
			conn.send(response);
		}
	}

	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		System.out.println("connection from " + conn.getRemoteSocketAddress());
	}

	@Override
	public void onStart() {
		System.out.println("started listening for WebSocket connections");
	}

	public void authorize(WebSocket conn) {
		if(!authedClients.contains(conn)) {
			authedClients.add(conn);
		}
	}
	
	public boolean isAuthorized(WebSocket conn) {
		return authedClients.contains(conn);
	}
	
	public void broadcastMessage(OutMessage message) {
		broadcast(mod.gson.toJson(message));
	}
	
	public void broadcastMessageToAuthed(OutMessage message) {
		for(WebSocket conn: authedClients) {
			conn.send(mod.gson.toJson(message));
		}
	}
	
}
