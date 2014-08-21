package com.hyeok.maccontrol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class SocketMain {
	private ServerSocket serversocket;
	private Socket socket;
	private final int PORT = 1837;

	public static void main(String args[]) {
		SocketMain main = new SocketMain();
		main.SocketInit();
	}

	private void SocketInit() {
		try {
			serversocket = new ServerSocket(PORT);
			while (true) {
				System.out.println("클라이언트 대기중");
				socket = serversocket.accept();
				System.out.println(socket.getInetAddress() + " 의 클라이언트 연결됨.");
				ReadMessage read = new ReadMessage(socket.getInputStream());
				read.start();
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	class ReadMessage extends Thread {
		private InputStream ReadInputStream;
		private BufferedReader bufferedreader;

		public ReadMessage(InputStream ReadInputStream) {
			this.ReadInputStream = ReadInputStream;
		}

		@Override
		public void run() {
			bufferedreader = new BufferedReader(new InputStreamReader(
					ReadInputStream));
			try {
				while (true) {
					String strcommand = bufferedreader.readLine();
					if (!strcommand.isEmpty()) {
						System.out.println(String.format(
								StringUtil.WAIT_COMMAND, strcommand));
					Command command = new Command(strcommand);
					command.commandwork();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
				killstream();
			} catch (Exception e) {
				e.printStackTrace();
				killstream();
			}
		}

		private void killstream() {
			try {
				System.out.println(StringUtil.DISCONNECT);
				ReadInputStream.close();
				bufferedreader.close();
				interrupted();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}