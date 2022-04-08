package ch.zhaw.pm2.multichat.client;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.pm2.multichat.client.ClientMessageList.MessageType;

/**
 * Adds observable functionality to one ClientMessageList-object.
 * The decorator uses the original methods of the ClientMessageList-object.
 * @author jasarard
 *
 */
public class ClientMessageListDecorator implements IsObservable {
    private final ClientMessageList clientMessageList;
    private List<IsObserver> listener = new ArrayList<>();

    public ClientMessageListDecorator(ClientMessageList clientMessageList) {
        this.clientMessageList = clientMessageList;
    }

    @Override
    public void addListener(IsObserver observer) {
        listener.add(observer);
    }

    @Override
    public void removeListener(IsObserver observer) {
        listener.remove(observer);
    }

    public List<MessageType> getTypeList() {
		return clientMessageList.getTypeList();
	}

	public List<String> getSenderList() {
		return clientMessageList.getSenderList();
	}

	public List<String> getReceiverList() {
		return clientMessageList.getReceiverList();
	}

	public List<String> getMessageList() {
		return clientMessageList.getMessageList();
	}

    public void addMessage(MessageType type, String sender, String receiver, String message) {
        clientMessageList.addMessage(type, sender, receiver, message);
        informListener();
    }
    
    private void informListener() {
		for(IsObserver observer : listener) {
			observer.update();
		}
	}
}
