package net.rezxis.mchosting.tps;

import java.nio.ByteBuffer;
import java.util.Date;
import java.util.HashMap;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;

import com.google.gson.Gson;

import net.rezxis.mchosting.database.Tables;
import net.rezxis.mchosting.database.object.ServerWrapper;
import net.rezxis.mchosting.database.object.server.DBThirdParty;
import net.rezxis.mchosting.network.ServerHandler;
import net.rezxis.mchosting.network.packet.sync.SyncThirdPartyPacket;
import net.rezxis.mchosting.network.packet.sync.SyncThirdPartyPacket.Action;
import net.rezxis.thirdParty.packet.TAuthServerPacket;
import net.rezxis.thirdParty.packet.TAuthServerResponse;
import net.rezxis.thirdParty.packet.TPacket;
import net.rezxis.thirdParty.packet.TServerStoppedPacket;
import net.rezxis.thirdParty.packet.TServerUpdatePacket;

public class WSServerHandler implements ServerHandler {
	
	private Gson gson = new Gson();
	public static HashMap<WebSocket,Integer> connections = new HashMap<>();
	
	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {
		System.out.println("A connection was established");
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		if (connections.containsKey(conn)) {
			DBThirdParty dtp = Tables.getTTable().getByID(connections.get(conn));
			dtp.setMax(0);
			dtp.setPlayers(0);
			dtp.setHost("");
			dtp.setName("");
			dtp.setPort(-1);
			dtp.setOnline(false);
			dtp.update();
			ThirdPartySync.client.send(gson.toJson(new SyncThirdPartyPacket(dtp.getKey(), Action.STOP)));
			connections.remove(conn);
		}
		System.out.println("closed / code : "+code+" / reason : "+reason+" / remote : "+remote);
	}

	@Override
	public void onMessage(WebSocket conn, String message) {
		TPacket packet = gson.fromJson(message, TPacket.class);
		if (packet.getId() == 1) {
			TAuthServerPacket tap = gson.fromJson(message, TAuthServerPacket.class);
			System.out.println(message);
			DBThirdParty dtp = Tables.getTTable().getByKey(tap.getToken());
			TAuthServerResponse res = null;
			System.out.println("tried to connect with "+tap.getToken()+":"+conn.getRemoteSocketAddress().getAddress().getHostAddress());
			if (dtp == null) {
				res = new TAuthServerResponse(-1, "Invalid thirdparty key!");
				conn.send(gson.toJson(res));
				return;
			}
			if (dtp.isLocked()) {
				res = new TAuthServerResponse(-1, "Your ThirdParty was locked.");
				conn.send(gson.toJson(res));
				return;
			}
			if (dtp.isOnline()) {
				res = new TAuthServerResponse(-1, "A server connected with your key!");
				conn.send(gson.toJson(res));
				return;
			}
			if (ServerWrapper.getServerByName(tap.getName()) != null) {
				res = new TAuthServerResponse(-1, "A server same name is exists");
				conn.send(gson.toJson(res));
				return;
			}
			if (tap.getName().contains("lobby")) {
				res = new TAuthServerResponse(-1, "That name doesn't allow");
				conn.send(gson.toJson(res));
				return;
			}
			if (dtp.getExpire().before(new Date())) {
				res = new TAuthServerResponse(-1, "your thirdparty was expired");
				conn.send(gson.toJson(res));
				return;
			}
			if (tap.getVersion() != ThirdPartySync.cver) {
				res = new TAuthServerResponse(-1, "your thirdparty plugin is outdated, please update your thirdparty plugin.");
				conn.send(gson.toJson(res));
				return;
			}
			if (tap.getIcon().equalsIgnoreCase("air")) {
				res = new TAuthServerResponse(-1, "that icon is not allowed");
				conn.send(gson.toJson(res));
				return;
			}
			if (tap.getName().length() <= 16) {
				res = new TAuthServerResponse(-1, "Server name is 16 characters or less");
				conn.send(gson.toJson(res));
				return;
			}
			dtp.setName(tap.getName());
			dtp.setMotd(tap.getMotd());
			dtp.setMax(tap.getMax());
			dtp.setPlayers(tap.getPlayers());
			dtp.setHost(conn.getRemoteSocketAddress().getAddress().getHostAddress());
			dtp.setPort(tap.getPort());
			dtp.setOnline(true);
			dtp.setIcon(tap.getIcon());
			dtp.setVisible(tap.isVisible());
			dtp.update();
			res = new TAuthServerResponse(0, "");
			conn.send(gson.toJson(res));
			//send to sync
			ThirdPartySync.client.send(gson.toJson(new SyncThirdPartyPacket(dtp.getKey(), Action.START)));
			connections.put(conn, dtp.getId());
		} else if (packet.getId() == 3) {
			TServerStoppedPacket tsp = gson.fromJson(message, TServerStoppedPacket.class);
			DBThirdParty dtp = Tables.getTTable().getByKey(tsp.getToken());
			dtp.setMax(0);
			dtp.setPlayers(0);
			dtp.setHost("");
			dtp.setName("");
			dtp.setPort(0);
			dtp.setOnline(false);
			dtp.update();
			//send to sync
			ThirdPartySync.client.send(gson.toJson(new SyncThirdPartyPacket(dtp.getKey(), Action.STOP)));
			connections.remove(conn);
		} else if (packet.getId() == 4) {
			TServerUpdatePacket tup = gson.fromJson(message, TServerUpdatePacket.class);
			DBThirdParty dtp = Tables.getTTable().getByKey(tup.getToken());
			if (dtp == null)
				return;
			if (ServerWrapper.getServerByName(tup.getName()) != null)
				return;
			if (tup.getName() != null)
				dtp.setName(tup.getName());
			if (tup.getMotd() != null)
				dtp.setMotd(tup.getMotd());
			if (tup.getIcon() != null)
				dtp.setIcon(tup.getIcon());
			dtp.update();
		}
	}

	
	@Override
	public void onError(WebSocket conn, Exception ex) {
		ex.printStackTrace();
	}

	@Override
	public void onMessage(WebSocket conn, ByteBuffer buffer) {
	}

	@Override
	public void onStart() {
	}
}