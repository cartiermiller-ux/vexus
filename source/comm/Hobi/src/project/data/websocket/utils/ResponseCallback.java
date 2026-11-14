package project.data.websocket.utils;

@FunctionalInterface
public interface ResponseCallback<T> {

	void onResponse(T response);
}
