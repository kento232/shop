package jp.ken.shop.domain.entity;

import java.io.Serializable;
import java.time.LocalDate;

import lombok.Data;

@Data

public class OrderEntity implements Serializable {

	private int purchase_id;
	
	private int member_id;
	
	private int used_point;
	
	private int total_amount;
	
	private int used_point_total_amount;
	
	private String cancel_flag;
	
	private LocalDate purchase_day;
	
	private LocalDate sent_day;
	
	private LocalDate cancel_day;
	
	private String valid_flag;
}
