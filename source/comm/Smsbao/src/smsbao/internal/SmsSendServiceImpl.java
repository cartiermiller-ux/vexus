package smsbao.internal;

import smsbao.SmsSendService;
import smsbao.sender.SmsMessage;
import smsbao.sender.SmsMessageQueue;

public class SmsSendServiceImpl implements SmsSendService {

	@Override
	public void send(String mobile, String content) {
		SmsMessage smsMessage = new SmsMessage(mobile, content);
		String strh = "";
		strh = mobile.substring(0, 2);
		if ("86".equals(strh)) {
			smsMessage.setInter(false);
		}

		SmsMessageQueue.add(smsMessage);
	}

}
