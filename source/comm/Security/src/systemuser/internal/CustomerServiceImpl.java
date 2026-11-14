package systemuser.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import kernel.web.ApplicationUtil;
import systemuser.CustomerService;
import systemuser.model.Customer;

public class CustomerServiceImpl implements CustomerService {

	private Map<String, Customer> cache = new ConcurrentHashMap<String, Customer>();

	public void init() {
		List<Customer> list=ApplicationUtil.executeSelect(Customer.class);
		list.forEach(customer->cache.put(customer.getUsername(),customer));
	}

	public void save(Customer entity) {
		ApplicationUtil.executeInsert(entity);
		cache.put(entity.getUsername(), entity);
	}

	/**
	 * 更新
	 * @param entity
	 * @param isOnline true:必须在线才更新，false：都能更新
	 */
	public boolean update(Customer entity, boolean isOnline) {
		if (isOnline && cacheByUsername(entity.getUsername()).getOnline_state() != 1) return false;
		ApplicationUtil.executeUpdate(entity);
		cache.put(entity.getUsername(), entity);
		return true;
	}

	public void delete(String username) {
		Customer entity = cacheByUsername(username);
		ApplicationUtil.executeDelete(entity);
		cache.remove(entity.getUsername());
	}

	public Customer cacheByUsername(String username) {
		return cache.get(username);
	}

	/**
	 * 分配一个在线客服给用户
	 * 
	 * @return
	 */
	public Customer cacheOnlineOne() {
		List<Customer> list = new ArrayList<Customer>(cache.values());
		
		CollectionUtils.filter(list, new Predicate() {// 在线客服
			@Override
			public boolean evaluate(Object arg0) {
				return ((Customer) arg0).getOnline_state() == 1;
			}
		});
		
		if (CollectionUtils.isEmpty(list)) return null;
		
		Collections.sort(list, new Comparator<Customer>() {
			@Override
			public int compare(Customer arg0, Customer arg1) {
				if (arg0.getLast_customer_time() == null) {
					return -1;
				} else if (arg1.getLast_customer_time() == null) {
					return 1;
				}else {
					return (int) (arg0.getLast_customer_time().getTime() - arg1.getLast_customer_time().getTime());
				}
			}
		});
		return list.get(0);
	}
}
