package ch.zhaw.pm2.multichat.client;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.pm2.multichat.Message;

public class ClientMessageList {
	private final List<Message> messageList = new ArrayList<>();

	public List<Message> getMessageList() {
		return messageList;
	}

    public void addMessage(Message message) {
    	messageList.add(message);
    }
}
