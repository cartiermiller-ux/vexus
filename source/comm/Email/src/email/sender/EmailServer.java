package email.sender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.TaskExecutor;

import email.internal.InternalEmailSenderService;
import kernel.util.ThreadUtils;

/**
 * 邮件服务类，负责从短信消息队列取出短信消息并发送
 */
public class EmailServer implements InitializingBean, Runnable {

    private static final Logger logger = LoggerFactory.getLogger(EmailServer.class);

    private TaskExecutor taskExecutor;
    
    private InternalEmailSenderService internalEmailSenderService;

    
    /**
     * 服务运行：
     * 1. 从消息队列获取message
     * 2.调用currentProvider发送短信
     */
    public void run() {
        while (true) {

            try {
                EmailMessage item = EmailMessageQueue.poll();

                if (item != null) {
                	System.out.println("邮寄地址：" + item.getTomail());
                    taskExecutor.execute(new HandleRunner(item));
                }
                else {
                	/*
                	 * 限速，最多1秒20个
                	 */
                    ThreadUtils.sleep(50);
                }

            } catch (Throwable e) {
                logger.error("EmailServer taskExecutor.execute() fail", e);

            }
        }
    }

    public class HandleRunner implements Runnable {
        private EmailMessage item;

        public HandleRunner(EmailMessage item) {
            this.item = item;
        }

        public void run() {
            try {
            	internalEmailSenderService.send(item);
            } catch (Throwable t) {
            	 logger.error("EmailServer taskExecutor.execute() fail", t);
            }
           
        }

    }


    public void afterPropertiesSet() throws Exception {

        new Thread(this, "EmailServer").start();
        if (logger.isInfoEnabled()) {
            logger.info("启动邮件发送服务！");
        }

    }
    

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }


	public void setInternalEmailSenderService(InternalEmailSenderService internalEmailSenderService) {
		this.internalEmailSenderService = internalEmailSenderService;
	}




  

}
