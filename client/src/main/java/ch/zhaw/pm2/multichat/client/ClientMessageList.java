package ch.zhaw.pm2.multichat.client;

import ch.zhaw.pm2.multichat.protocol.Message;

import java.util.ArrayList;
import java.util.List;

public class ClientMessageList {
	private final List<Message> messageList = new ArrayList<>();

	public List<Message> getMessageList() {
		return messageList;
	}

    public void addMessage(Message message) {
    	messageList.add(message);
    }
}
