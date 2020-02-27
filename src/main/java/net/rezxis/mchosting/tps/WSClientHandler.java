package net.rezxis.mchosting.tps;

import java.nio.ByteBuffer;

import org.java_websocket.handshake.ServerHandshake;

import net.rezxis.mchosting.network.ClientHandler;

public class WSClientHandler implements ClientHandler {

	@Override
	public void onOpen(ServerHandshake handshakedata) {
	}

	@Override
	public void onMessage(String message) {
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
