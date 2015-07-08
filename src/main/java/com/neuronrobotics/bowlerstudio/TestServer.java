package com.neuronrobotics.bowlerstudio;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.BowlerDataType;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.common.BowlerMethod;
import com.neuronrobotics.sdk.common.DeviceManager;
import com.neuronrobotics.sdk.common.Log;
import com.neuronrobotics.sdk.common.MACAddress;
import com.neuronrobotics.sdk.common.RpcEncapsulation;
import com.neuronrobotics.sdk.common.device.server.BowlerAbstractDeviceServerNamespace;
import com.neuronrobotics.sdk.common.device.server.BowlerAbstractServer;
import com.neuronrobotics.sdk.common.device.server.IBowlerCommandProcessor;
import com.neuronrobotics.sdk.network.BowlerTCPClient;
import com.neuronrobotics.sdk.network.UDPBowlerConnection;

public class TestServer {

	public static void main(String[] args) throws Exception {
			class SampleBowlerServer extends BowlerAbstractServer {
				BowlerAbstractDeviceServerNamespace ns = new BowlerAbstractDeviceServerNamespace(
						getMacAddress(), "test.thingy.*;0.3;;") {
				};

				public SampleBowlerServer() {
					super(new MACAddress());

					ns.addRpc(new RpcEncapsulation(ns.getNamespaceIndex(), ns
							.getNamespace(), "test", BowlerMethod.GET,
							new BowlerDataType[] { BowlerDataType.I32,
									BowlerDataType.I32, BowlerDataType.I32 },// send 3
																				// integers
							BowlerMethod.POST, new BowlerDataType[] {
									BowlerDataType.I32, BowlerDataType.I32,
									BowlerDataType.I32 }, // get 3 integers back
							new IBowlerCommandProcessor() {
								public Object[] process(Object[] data) {
									for (int i = 0; i < data.length; i++) {
										System.out.println("Server Got # " + data[i]);
									}
									return new Object[] { 37,42, 999999};
								}
							}));
					addBowlerDeviceServerNamespace(ns);

					Log.info("Starting UDP");
					try {
						startNetworkServer(1865);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}// starts the UDP server
						// this also starts tcp server on port+1, in this case 1866

				}
			}

			class SampleBowlerClient extends BowlerAbstractDevice {

				public void runCommand() {
					Object[] args = send("test.thingy.*;0.3;;", BowlerMethod.GET, "test",
							new Object[] { 36, 83, 13 });// send some numbers
					for (int i = 0; i < args.length; i++) {
						System.out.println("Client Received  # " + args[i]);
					}
				}

				@Override
				public void onAsyncResponse(BowlerDatagram data) {
				}// no async in this demo
			}

			SampleBowlerServer server = new SampleBowlerServer();
			
			SampleBowlerClient client = new SampleBowlerClient();
			
			//client.setConnection(new UDPBowlerConnection(InetAddress.getByName("127.0.0.1"), 1865));
			// Alternately you can use the tcp connection
			 client.setConnection( new BowlerTCPClient("127.0.0.1",1866));
			DeviceManager.addConnection(client, "sampleClient");

			client.runCommand();// runs our test command from client to server and
								// back
		
	}

}
