package project.wallet.rate;

import java.util.List;
import java.util.Map;

import kernel.web.Page;

public interface ExchangeRateService {

	public List<ExchangeRate> findBy(String out_or_in);

	public ExchangeRate findBy(String out_or_in, String currency);

	public ExchangeRate get(String id);

	public Page pagedQuery(int pageNo, int pageSize, String out_or_in);

	public void update(ExchangeRate entity);

}
