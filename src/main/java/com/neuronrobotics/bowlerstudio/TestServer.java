package com.neuronrobotics.bowlerstudio;

import com.neuronrobotics.sdk.common.*;
import com.neuronrobotics.sdk.common.device.server.BowlerAbstractDeviceServerNamespace;
import com.neuronrobotics.sdk.common.device.server.BowlerAbstractServer;
import com.neuronrobotics.sdk.network.BowlerTCPClient;

import java.io.IOException;

public class TestServer {

    private TestServer() {
    }

    public static void main(String[] args) throws Exception {
        class SampleBowlerServer extends BowlerAbstractServer {
            private BowlerAbstractDeviceServerNamespace ns = new BowlerAbstractDeviceServerNamespace(getMacAddress(), "test.thingy.*;0.3;;") {
            };

            public SampleBowlerServer() {
                super(new MACAddress());

                ns.addRpc(new RpcEncapsulation(ns.getNamespaceIndex(), ns
                        .getNamespace(), "test", BowlerMethod.GET,
                                               new BowlerDataType[]{BowlerDataType.I32,
                                                                    BowlerDataType.I32, BowlerDataType.I32},// send 3
                                               // integers
                                               BowlerMethod.POST, new BowlerDataType[]{
                        BowlerDataType.I32, BowlerDataType.I32,
                        BowlerDataType.I32}, // get 3 integers back
                                               data -> {
                                                   for (Object aData : data)
                                                       System.out.println("Server Got # " + aData);
                                                   return new Object[]{37, 42, 999999};
                                               }));
                addBowlerDeviceServerNamespace(ns);

                Log.info("Starting UDP");
                try {
                    startNetworkServer(1865);
                } catch (IOException e) {
                    e.printStackTrace();
                }// starts the UDP server, this also starts tcp server on port+1, in this case 1866
            }
        }

        class SampleBowlerClient extends BowlerAbstractDevice {
            public void runCommand() {
                Object[] args = send("test.thingy.*;0.3;;", BowlerMethod.GET, "test",
                                     new Object[]{36, 83, 13});// send some numbers
                for (Object arg : args)
                    System.out.println("Client Received  # " + arg);
            }

            @Override
            public void onAsyncResponse(BowlerDatagram data) {
            }// no async in this demo
        }

        SampleBowlerClient client = new SampleBowlerClient();

        //client.setConnection(new UDPBowlerConnection(InetAddress.getByName("127.0.0.1"), 1865));
        // Alternately you can use the tcp connection
        client.setConnection(new BowlerTCPClient("127.0.0.1", 1866));
        DeviceManager.addConnection(client, "sampleClient");

        client.runCommand();// runs our test command from client to server and
        // back
    }
}
