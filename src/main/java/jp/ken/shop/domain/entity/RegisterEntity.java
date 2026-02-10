package jp.ken.shop.domain.entity;

import java.time.LocalDate;

import lombok.Data;

@Data
public class RegisterEntity {

	private Integer memberId;
	private String memberMail;
	private String memberName;
	private String memberKana;
	private String memberPost;
	private String memberAddress;
	private String memberPhone;
	private LocalDate memberBirthday;
	private String memberPayment;
	private String creditType;
	private String creditNumber;
	private String creditName;
	private String creditSecurityNumber;
	private String memberPassword;
	private Integer memberPoint;
	private LocalDate signupDay;
	private LocalDate withdrawalDay;
	private String comment;
	private String validFlag;

	public String getPaymentDisplay() {
		if ("1".equals(this.memberPayment)) {
			return "クレジットカード";
		} else if("2".equals(this.memberPayment)){
			return "現金";
		}
		return "不明";

	}

	// getter / setter
}
