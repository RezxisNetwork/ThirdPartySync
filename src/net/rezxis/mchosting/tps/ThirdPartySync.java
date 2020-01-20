package net.rezxis.mchosting.tps;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;

import net.rezxis.mchosting.database.Database;
import net.rezxis.mchosting.database.MySQLProvider;
import net.rezxis.mchosting.network.WSClient;
import net.rezxis.mchosting.network.WSServer;

public class ThirdPartySync {

	public static WSServer server;
	public static WSClient client;
	public static Props props;
	public static double cver = 0.4;
	
	public static void main(String[] args) {
		props = new Props("tps.propertis");
		Database.init(props.DB_HOST,props.DB_USER,props.DB_PASS,props.DB_PORT,props.DB_NAME,false);
		try {
			client = new WSClient(new URI("ws://"+props.SYNC_ADDRESS+":"+props.SYNC_PORT),  new WSClientHandler());
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println("failed to init websocket.");
			return;
		}
		Runtime.getRuntime().addShutdownHook(new Thread(()->{
			try {
				server.stop();
				client.close();
				MySQLProvider.closeAllConnections();
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}));;
		client.connect();
		System.out.println("Listening to 2020 ThirdPartySync Server");
		server = new WSServer(new InetSocketAddress(2020), new WSServerHandler());
		server.start();
	}
}
