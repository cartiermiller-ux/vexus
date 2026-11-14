package project.web.api;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kernel.web.ResultObject;
import project.Constants;
import project.wallet.rate.ExchangeRateService;

@RestController
@CrossOrigin
public class ExchangeRateController {

	private static final Logger logger = LoggerFactory.getLogger(ExchangeRateController.class);

	@Autowired
	private ExchangeRateService exchangeRateService;

	@RequestMapping("api/exchangerate!list.action")
	public Object list() throws IOException {

		ResultObject resultObject = new ResultObject();

		try {
			String out_or_in = Constants.OUT_OR_IN_DEFAULT;
			resultObject.setData(this.exchangeRateService.findBy(out_or_in));
		} catch (Exception e) {
			resultObject.setCode("1");
			resultObject.setMsg("程序错误");
			logger.error("error:", e);
		}
		return resultObject;
	}
}
