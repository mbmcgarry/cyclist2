package edu.utah.sci.cyclist.presenter;

import edu.utah.sci.cyclist.event.notification.CyclistNotification;
import edu.utah.sci.cyclist.event.notification.CyclistNotificationHandler;
import edu.utah.sci.cyclist.event.notification.CyclistNotifications;
import edu.utah.sci.cyclist.event.notification.EventBus;
import edu.utah.sci.cyclist.view.View;

public class PresenterBase implements Presenter {
	private static int _idCounter = 0;
	private String _id;
	
	private View _view;
	private EventBus _eventBus;
	
	public PresenterBase(EventBus bus) {
		_id = "presenter"+_idCounter++;
		_eventBus = bus;
	}
	
	@Override
	public String getId() {
		return _id;
	}
	
	@Override
	public EventBus getEventBus() {
		return _eventBus;
	}
	
	@Override
	public void setView(View view) {
		_view = view;
	}

	public View getView() {
		return _view;
	}

	public void addNotificationHandler(String type, CyclistNotificationHandler handler) {
		_eventBus.addHandler(type, _id, handler);
	}
	
	@Override
	public void broadcast(CyclistNotification notification) {
		notification.setSource(this);
		_eventBus.notify(notification);
	}
}