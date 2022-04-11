package ch.zhaw.pm2.multichat.client;

import java.util.ArrayList;
import java.util.List;

import ch.zhaw.pm2.multichat.Message;

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

	public List<Message> getMessageList() {
		return clientMessageList.getMessageList();
	}

    public void addMessage(Message message) {
        clientMessageList.addMessage(message);
        informListener();
    }
    
    private void informListener() {
		for(IsObserver observer : listener) {
			observer.update();
		}
	}
}
