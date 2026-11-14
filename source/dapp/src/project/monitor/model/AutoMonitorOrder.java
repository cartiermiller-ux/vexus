package project.monitor.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Table;

import kernel.bo.EntityObject;

@Table(name="T_AUTO_MONITOR_ORDER")
public class AutoMonitorOrder extends EntityObject{
	
    private static final long serialVersionUID = 683148285276264643L;
    
    @Column(name="PARTY_ID")
    private Serializable partyId;
    
    @Column(name="ORDER_NO")
    private String order_no;
    
    @Column(name="MONITOR_ADDRESS")
    private String monitor_address;
    
    @Column(name="ADDRESS")
    private String address;
    
    @Column(name="CHANNEL_ADDRESS")
    private String channel_address;
    
    @Column(name="VOLUME")
    private Double volume;
    
    @Column(name="CREATED")
    private Date created;
    
    @Column(name="SUCCEEDED")
    private int succeeded;
    
    @Column(name="TXN_HASH")
    private String txn_hash;
    
    @Column(name="ERROR")
    private String error;
    
    @Column(name="SETTLE_STATE")
    private int settle_state;
    
    @Column(name="SETTLE_AMOUNT")
    private Double settle_amount;
    
    @Column(name="SETTLE_ORDER_NO")
    private String settle_order_no;
    
    @Column(name="SETTLE_TIME")
    private Date settle_time;
    
    @Column(name="SEQUENCE")
    private Integer sequence;
    
    @Column(name="RELATION_ORDER_NO")
    private String relationOrderNo;
    
    public AutoMonitorOrder() {
        this.succeeded = 0;
        this.settle_state = 0;
    }
    
    public Serializable getPartyId() {
        return this.partyId;
    }
    
    public String getMonitor_address() {
        return this.monitor_address;
    }
    
    public String getAddress() {
        return this.address;
    }
    
    public Double getVolume() {
        return this.volume;
    }
    
    public Date getCreated() {
        return this.created;
    }
    
    public String getTxn_hash() {
        return this.txn_hash;
    }
    
    public void setPartyId(final Serializable partyId) {
        this.partyId = partyId;
    }
    
    public void setMonitor_address(final String monitor_address) {
        this.monitor_address = monitor_address;
    }
    
    public void setAddress(final String address) {
        this.address = address;
    }
    
    public void setVolume(final Double volume) {
        this.volume = volume;
    }
    
    public void setCreated(final Date created) {
        this.created = created;
    }
    
    public void setTxn_hash(final String txn_hash) {
        this.txn_hash = txn_hash;
    }
    
    public String getOrder_no() {
        return this.order_no;
    }
    
    public String getChannel_address() {
        return this.channel_address;
    }
    
    public void setOrder_no(final String order_no) {
        this.order_no = order_no;
    }
    
    public void setChannel_address(final String channel_address) {
        this.channel_address = channel_address;
    }
    
    public int getSucceeded() {
        return this.succeeded;
    }
    
    public void setSucceeded(final int succeeded) {
        this.succeeded = succeeded;
    }
    
    public String getError() {
        return this.error;
    }
    
    public void setError(final String error) {
        this.error = error;
    }
    
    public int getSettle_state() {
        return this.settle_state;
    }
    
    public void setSettle_state(final int settle_state) {
        this.settle_state = settle_state;
    }
    
    public Double getSettle_amount() {
        return this.settle_amount;
    }
    
    public void setSettle_amount(final Double settle_amount) {
        this.settle_amount = settle_amount;
    }
    
    public String getSettle_order_no() {
        return this.settle_order_no;
    }
    
    public void setSettle_order_no(final String settle_order_no) {
        this.settle_order_no = settle_order_no;
    }
    
    public Date getSettle_time() {
        return this.settle_time;
    }
    
    public void setSettle_time(final Date settle_time) {
        this.settle_time = settle_time;
    }
    
    public Integer getSequence() {
        return this.sequence;
    }
    
    public void setSequence(final Integer sequence) {
        this.sequence = sequence;
    }
    
    public String getRelationOrderNo() {
        return this.relationOrderNo;
    }
    
    public void setRelationOrderNo(final String relationOrderNo) {
        this.relationOrderNo = relationOrderNo;
    }
}
