package ch.zhaw.pm2.multichat.client;

import java.util.ArrayList;
import java.util.List;

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
    
    private void informListener() {
		for(IsObserver observer : listener) {
			observer.update();
		}
	}
}
