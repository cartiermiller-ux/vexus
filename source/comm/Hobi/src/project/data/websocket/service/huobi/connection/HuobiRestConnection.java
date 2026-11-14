package project.data.websocket.service.huobi.connection;

import java.net.MalformedURLException;
import java.net.URL;

import project.data.websocket.constant.Options;

public class HuobiRestConnection {

	private Options options;

	private String host;

	public Options getOptions() {
		return options;
	}

	public HuobiRestConnection(Options options) {
		this.options = options;
		try {
			this.host = new URL(this.options.getRestHost()).getHost();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
}
