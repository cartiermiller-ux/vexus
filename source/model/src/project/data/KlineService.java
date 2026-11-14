package project.data;

import java.util.List;

import project.data.model.Kline;
import project.data.model.Realtime;

public interface KlineService {
	
	public void delete(String line, int days);

	public void saveOne(String symbol, String line);
	
	public List<Kline> find(String symbol, String line, int pageSie);
	
	public void saveInit(String symbol,String line,List<Kline> klineList);

	public Kline bulidKline(Realtime realtime, Kline lastOne, Kline hobiOne, String line) ;
}
