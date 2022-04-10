package ch.zhaw.pm2.multichat.client;

import java.util.ArrayList;
import java.util.List;

public class ClientMessageList {
	private final List<MessageType> typeList = new ArrayList<>();
	private final List<String> senderList = new ArrayList<>();
	private final List<String> receiverList = new ArrayList<>();
	private final List<String> messageList = new ArrayList<>();

	public List<MessageType> getTypeList() {
		return typeList;
	}

	public List<String> getSenderList() {
		return senderList;
	}

	public List<String> getReceiverList() {
		return receiverList;
	}

	public List<String> getMessageList() {
		return messageList;
	}

    public void addMessage(MessageType type, String sender, String receiver, String message) {
	    typeList.add(type);
    	senderList.add(sender);
    	receiverList.add(receiver);
    	messageList.add(message);
    }

    public enum MessageType {
        INFO, MESSAGE, ERROR;
    }

}
