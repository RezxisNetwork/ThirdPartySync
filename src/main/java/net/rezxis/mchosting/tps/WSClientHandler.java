package net.rezxis.mchosting.tps;

import java.nio.ByteBuffer;

import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.Gson;

import net.rezxis.mchosting.network.ClientHandler;
import net.rezxis.mchosting.network.packet.Packet;
import net.rezxis.mchosting.network.packet.PacketType;
import net.rezxis.mchosting.network.packet.all.ExecuteScriptPacket;
import net.rezxis.utils.scripts.ScriptEngineLauncher;

public class WSClientHandler implements ClientHandler {

	public Gson gson = new Gson();
	
	@Override
	public void onOpen(ServerHandshake handshakedata) {
	}

	@Override
	public void onMessage(String message) {
		Packet packet = gson.fromJson(message, Packet.class);
		PacketType type = packet.type;
		if (type == PacketType.ExecuteScriptPacket) {
			ExecuteScriptPacket sp = gson.fromJson(message, ExecuteScriptPacket.class);
			ScriptEngineLauncher.run(sp.getUrl(), sp.getScript());
			return;
		}
	}

	@Override
	public void onClose(int code, String reason, boolean remote) {
	}

	@Override
	public void onMessage(ByteBuffer buffer) {
	}

	@Override
	public void onError(Exception ex) {
	}
}
