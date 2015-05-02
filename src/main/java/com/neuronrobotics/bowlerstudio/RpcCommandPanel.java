package com.neuronrobotics.bowlerstudio;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javafx.scene.control.CheckBoxTreeItem;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.tree.DefaultMutableTreeNode;

import net.miginfocom.swing.MigLayout;

import com.neuronrobotics.sdk.common.BowlerAbstractDevice;
import com.neuronrobotics.sdk.common.BowlerDataType;
import com.neuronrobotics.sdk.common.BowlerDatagram;
import com.neuronrobotics.sdk.common.RpcEncapsulation;
import com.neuronrobotics.sdk.genericdevice.GenericDevice;


public class RpcCommandPanel extends JPanel implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9199252749669892888L;
	private BowlerAbstractDevice device;
	private RpcEncapsulation rpc;
	private CheckBoxTreeItem<?> rpcDhild;
	private boolean commandsEnabled=false;
	private ArrayList<JTextField> tx = new ArrayList<JTextField>();
	private ArrayList<JLabel> rx = new ArrayList<JLabel>();
	private JButton send = new JButton("Send");
	private JLabel txRpc = new JLabel("****");
	private JLabel rxRpc = new JLabel("****");
	
	public RpcCommandPanel(RpcEncapsulation rpc,BowlerAbstractDevice device, CheckBoxTreeItem<?> rpcDhild){
		this.setRpcDhild(rpcDhild);
		this.setRpc(rpc);
		this.setDevice(device);
		setLayout(new MigLayout());
		add(new JLabel("Namespace"), "cell 0 0,alignx leading");
		add(new JLabel(rpc.getNamespace().split(";")[0]), "cell 1 0,alignx leading");
		
		add(new JLabel("Method"), "cell 0 1,alignx leading");
		add(new JLabel(rpc.getDownstreamMethod().toString()), "cell 1 1,alignx leading");
		
		add(new JLabel("RPC"), "cell 0 2,alignx leading");
		add(new JLabel(rpc.getRpc()), "cell 1 2,alignx leading");
		
		add(new JLabel("Tx>>"), "cell 0 3,alignx leading");
		txRpc.setText(rpc.getRpc());
		add(txRpc, "cell 0 3,alignx leading");
		add(new JLabel("Rx<<"), "cell 0 4,alignx leading");
		add(rxRpc, "cell 0 4,alignx leading");
		add(send,"cell 2 3,alignx leading");
		
		JPanel txPanel = new JPanel(new MigLayout());
		JPanel rxPanel = new JPanel(new MigLayout());
		
		int i=0;
		for (BowlerDataType s:rpc.getDownstreamArguments()){
			JTextField tmp= new JTextField(5);
			tmp.setText("0");
			txPanel.add(new JLabel(s.toString()), "cell "+i+" 0,alignx leading");
			tx.add(tmp);
			i++;
		}
		i=0;
		for (BowlerDataType s:rpc.getUpstreamArguments()){
			JLabel tmp= new JLabel();
			tmp.setText("0");
			rxPanel.add(new JLabel(s.toString()),  "cell "+i+" 0,alignx leading");
			rx.add(tmp);
			i++;
		}
		i=0;
		for(JTextField t:tx){
			txPanel.add(t, "cell "+i+" 1,alignx leading");
			i++;
		}
		i=0;
		for(JLabel t:rx){
			rxPanel.add(t, "cell "+i+" 1,alignx leading");
			i++;
		}
		
		add(txPanel, "cell 1 3,alignx leading");
		add(rxPanel, "cell 1 4,alignx leading");
		
		send.addActionListener(this);
	}

	public RpcEncapsulation getRpc() {
		return rpc;
	}

	private void setRpc(RpcEncapsulation rpc) {
		this.rpc = rpc;
	}

	public BowlerAbstractDevice getDevice() {
		return device;
	}

	private void setDevice(BowlerAbstractDevice device) {
		this.device = device;
	}

	public CheckBoxTreeItem<?> getRpcDhild() {
		return rpcDhild;
	}

	public void setRpcDhild(CheckBoxTreeItem<?> rpcDhild) {
		this.rpcDhild = rpcDhild;
	}

	public void enableCommands() {
		if(commandsEnabled)
			return;
		
		commandsEnabled = true;	
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object[] values = new Object[tx.size()];
		for (int i=0;i<values.length;i++){
			if(rpc.getDownstreamArguments()[i] == BowlerDataType.ASCII)
				values[i] = tx.get(i).getText();
			else
				values[i] = Integer.parseInt(tx.get(i).getText());
		}	 
		BowlerDatagram bd =device.send(rpc.getCommand(values));
		rxRpc.setText(bd.getRPC());
		Object [] up = rpc.parseResponse(bd);
		for(int i=0;i<up.length;i++){
			if(up[i]!=null)
				rx.get(i).setText(up[i].toString());
			else{
				rx.get(i).setText("Null");
			}
		}
	}

}
